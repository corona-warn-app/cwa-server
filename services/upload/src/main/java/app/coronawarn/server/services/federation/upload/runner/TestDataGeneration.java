package app.coronawarn.server.services.federation.upload.runner;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.domain.FederationUploadKey;
import app.coronawarn.server.common.persistence.service.common.CommonDataGeneration;
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import app.coronawarn.server.common.shared.util.HashUtils;
import app.coronawarn.server.services.federation.upload.config.UploadServiceConfig;
import app.coronawarn.server.services.federation.upload.testdata.TestDataUploadRepository;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(-1)
@Profile("testdata")
public class TestDataGeneration extends CommonDataGeneration<FederationUploadKey> {

  private final Logger logger = LoggerFactory.getLogger(TestDataGeneration.class);
  private final TestDataUploadRepository keyRepository;
  private final Integer maxPendingKeys;

  TestDataGeneration(UploadServiceConfig uploadServiceConfig,
      TestDataUploadRepository keyRepository) {
    super(uploadServiceConfig.getRetentionDays());
    this.keyRepository = keyRepository;
    this.maxPendingKeys = uploadServiceConfig.getTestData().getMaxPendingKeys();
  }

  @Override
  public void run(ApplicationArguments args) {
    var fakeKeys = generateFakeKeysForPreviousDay();
    logger.info("Storing keys in the DB");
    this.storeUploadKeys(fakeKeys);
    logger.info("Finished Test Data Generation Step");
  }

  private void storeUploadKeys(List<FederationUploadKey> diagnosisKeys) {
    diagnosisKeys.forEach(this::storeUploadKey);
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
        key.isConsentToFederation(),
        key.getBatchTag(),
        key.getSubmissionType());
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
        .minusDays(retentionDays)
        .toEpochSecond(ZoneOffset.UTC) / 600L;
    logger.info("Deleting test keys with rolling_start_interval_number less than {}", timestamp);
    keyRepository.applyRetentionToTestKeys((int) timestamp);
    int pendingKeys = keyRepository.countPendingKeys();
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
          .mapToObj(ignoredValue -> generateDiagnosisKey(getRandomBetween(hourStart, hourEnd), "DE"))
          .collect(Collectors.toList());
    } else {
      logger.info("Skipping generation");
      return Collections.emptyList();
    }
  }

  private LocalDateTime getCurrentTimestampTruncatedHour() {
    return LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC).truncatedTo(ChronoUnit.HOURS);
  }

  @Override
  protected FederationUploadKey generateDiagnosisKey(long submissionTimestamp, String country) {
    return FederationUploadKey.from(DiagnosisKey.builder()
        .withKeyDataAndSubmissionType(HashUtils.generateSecureRandomByteArrayData(16), generateSubmissionType())
        .withRollingStartIntervalNumber(generateRollingStartIntervalNumber(submissionTimestamp))
        .withTransmissionRiskLevel(generateTransmissionRiskLevel())
        .withConsentToFederation(true)
        .withCountryCode(country)
        .withDaysSinceOnsetOfSymptoms(1)
        .withSubmissionTimestamp(submissionTimestamp)
        .withVisitedCountries(Set.of("FR", "DK"))
        .withReportType(ReportType.CONFIRMED_TEST)
        .build());
  }
}
