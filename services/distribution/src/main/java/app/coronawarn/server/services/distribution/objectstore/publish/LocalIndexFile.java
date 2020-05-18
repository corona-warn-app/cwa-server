package app.coronawarn.server.services.distribution.objectstore.publish;

import java.nio.file.Path;

/**
 * Represents a file of a specific category: Index files.
 * <br>
 * Index files contain information about the available packages on the S3, which makes discovery of
 * those files easier for the consumers. Index files are assembled with the name "index", but should
 * be published on S3 w/o the index part, to makee.g.:
 * <br>
 * /diagnosis-keys/date/2020-12-12/index -> /diagnosis-keys/date/2020-12-12
 */
public class LocalIndexFile extends LocalFile {

  /**
   * the suffix for index files.
   */
  private static final String INDEX_FILE_SUFFIX = "/index";

  /**
   * Constructs a new file, which is treated as an index file.
   *
   * @param file the file on the disk
   * @param basePath the base path, from where the file was loaded. This will be used in order to
   *                  determine the S3 key
   */
  public LocalIndexFile(Path file, Path basePath) {
    super(file, basePath);
  }

  @Override
  protected String createS3Key(Path file, Path rootFolder) {
    String s3Key = super.createS3Key(file, rootFolder);

    // Deactivate index mapping for now
    // return s3Key.substring(0, s3Key.length() - INDEX_FILE_SUFFIX.length());
    return s3Key;
  }
}
