/*-
 * ---license-start
 * Corona-Warn-App
 * ---
 * Copyright (C) 2020 SAP SE and all other contributors
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ---license-end
 */

package app.coronawarn.server.services.federation.upload.runner;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.domain.FederationUploadKey;
import app.coronawarn.server.common.persistence.service.FederationUploadKeyService;
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import app.coronawarn.server.common.protocols.internal.RiskLevel;
import app.coronawarn.server.services.federation.upload.config.UploadServiceConfig;
import app.coronawarn.server.services.federation.upload.testdata.TestDataUploadRepository;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
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

  public static final long ONE_HOUR_INTERVAL_SECONDS = TimeUnit.HOURS.toSeconds(1);
  public static final long TEN_MINUTES_INTERVAL_SECONDS = TimeUnit.MINUTES.toSeconds(10);

  public TestDataGeneration(UploadServiceConfig uploadServiceConfig,
      TestDataUploadRepository keyRepository) {
    this.uploadServiceConfig = uploadServiceConfig;
    this.keyRepository = keyRepository;
  }

  private static byte[] randomByteData() {
    byte[] keydata = new byte[16];
    new SecureRandom().nextBytes(keydata);
    return keydata;
  }

  private FederationUploadKey makeKeyFromTimestamp(long timestamp) {
    return FederationUploadKey.from(DiagnosisKey.builder().withKeyData(randomByteData())
        .withRollingStartIntervalNumber(generateRollingStartIntervalNumber(timestamp))
        .withTransmissionRiskLevel(generateTransmissionRiskLevel())
        .withConsentToFederation(true)
        .withCountryCode("DE")
        .withDaysSinceOnsetOfSymptoms(1)
        .withSubmissionTimestamp(timestamp)
        .withVisitedCountries(List.of("FR", "DK"))
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
            - TimeUnit.DAYS.toSeconds(14) / TEN_MINUTES_INTERVAL_SECONDS;
    return Math.toIntExact(getRandomBetween(minRollingStartIntervalNumber, maxRollingStartIntervalNumber));
  }

  private long getRandomBetween(long minIncluding, long maxIncluding) {
    return minIncluding + (long) (ThreadLocalRandom.current().nextDouble() * (maxIncluding - minIncluding));
  }

  private List<FederationUploadKey> makeKeysFromTimestamp(long timestamp, int quantity) {
    return IntStream.range(0, quantity)
        .mapToObj(ignoredValue -> makeKeyFromTimestamp(timestamp))
        .collect(Collectors.toList());
  }

  private LocalDateTime getCurrentTimestampTruncatedHour() {
    return LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC).truncatedTo(ChronoUnit.HOURS);
  }

  private long secondsToHours(long timestampInSeconds) {
    return timestampInSeconds / ONE_HOUR_INTERVAL_SECONDS;
  }

  /**
   * Creates a list of Fake Upload keys for the day before.
   * If there are already upload keys in the table, generate keys only between latest submission timestamp and
   * yesterday.
   * -> No keys in the DB: Generates keys from Now-49h until Now-25h
   * -> Keys in the DB: Generates keys from Last Timestamp until Now-25h
   * Keys need to be created with 1 full day + 2 hour offset to make sure that they are expired.
   * For each hour free N Diagnosis Keys will be created.
   * Where N is defined by property services.upload.test-data.keys-per-hour
   * @return List of Federation Upload Keys generated
   */
  private List<FederationUploadKey> generateFakeKeysForYesterday() {
    long latestStartTimestamp = keyRepository.getMaxSubmissionTimestamp().orElse(0L) + 1;
    int keysToGeneratePerHour = this.uploadServiceConfig.getTestData().getKeysPerHour();

    LocalDateTime upperHour = getCurrentTimestampTruncatedHour()
        .minusDays(1L)
        .minusHours(2L);
    LocalDateTime lowerHour = upperHour
        .minusDays(1L);

    long hourStart = secondsToHours(lowerHour.toEpochSecond(ZoneOffset.UTC));
    if (hourStart < latestStartTimestamp) {
      hourStart = latestStartTimestamp;
    }
    long hourEnd = secondsToHours(upperHour.toEpochSecond(ZoneOffset.UTC));

    long keysToGenerate = keysToGeneratePerHour * (hourEnd - hourStart);
    if (keysToGenerate > 0) {
      logger.info("Generating {} upload keys between hours {} and {}",
          keysToGenerate,
          hourStart,
          hourEnd);
      return LongStream.range(hourStart, hourEnd)
          .mapToObj(i -> this.makeKeysFromTimestamp(i, keysToGeneratePerHour))
          .flatMap(Collection::stream)
          .collect(Collectors.toList());
    } else {
      logger.info("Keys for earliest distributable hour already generated, skipping generation");
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
    var fakeKeys = generateFakeKeysForYesterday();
    logger.info("Storing keys in the DB");
    this.storeUploadKeys(fakeKeys);
    logger.info("Finished Test Data Generation Step");
  }
}
