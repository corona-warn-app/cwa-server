package app.coronawarn.server.services.distribution.objectstore.publish;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A set of files, which are subject for publishing to S3.
 */
public class PublishFileSet {

  /** the root folder from which to read all files. */
  private final Path root;

  /** the list of identified files in the root folder. */
  private final List<LocalFile> files;

  public PublishFileSet(Path root) throws IOException {
    this.root = root;
    this.files = getFilesOnPath(root);
  }

  private List<LocalFile> getFilesOnPath(Path path) throws IOException {
    if (path == null || !path.toFile().isDirectory()) {
      throw new UnsupportedOperationException("Supplied path is not a folder: " + path);
    }

    try (Stream<Path> stream = Files.walk(path, Integer.MAX_VALUE)) {
      return stream
          .filter(Files::isRegularFile)
          .map(this::constructPublishFile)
          .collect(Collectors.toList());
    }
  }

  private LocalFile constructPublishFile(Path path) {
    if (path.endsWith("index")) {
      return new LocalIndexFile(path, root);
    }

    return new LocalGenericFile(path, root);
  }

  public List<LocalFile> getFiles() {
    return files;
  }
}
