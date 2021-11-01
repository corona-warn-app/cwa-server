package app.coronawarn.server.services.distribution.objectstore;

import static app.coronawarn.server.common.shared.util.TimeUtils.getCurrentUtcHour;
import static app.coronawarn.server.common.shared.util.TimeUtils.getUtcDate;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.Api;
import app.coronawarn.server.services.distribution.objectstore.client.ObjectStoreOperationFailedException;
import app.coronawarn.server.services.distribution.objectstore.client.S3Object;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
  public static final String EPOCH_HOUR_REGEX = ".*([0-9]{6,7})";
  public static final String HOUR_PATH_REGEX = ".*(hour).*";
  private final Pattern datePattern = Pattern.compile(DATE_REGEX);
  private final Pattern epochHourPattern = Pattern.compile(EPOCH_HOUR_REGEX);
  private final Pattern hourPathPattern = Pattern.compile(HOUR_PATH_REGEX);

  private static final Logger logger = LoggerFactory
      .getLogger(S3RetentionPolicy.class);

  /**
   * Creates an {@link S3RetentionPolicy} instance with the specified parameters.
   *
   * @param objectStoreAccess         ObjectStoreAccess
   * @param distributionServiceConfig config containing the API, origin Country and origin country
   * @param failedOperationsCounter   FailedObjectStoreOperationsCounter
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
   * Deletes all diagnosis-key day files from S3 that are older than retentionDays.
   *
   * @param retentionDays the number of days, that files should be retained on S3.
   */
  public void applyDiagnosisKeyDayRetentionPolicy(int retentionDays) {
    Set<String> countries = Set.of(originCountry, euPackageName);
    countries.forEach(country -> {
      List<S3Object> diagnosisKeysObjects = objectStoreAccess.getObjectsWithPrefix(getDiagnosisKeyPrefix(country));
      final LocalDate cutOffDate = getUtcDate().minusDays(retentionDays);
      diagnosisKeysObjects.stream()
          .filter(diagnosisKeysObject -> isDiagnosisKeyFilePathOlderThan(diagnosisKeysObject, cutOffDate))
          .forEach(this::deleteS3Object);
    });
  }

  /**
   * Deletes all diagnosis-key hour files from S3 that are older than retentionDays.
   *
   * @param retentionDays number of days back where hourly files should be deleted
   */
  public void applyDiagnosisKeyHourRetentionPolicy(long retentionDays) {
    Set<String> countries = Set.of(originCountry, euPackageName);
    countries.forEach(country -> {
      List<S3Object> diagnosisKeysObjects = objectStoreAccess.getObjectsWithPrefix(getDiagnosisKeyPrefix(country));
      final LocalDate cutOffDate = getUtcDate().minusDays(retentionDays - 1L);
      var deletableKeys = diagnosisKeysObjects.stream()
          .filter(diagnosisKeysObject -> isDiagnosisKeyFilePathOlderThan(diagnosisKeysObject, cutOffDate))
          .filter(this::isDiagnosisKeyFilePathOnHourFolder)
          .collect(Collectors.toList());

      logger.info("Deleting {} diagnosis key files from hourly folders older than {}", deletableKeys.size(),
          cutOffDate);
      deletableKeys.forEach(this::deleteS3Object);
    });
  }

  /**
   * Deletes all trace time warning hour files from S3 that are older than retentionDays.
   *
   * @param retentionDays number of days back where hourly files should be deleted
   */
  public void applyTraceTimeWarningHourRetentionPolicy(long retentionDays) {
    Set<String> countries = Set.of(originCountry, euPackageName);
    countries.forEach(country -> {
      List<S3Object> traceTimeWarningsObjects = objectStoreAccess
          .getObjectsWithPrefix(getTraceTimeWarningPrefix(country));
      final LocalDateTime cutOffTime = getCurrentUtcHour().minusDays(retentionDays);
      final LocalDate cutOffDate = getUtcDate().minusDays(retentionDays - 1L);
      var deletableTraceTimeWarnings = traceTimeWarningsObjects.stream()
          .filter(traceTimeWarningsObject -> isTraceTimeWarningFilePathOlderThan(traceTimeWarningsObject, cutOffTime))
          .collect(Collectors.toList());

      logger.info("Deleting {} trace time warning files older than {}", deletableTraceTimeWarnings.size(), cutOffDate);
      deletableTraceTimeWarnings.forEach(this::deleteS3Object);
    });
  }

  /**
   * Java stream do not support checked exceptions within streams. This helper method rethrows them as unchecked
   * expressions, so they can be passed up to the Retention Policy.
   *
   * @param s3Object the S3 object, that should be deleted.
   */
  public void deleteS3Object(S3Object s3Object) {
    try {
      objectStoreAccess.deleteObjectsWithPrefix(s3Object.getObjectName());
    } catch (ObjectStoreOperationFailedException e) {
      failedObjectStoreOperationsCounter.incrementAndCheckThreshold(e);
    }
  }


  private boolean isDiagnosisKeyFilePathOnHourFolder(S3Object s3Object) {
    Matcher matcher = hourPathPattern.matcher(s3Object.getObjectName());
    return matcher.matches();
  }

  private boolean isDiagnosisKeyFilePathOlderThan(S3Object s3Object, LocalDate cutOffDate) {
    Matcher matcher = datePattern.matcher(s3Object.getObjectName());
    return matcher.matches() && LocalDate.parse(matcher.group(1), DateTimeFormatter.ISO_LOCAL_DATE)
        .isBefore(cutOffDate);
  }

  private boolean isTraceTimeWarningFilePathOlderThan(S3Object s3Object, LocalDateTime cutOffTime) {
    return Stream.of(s3Object)
        .map(S3Object::getObjectName)
        .map(epochHourPattern::matcher)
        .filter(Matcher::matches)
        .map(match -> match.group(1))
        .map(Integer::parseInt)
        .map(TimeUnit.HOURS::toSeconds)
        .map(epochSeconds -> LocalDateTime.ofEpochSecond(epochSeconds, 0, ZoneOffset.UTC))
        .map(packageHour -> packageHour.isBefore(cutOffTime))
        .findFirst()
        .orElse(false);
  }

  private String getDiagnosisKeyPrefix(String country) {
    return api.getVersionPath() + "/" + api.getVersionV1() + "/" + api.getDiagnosisKeysPath() + "/"
        + api.getCountryPath() + "/" + country + "/" + api.getDatePath() + "/";
  }

  private String getTraceTimeWarningPrefix(String country) {
    return api.getVersionPath() + "/" + api.getVersionV1() + "/" + api.getTraceWarningsPath() + "/"
        + api.getCountryPath() + "/" + country + "/" + api.getHourPath() + "/";
  }
}
