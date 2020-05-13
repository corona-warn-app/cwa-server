package app.coronawarn.server.services.distribution.objectstore;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This publisher is the interface to the S3, translating a local file structure to the target S3
 * objects.
 */
@Component
public class S3Publisher {

  private Logger logger = LoggerFactory.getLogger(this.getClass());

  /**
   * prefix path on S3, enforced for all methods on this class.
   */
  private String prefixPath = "cwa/";

  private ObjectStoreAccess objectStoreAccess;

  @Autowired
  public S3Publisher(ObjectStoreAccess objectStoreAccess) {
    this.objectStoreAccess = objectStoreAccess;
  }

  /**
   * Publishes a local folder to S3. All files in the target folder will be copied over to S3,
   * keeping the file structure. This operation is running also through all subfolders
   * (recursively).
   *
   * @param path the folder on the local disk
   * @throws IOException in case there was a problem reading the folder/contents
   */
  public void publishFolder(Path path) throws IOException {
    if (!path.toFile().isDirectory()) {
      throw new UnsupportedOperationException("Supplied path is not a folder: " + path);
    }

    try (Stream<Path> stream = Files.walk(path, Integer.MAX_VALUE)) {
      stream.filter(Files::isRegularFile).forEach(file -> publishFile(file, path));
    }
  }

  /**
   * Deletes a folder on S3.
   *
   * @param path the folder to delete
   */
  public void deleteFolder(String path) {
    objectStoreAccess.deleteObjectsWithPrefix(prefixPath + path);
  }

  /**
   * Publishes the given file.
   *
   * @param file the file to publish
   * @param root the root, needed to compute the relative path for the S3 location
   */
  public void publishFile(Path file, Path root) {
    if (file.toFile().isFile()) {
      throw new UnsupportedOperationException("Supplied path is not a file: " + file);
    }
    String publishingPath = createS3Key(file, root);

    logger.info("Publishing " + publishingPath);
    this.objectStoreAccess.putObject(publishingPath, file.toFile());
  }

  /**
   * Checks whether the given file exists.
   * <br>
   * Both parameters are local on disk, and the S3 path is derived relatively by the root & file.
   * E.g.:<br> file: /folder1/folder2/file<br> root: /folder1/<br>
   * <br>
   * The result S3 location will be /folder2/file, which will be checked for existence.
   *
   * @param file the file to check
   * @param root the root, needed to compute the relative path for the S3 location
   * @return true, if it exists, false otherwise
   */
  public boolean isFileExisting(Path file, Path root) {
    return this.objectStoreAccess.getObjectsWithPrefix(createS3Key(file, root)).hasContents();
  }

  /**
   * Deletes the given file on S3.
   * <br>
   * Both parameters are local on disk, and the S3 path is derived relatively by the root & file
   * E.g.:<br> file: /folder1/folder2/file<br> root: /folder1/<br>
   * <br>
   * The result S3 location will be /folder2/file, which will be deleted.
   *
   * @param file the file to delete
   * @param root the root, needed to compute the relative path for the S3 location
   */
  public void deleteFile(Path file, Path root) {
    this.objectStoreAccess.deleteObjectsWithPrefix(createS3Key(file, root));
  }

  private String createS3Key(Path file, Path rootFolder) {
    Path relativePath = rootFolder.relativize(file);
    return s3path(relativePath.toString());
  }

  private String s3path(String path) {
    return prefixPath + path;
  }

  /**
   * Changes the prefix path for all S3Publisher operations. The default root.
   *
   * @param prefixPath the new prefix path
   */
  public void setPrefixPath(String prefixPath) {
    this.prefixPath = prefixPath;
  }
}
