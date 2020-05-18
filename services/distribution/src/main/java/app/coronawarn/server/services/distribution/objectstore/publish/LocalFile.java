package app.coronawarn.server.services.distribution.objectstore.publish;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Represents a file, which is subject for publishing to S3.
 */
public abstract class LocalFile {

  /** the path to the file to be represented. */
  private final Path file;

  /** the assigned S3 key. */
  private final String s3Key;

  /** the hash of this file. */
  private final String hash;

  /**
   * Constructs a new file representing a file on the disk.
   *
   * @param file the path to the file to be represented
   * @param basePath the base path
   */
  public LocalFile(Path file, Path basePath) {
    this.file = file;
    this.hash = hash();
    this.s3Key = createS3Key(file, basePath);
  }

  public String getS3Key() {
    return s3Key;
  }

  public String getHash() {
    return hash;
  }

  public Path getFile() {
    return file;
  }

  private String hash() {
    try {
      MessageDigest digester = MessageDigest.getInstance("SHA-256");
      digester.update(Files.readAllBytes(file));

      return Base64.getEncoder().encodeToString(digester.digest());
    } catch (IOException | NoSuchAlgorithmException e) {
      throw new RuntimeException("Unable to compute hashes due to ", e);
    }
  }

  protected String createS3Key(Path file, Path rootFolder) {
    Path relativePath = rootFolder.relativize(file);
    return relativePath.toString().replaceAll("\\\\", "/");
  }
}
