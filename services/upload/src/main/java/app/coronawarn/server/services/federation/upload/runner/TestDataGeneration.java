

package app.coronawarn.server.services.federation.upload.runner;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.domain.FederationUploadKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import app.coronawarn.server.common.protocols.internal.RiskLevel;
import app.coronawarn.server.services.federation.upload.config.UploadServiceConfig;
import app.coronawarn.server.services.federation.upload.testdata.TestDataUploadRepository;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(-1)
@Profile("testdata")
public class TestDataGeneration implements ApplicationRunner {

  private final UploadServiceConfig uploadServiceConfig;
  private final Logger logger = LoggerFactory.getLogger(TestDataGeneration.class);
  private final TestDataUploadRepository keyRepository;
  private final SecureRandom random = new SecureRandom();

  public static final long ONE_HOUR_INTERVAL_SECONDS = TimeUnit.HOURS.toSeconds(1);
  public static final long TEN_MINUTES_INTERVAL_SECONDS = TimeUnit.MINUTES.toSeconds(10);

  public TestDataGeneration(UploadServiceConfig uploadServiceConfig,
      TestDataUploadRepository keyRepository) {
    this.uploadServiceConfig = uploadServiceConfig;
    this.keyRepository = keyRepository;
  }

  private static byte[] randomByteData() {
    byte[] keyData = new byte[16];
    new SecureRandom().nextBytes(keyData);
    return keyData;
  }

  private FederationUploadKey makeKeyFromTimestamp(long timestamp) {
    return FederationUploadKey.from(DiagnosisKey.builder().withKeyData(randomByteData())
        .withRollingStartIntervalNumber(generateRollingStartIntervalNumber(timestamp))
        .withTransmissionRiskLevel(generateTransmissionRiskLevel())
        .withConsentToFederation(true)
        .withCountryCode("DE")
        .withDaysSinceOnsetOfSymptoms(1)
        .withSubmissionTimestamp(timestamp)
        .withVisitedCountries(Set.of("FR", "DK"))
        .withReportType(ReportType.CONFIRMED_TEST)
        .build());
  }

  private int generateTransmissionRiskLevel() {
    return Math.toIntExact(
        getRandomBetween(RiskLevel.RISK_LEVEL_LOWEST_VALUE, RiskLevel.RISK_LEVEL_HIGHEST_VALUE));
  }

  private int generateRollingStartIntervalNumber(long submissionTimestamp) {
    long maxRollingStartIntervalNumber =
        submissionTimestamp * ONE_HOUR_INTERVAL_SECONDS / TEN_MINUTES_INTERVAL_SECONDS;
    long minRollingStartIntervalNumber =
        maxRollingStartIntervalNumber
            - TimeUnit.DAYS.toSeconds(11) / TEN_MINUTES_INTERVAL_SECONDS;
    return Math.toIntExact(getRandomBetween(minRollingStartIntervalNumber, maxRollingStartIntervalNumber));
  }

  private long getRandomBetween(long minIncluding, long maxIncluding) {
    return minIncluding + (long) (random.nextDouble() * (maxIncluding - minIncluding));
  }

  private LocalDateTime getCurrentTimestampTruncatedHour() {
    return LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC).truncatedTo(ChronoUnit.HOURS);
  }

  private long secondsToHours(long timestampInSeconds) {
    return timestampInSeconds / ONE_HOUR_INTERVAL_SECONDS;
  }

  /**
   * Creates a list of Fake Upload keys for the day before. Number of keys generated is defined by the following
   * formula:
   * <i>upload.test-data.max-pending-keys</i> - <i>number pending keys in DB</i>
   * Where <i>pending key in DB</i> is any key where <i>batch_tag</i> is NULL.
   *
   * @return List of Federation Upload Keys generated.
   */
  private List<FederationUploadKey> generateFakeKeysForPreviousDay() {
    long timestamp = getCurrentTimestampTruncatedHour()
        .minusDays(this.uploadServiceConfig.getRetentionDays())
        .toEpochSecond(ZoneOffset.UTC) / 600L;
    logger.info("Deleting test keys with rolling_start_interval_number less than {}", timestamp);
    keyRepository.applyRetentionToTestKeys((int)timestamp);
    int pendingKeys = keyRepository.countPendingKeys();
    int maxPendingKeys = this.uploadServiceConfig.getTestData().getMaxPendingKeys();
    logger.info("Found {} pending upload keys on DB", pendingKeys);
    int numberOfKeysToGenerate = maxPendingKeys - pendingKeys;

    if (numberOfKeysToGenerate > 0) {
      LocalDateTime upperHour = getCurrentTimestampTruncatedHour()
          .minusDays(1L)
          .minusHours(2L);
      LocalDateTime lowerHour = upperHour
          .minusDays(1L);

      long hourStart = secondsToHours(lowerHour.toEpochSecond(ZoneOffset.UTC));
      long hourEnd = secondsToHours(upperHour.toEpochSecond(ZoneOffset.UTC));

      logger.info("Generating {} fake upload keys between times {} and {}",
          numberOfKeysToGenerate,
          hourStart,
          hourEnd);
      return IntStream.range(0, numberOfKeysToGenerate)
          .mapToObj(ignoredValue -> this.makeKeyFromTimestamp(this.getRandomBetween(hourStart, hourEnd)))
          .collect(Collectors.toList());
    } else {
      logger.info("Skipping generation");
      return Collections.emptyList();
    }
  }

  private void storeUploadKey(FederationUploadKey key) {
    keyRepository.storeUploadKey(key.getKeyData(),
        key.getRollingStartIntervalNumber(),
        key.getRollingPeriod(),
        key.getSubmissionTimestamp(),
        key.getTransmissionRiskLevel(),
        key.getOriginCountry(),
        key.getVisitedCountries().toArray(new String[0]),
        key.getReportType().name(),
        key.getDaysSinceOnsetOfSymptoms(),
        key.isConsentToFederation());
  }

  public void storeUploadKeys(List<FederationUploadKey> diagnosisKeys) {
    diagnosisKeys.forEach(this::storeUploadKey);
  }

  @Override
  public void run(ApplicationArguments args) {
    var fakeKeys = generateFakeKeysForPreviousDay();
    logger.info("Storing keys in the DB");
    this.storeUploadKeys(fakeKeys);
    logger.info("Finished Test Data Generation Step");
  }
}
