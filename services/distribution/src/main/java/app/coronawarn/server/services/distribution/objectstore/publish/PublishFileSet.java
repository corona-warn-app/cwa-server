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

  private final Path root;

  private final List<PublishFile> files;

  public PublishFileSet(Path root) throws IOException {
    this.root = root;
    this.files = getFiles(root);
  }

  private List<PublishFile> getFiles(Path path) throws IOException {
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

  private PublishFile constructPublishFile(Path path) {
    if (path.endsWith("index")) {
      return new PublishIndexFile(path, root);
    }

    return new PublishFile(path, root);
  }

  public List<PublishFile> getFiles() {
    return files;
  }
}
