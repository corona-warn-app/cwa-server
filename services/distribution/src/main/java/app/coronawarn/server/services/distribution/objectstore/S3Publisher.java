package app.coronawarn.server.services.distribution.objectstore;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class S3Publisher {

  private Logger logger = LoggerFactory.getLogger(this.getClass());

  private String prefixPath = "cwa/";

  private ObjectStoreAccess objectStoreAccess;

  @Autowired
  public S3Publisher(ObjectStoreAccess objectStoreAccess) {
    this.objectStoreAccess = objectStoreAccess;
  }

  public void publishFolder(Path path) throws IOException {
    try (Stream<Path> stream = Files.walk(path, Integer.MAX_VALUE)) {
      stream.filter(Files::isRegularFile).forEach(file -> publishFile(file, path));
    }
  }

  public void deleteFolder(String path) {
    objectStoreAccess.deleteFilesWithPrefix(prefixPath + path);
  }

  public void deleteFolder(Path file, Path root) {
    objectStoreAccess.deleteFilesWithPrefix(createS3Key(file, root));
  }

  public void publishFile(Path file, Path root) {
    String publishingPath = createS3Key(file, root);

    logger.info("Publishing " + publishingPath);
    this.objectStoreAccess.put(publishingPath, file.toFile());
  }

  public boolean isFileExisting(Path file, Path root) {
    return this.objectStoreAccess.getFilesWithPrefix(createS3Key(file, root)).hasContents();
  }

  public void deleteFile(Path file, Path root) {
    this.objectStoreAccess.deleteFilesWithPrefix(createS3Key(file, root));
  }

  private String createS3Key(Path file, Path rootFolder) {
    Path relativePath = rootFolder.relativize(file);
    return s3path(relativePath.toString());
  }

  private String s3path(String path) {
    return prefixPath + path;
  }

  public void setPrefixPath(String prefixPath) {
    this.prefixPath = prefixPath;
  }
}
