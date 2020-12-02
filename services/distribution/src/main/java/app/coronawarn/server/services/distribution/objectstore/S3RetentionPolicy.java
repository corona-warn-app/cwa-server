package app.coronawarn.server.services.distribution.objectstore;

import app.coronawarn.server.services.distribution.assembly.structure.util.TimeUtils;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.Api;
import app.coronawarn.server.services.distribution.objectstore.client.ObjectStoreOperationFailedException;
import app.coronawarn.server.services.distribution.objectstore.client.S3Object;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Creates an S3RetentionPolicy object, which applies the retention policy to the S3 compatible storage.
 */
@Component
public class S3RetentionPolicy {

  private final ObjectStoreAccess objectStoreAccess;
  private final Api api;
  private final FailedObjectStoreOperationsCounter failedObjectStoreOperationsCounter;
  private final String originCountry;
  private final String euPackageName;

  public static final String DATE_REGEX = ".*([0-9]{4}-[0-9]{2}-[0-9]{2}).*";
  public static final String HOUR_REGEX = ".*(hour).*";
  private final Pattern datePattern = Pattern.compile(DATE_REGEX);
  private final Pattern hourPattern = Pattern.compile(HOUR_REGEX);

  private static final Logger logger = LoggerFactory
      .getLogger(S3RetentionPolicy.class);

  /**
   * Creates an {@link S3RetentionPolicy} instance with the specified parameters.
   */
  public S3RetentionPolicy(ObjectStoreAccess objectStoreAccess, DistributionServiceConfig distributionServiceConfig,
      FailedObjectStoreOperationsCounter failedOperationsCounter) {
    this.objectStoreAccess = objectStoreAccess;
    this.api = distributionServiceConfig.getApi();
    this.failedObjectStoreOperationsCounter = failedOperationsCounter;
    this.originCountry = distributionServiceConfig.getApi().getOriginCountry();
    this.euPackageName = distributionServiceConfig.getEuPackageName();
  }

  /**
   * Deletes all diagnosis-key files from S3 that are older than retentionDays.
   *
   * @param retentionDays the number of days, that files should be retained on S3.
   */
  public void applyRetentionPolicy(int retentionDays) {
    Set<String> countries = Set.of(originCountry, euPackageName);
    countries.forEach(country -> {
      List<S3Object> diagnosisKeysObjects = objectStoreAccess.getObjectsWithPrefix(getPrefix(country));
      final LocalDate cutOffDate = TimeUtils.getUtcDate().minusDays(retentionDays);
      diagnosisKeysObjects.stream()
          .filter(diagnosisKeysObject -> isFilePathOlderThan(diagnosisKeysObject, cutOffDate))
          .forEach(this::deleteDiagnosisKey);
    });
  }

  /**
   * Delete old hourly files based on retention policy.
   *
   * @param retentionDays number of days back where hourly files should be deleted
   */
  public void applyHourFileRetentionPolicy(long retentionDays) {
    Set<String> countries = Set.of(originCountry, euPackageName);
    countries.forEach(country -> {
      List<S3Object> diagnosisKeysObjects = objectStoreAccess.getObjectsWithPrefix(getPrefix(country));
      final LocalDate cutOffDate = TimeUtils.getUtcDate().minusDays(retentionDays - 1L);
      var deletableKeys = diagnosisKeysObjects.stream()
          .filter(diagnosisKeysObject -> isFilePathOlderThan(diagnosisKeysObject, cutOffDate))
          .filter(this::isFilePathOnHourFolder)
          .collect(Collectors.toList());

      logger.info("Deleting {} files from hourly folders older than {}", deletableKeys.size(), cutOffDate.toString());
      deletableKeys.forEach(this::deleteDiagnosisKey);
    });
  }

  /**
   * Java stream do not support checked exceptions within streams. This helper method rethrows them as unchecked
   * expressions, so they can be passed up to the Retention Policy.
   *
   * @param diagnosisKey the  diagnosis key, that should be deleted.
   */
  public void deleteDiagnosisKey(S3Object diagnosisKey) {
    try {
      objectStoreAccess.deleteObjectsWithPrefix(diagnosisKey.getObjectName());
    } catch (ObjectStoreOperationFailedException e) {
      failedObjectStoreOperationsCounter.incrementAndCheckThreshold(e);
    }
  }


  private boolean isFilePathOnHourFolder(S3Object diagnosisKeysObject) {
    Matcher matcher = hourPattern.matcher(diagnosisKeysObject.getObjectName());
    return matcher.matches();
  }

  private boolean isFilePathOlderThan(S3Object diagnosisKeysObject, LocalDate cutOffDate) {
    Matcher matcher = datePattern.matcher(diagnosisKeysObject.getObjectName());
    return matcher.matches() && LocalDate.parse(matcher.group(1), DateTimeFormatter.ISO_LOCAL_DATE)
        .isBefore(cutOffDate);
  }

  private String getPrefix(String country) {
    return api.getVersionPath() + "/" + api.getVersionV1() + "/" + api.getDiagnosisKeysPath() + "/"
        + api.getCountryPath() + "/" + country + "/" + api.getDatePath() + "/";
  }
}
