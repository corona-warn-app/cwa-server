package app.coronawarn.server.services.distribution.objectstore.publish;

import java.nio.file.Path;

/**
 * Represents a file of a specific category: Index files.
 * <br>
 * Index files contain information about the available packages on the S3, which makes discovery
 * of those files easier for the consumers. Index files are assembled with the name "index", but
 * should be published on S3 w/o the index part, to makee.g.:
 * <br>
 * /diagnosis-keys/date/2020-12-12/index -> /diagnosis-keys/date/2020-12-12
 */
public class PublishIndexFile extends PublishFile {

  private static final String INDEX_FILE_SUFFIX = "/index";

  public PublishIndexFile(Path file, Path basePath) {
    super(file, basePath);
  }

  @Override
  protected String createS3Key(Path file, Path rootFolder) {
    String s3Key = super.createS3Key(file, rootFolder);

    return s3Key.substring(0, s3Key.length() - INDEX_FILE_SUFFIX.length());
  }
}
