package app.coronawarn.server.services.distribution.objectstore.publish;

import static app.coronawarn.server.services.distribution.assembly.component.DigitalCertificatesStructureProvider.ACCEPTANCE_RULES;
import static app.coronawarn.server.services.distribution.assembly.component.DigitalCertificatesStructureProvider.DIGITAL_CERTIFICATES_STRUCTURE_PROVIDER;
import static app.coronawarn.server.services.distribution.assembly.component.DigitalCertificatesStructureProvider.INVALIDATION_RULES;
import static app.coronawarn.server.services.distribution.assembly.component.DigitalCertificatesStructureProvider.ONBOARDED_COUNTRIES;

import app.coronawarn.server.services.distribution.assembly.structure.file.FileOnDiskWithChecksum;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a file, which is subject for publishing to S3.
 */
public abstract class LocalFile {

  private static final Logger logger = LoggerFactory.getLogger(LocalFile.class);

  /**
   * the path to the file to be represented.
   */
  private final Path file;

  /**
   * the assigned S3 key.
   */
  private final String s3Key;

  /**
   * the checksum of this file.
   */
  private String checksum = "";

  /**
   * Constructs a new file representing a file on the disk.
   *
   * @param file     the path to the file to be represented
   * @param basePath the base path
   */
  protected LocalFile(Path file, Path basePath) {
    this.file = file;
    this.s3Key = createS3Key(file, basePath);
    this.checksum = loadChecksum();
  }

  public String getS3Key() {
    return s3Key;
  }

  public String getChecksum() {
    return checksum;
  }

  public Path getFile() {
    return file;
  }

  private String loadChecksum() {
    try {
      return Files.readString(FileOnDiskWithChecksum.buildChecksumPathForFile(file)).trim();
    } catch (IOException e) {
      logger.debug("Unable to load checksum file.");
      return "";
    }
  }

  protected String createS3Key(Path file, Path rootFolder) {
    Path relativePath = rootFolder.relativize(file);
    return relativePath.toString().replace("\\\\", "/");
  }

  /**
   * Value for the <code>content-type</code> header.
   *
   * @return Either <a href="https://www.iana.org/assignments/media-types/application/zip">zip</a> or
   *         <a href="https://www.iana.org/assignments/media-types/application/json">json</a>.
   */
  public String getContentType() {
    if (isConfigFile() || isStatisticFile() || isKeyFile() || isQrPosterTemplate() || isVaccineValueSet()
        || isDscFile()) {
      return "application/zip";
    }
    // list of versions, dates, hours
    return "application/json";
  }

  /**
   * Indicates if a local file is a Key-file or not. Only the Key files are stored in the Date / Hour tree structure.
   * One file per sub-folder (days: 1-31 / hours: 0-23). The index files are not stored in folders ending with a digit.
   *
   * @return <code>true</code> if and only if the {@link #s3Key} ends with a digit, false otherwise.
   */
  public boolean isKeyFile() {
    return s3Key.matches(".*\\d") && !isStatisticFile();
  }

  public boolean isQrPosterTemplate() {
    return s3Key.contains("qr_code_poster_template_");
  }

  private boolean isConfigFile() {
    return s3Key.endsWith("app_config") || s3Key.endsWith("app_config_ios") || s3Key.endsWith("app_config_android");
  }

  private boolean isStatisticFile() {
    return s3Key.endsWith("stats") || s3Key.matches(".*local_stats_\\d");
  }

  private boolean isVaccineValueSet() {
    return s3Key.endsWith("value-sets");
  }

  private boolean isDscFile() {
    return s3Key.endsWith(INVALIDATION_RULES) || s3Key.endsWith(ACCEPTANCE_RULES) || s3Key.endsWith(ONBOARDED_COUNTRIES)
        || s3Key.endsWith(DIGITAL_CERTIFICATES_STRUCTURE_PROVIDER);
  }
}
