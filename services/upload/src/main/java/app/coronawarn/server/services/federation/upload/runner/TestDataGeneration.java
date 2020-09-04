package app.coronawarn.server.services.federation.upload.runner;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.repository.TestDataUploadRepository;
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import app.coronawarn.server.services.federation.upload.config.UploadServiceConfig;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
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

  public TestDataGeneration(UploadServiceConfig uploadServiceConfig,
      TestDataUploadRepository keyRepository) {
    this.uploadServiceConfig = uploadServiceConfig;
    this.keyRepository = keyRepository;
  }

  private static byte[] randomByteData() {
    byte[] keydata = new byte[16];
    new Random().nextBytes(keydata);
    return keydata;
  }

  private DiagnosisKey makeKeyFromTimestamp(long timestamp) {
    return DiagnosisKey.builder().withKeyData(randomByteData())
        .withRollingStartIntervalNumber(1)
        .withTransmissionRiskLevel(2)
        .withConsentToFederation(true)
        .withCountryCode("DE")
        .withDaysSinceOnsetOfSymptoms(1)
        .withSubmissionTimestamp(timestamp)
        .withVisitedCountries(List.of("FR", "DK"))
        .withReportType(ReportType.CONFIRMED_TEST)
        .build();
  }

  private List<DiagnosisKey> makeKeysFromTimestamp(long timestamp, int quantity) {
    return IntStream.range(0, quantity)
        .mapToObj(ignoredValue -> makeKeyFromTimestamp(timestamp))
        .collect(Collectors.toList());
  }

  private List<DiagnosisKey> generateFakeKeysForToday() {
    int keysToGenerate = this.uploadServiceConfig.getTestData().getKeys();
    long upperHour = Instant.now(Clock.systemUTC()).truncatedTo(ChronoUnit.HOURS).getEpochSecond()
        / TimeUnit.HOURS.toSeconds(1);
    long lowerHour = upperHour - 24;
    logger.info("Generating {} upload test keys between {} and {}. ({} keys / hour)", keysToGenerate,
        lowerHour,
        upperHour,
        keysToGenerate / 24);
    return LongStream.range(lowerHour, upperHour)
        .mapToObj(i -> this.makeKeysFromTimestamp(i, keysToGenerate / 24))
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
  }

  private void storeUploadKey(DiagnosisKey key) {
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

  public void storeUploadKeys(List<DiagnosisKey> diagnosisKeys) {
    diagnosisKeys.forEach(this::storeUploadKey);
  }

  @Override
  public void run(ApplicationArguments args) throws Exception {
    var fakeKeys = generateFakeKeysForToday();
    logger.info("Storing keys in the DB");
    this.storeUploadKeys(fakeKeys);
    logger.info("Finished Test Data Generation Step");
  }
}
