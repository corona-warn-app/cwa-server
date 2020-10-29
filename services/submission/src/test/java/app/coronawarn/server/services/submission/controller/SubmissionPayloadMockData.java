package app.coronawarn.server.services.submission.controller;

import static app.coronawarn.server.common.protocols.external.exposurenotification.ReportType.CONFIRMED_TEST;
import static java.time.ZoneOffset.UTC;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import app.coronawarn.server.common.protocols.external.exposurenotification.TemporaryExposureKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.TemporaryExposureKey.Builder;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload;
import app.coronawarn.server.services.submission.config.SubmissionServiceConfig;
import com.google.protobuf.ByteString;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class SubmissionPayloadMockData {

  public static final String VALID_KEY_DATA_1 = "testKey111111111";
  public static final String VALID_KEY_DATA_2 = "testKey222222222";
  public static final String VALID_KEY_DATA_3 = "testKey333333333";

  public static SubmissionPayload buildPayload(TemporaryExposureKey key) {
    Collection<TemporaryExposureKey> keys = Stream.of(key).collect(Collectors.toCollection(ArrayList::new));
    return buildPayload(keys);
  }

  public static SubmissionPayload buildPayload(Collection<TemporaryExposureKey> keys) {
    return SubmissionPayload.newBuilder()
        .addAllKeys(keys)
        .addAllVisitedCountries(List.of("DE", "FR"))
        .setOrigin("DE")
        .build();
  }

  public static SubmissionPayload buildPayload(Collection<TemporaryExposureKey> keys, boolean consentToFederation) {
    return SubmissionPayload.newBuilder()
        .addAllKeys(keys)
        .addAllVisitedCountries(List.of("DE", "FR"))
        .setOrigin("DE")
        .setConsentToFederation(consentToFederation)
        .build();
  }

  public static SubmissionPayload buildPayloadForOriginCountry(Collection<TemporaryExposureKey> keys,
      String originCountry) {
    return SubmissionPayload.newBuilder()
        .addAllKeys(keys)
        .addAllVisitedCountries(List.of("DE", "FR"))
        .setOrigin(originCountry)
        .build();
  }

  public static SubmissionPayload buildPayloadWithoutOriginCountry(Collection<TemporaryExposureKey> keys) {
    return SubmissionPayload.newBuilder()
        .addAllKeys(keys)
        .addAllVisitedCountries(List.of("DE", "FR"))
        .clearOrigin()
        .build();
  }

  public static SubmissionPayload buildPayloadWithPadding(Collection<TemporaryExposureKey> keys) {
    return buildPayloadWithPadding(keys, "PaddingString".getBytes());
  }

  public static SubmissionPayload buildPayloadWithTooLargePadding(SubmissionServiceConfig config,
      Collection<TemporaryExposureKey> keys) {
    int exceedingSize = (int) (2 * config.getMaximumRequestSize().toBytes());
    byte[] bytes = new byte[exceedingSize];
    return buildPayloadWithPadding(keys, bytes);
  }

  public static Collection<TemporaryExposureKey> buildPayloadWithOneKey() {
    return Collections.singleton(buildTemporaryExposureKey(VALID_KEY_DATA_1, createRollingStartIntervalNumber(1), 3,
        ReportType.CONFIRMED_TEST, 1));
  }

  private static SubmissionPayload buildPayloadWithPadding(Collection<TemporaryExposureKey> keys, byte[] bytes) {
    return SubmissionPayload.newBuilder()
        .addAllKeys(keys)
        .addAllVisitedCountries(List.of("DE", "FR"))
        .setOrigin("DE")
        .setRequestPadding(ByteString.copyFrom(bytes))
        .build();
  }

  public static Collection<TemporaryExposureKey> buildMultipleKeys(SubmissionServiceConfig config) {
    int rollingStartIntervalNumber1 = createRollingStartIntervalNumber(config.getRetentionDays() - 1);
    int rollingStartIntervalNumber2 = rollingStartIntervalNumber1 + DiagnosisKey.MAX_ROLLING_PERIOD;
    int rollingStartIntervalNumber3 = rollingStartIntervalNumber2 + DiagnosisKey.MAX_ROLLING_PERIOD;
    return Stream.of(
        buildTemporaryExposureKey(VALID_KEY_DATA_1, rollingStartIntervalNumber1, 3, CONFIRMED_TEST, 1),
        buildTemporaryExposureKey(VALID_KEY_DATA_2, rollingStartIntervalNumber3, 6, CONFIRMED_TEST, 1),
        buildTemporaryExposureKey(VALID_KEY_DATA_3, rollingStartIntervalNumber2, 8, CONFIRMED_TEST, 1))
        .collect(Collectors.toCollection(ArrayList::new));
  }

  public static Collection<TemporaryExposureKey> buildMultipleKeysWithoutDSOS(SubmissionServiceConfig config) {
    int rollingStartIntervalNumber1 = createRollingStartIntervalNumber(config.getRetentionDays() - 1);
    int rollingStartIntervalNumber2 = rollingStartIntervalNumber1 + DiagnosisKey.MAX_ROLLING_PERIOD;
    int rollingStartIntervalNumber3 = rollingStartIntervalNumber2 + DiagnosisKey.MAX_ROLLING_PERIOD;
    return Stream.of(
        buildTemporaryExposureKeyWithoutDSOS(VALID_KEY_DATA_1, rollingStartIntervalNumber1, 3,
            CONFIRMED_TEST),
        buildTemporaryExposureKeyWithoutDSOS(VALID_KEY_DATA_2, rollingStartIntervalNumber3, 6,
            CONFIRMED_TEST),
        buildTemporaryExposureKeyWithoutDSOS(VALID_KEY_DATA_3, rollingStartIntervalNumber2, 8,
            CONFIRMED_TEST))
        .collect(Collectors.toCollection(ArrayList::new));
  }

  public static Collection<TemporaryExposureKey> buildMultipleKeysWithoutTRL(SubmissionServiceConfig config) {
    int rollingStartIntervalNumber1 = createRollingStartIntervalNumber(config.getRetentionDays() - 1);
    int rollingStartIntervalNumber2 = rollingStartIntervalNumber1 + DiagnosisKey.MAX_ROLLING_PERIOD;
    int rollingStartIntervalNumber3 = rollingStartIntervalNumber2 + DiagnosisKey.MAX_ROLLING_PERIOD;
    return Stream.of(
        buildTemporaryExposureKeyWithoutTRL(VALID_KEY_DATA_1, rollingStartIntervalNumber1, CONFIRMED_TEST, 8),
        buildTemporaryExposureKeyWithoutTRL(VALID_KEY_DATA_2, rollingStartIntervalNumber3, CONFIRMED_TEST, 10),
        buildTemporaryExposureKeyWithoutTRL(VALID_KEY_DATA_3, rollingStartIntervalNumber2, CONFIRMED_TEST, 14))
        .collect(Collectors.toCollection(ArrayList::new));
  }

  public static Collection<TemporaryExposureKey> buildMultipleKeysWithoutDSOSAndTRL(SubmissionServiceConfig config) {
    int rollingStartIntervalNumber1 = createRollingStartIntervalNumber(config.getRetentionDays() - 1);
    int rollingStartIntervalNumber2 = rollingStartIntervalNumber1 + DiagnosisKey.MAX_ROLLING_PERIOD;
    int rollingStartIntervalNumber3 = rollingStartIntervalNumber2 + DiagnosisKey.MAX_ROLLING_PERIOD;
    return Stream.of(
        buildTemporaryExposureKeyWithoutDSOSAndTRL(VALID_KEY_DATA_1, rollingStartIntervalNumber1, CONFIRMED_TEST),
        buildTemporaryExposureKeyWithoutDSOSAndTRL(VALID_KEY_DATA_2, rollingStartIntervalNumber3, CONFIRMED_TEST),
        buildTemporaryExposureKeyWithoutDSOSAndTRL(VALID_KEY_DATA_3, rollingStartIntervalNumber2, CONFIRMED_TEST))
        .collect(Collectors.toCollection(ArrayList::new));
  }

  public static SubmissionPayload buildPayloadWithOriginCountry(String originCountry) {
    TemporaryExposureKey key =
        buildTemporaryExposureKey(VALID_KEY_DATA_1, createRollingStartIntervalNumber(2), 3,
            ReportType.CONFIRMED_TEST, 1);
    return SubmissionPayload.newBuilder()
        .addKeys(key)
        .addAllVisitedCountries(List.of("DE", "FR"))
        .setOrigin(originCountry)
        .setRequestPadding(ByteString.copyFrom("PaddingString".getBytes()))
        .build();
  }

  public static SubmissionPayload buildPayloadWithVisitedCountries(List<String> visitedCountries) {
    TemporaryExposureKey key =
        buildTemporaryExposureKey(VALID_KEY_DATA_1, createRollingStartIntervalNumber(2), 3,
            ReportType.CONFIRMED_TEST, 1);
    return SubmissionPayload.newBuilder()
        .addKeys(key)
        .addAllVisitedCountries(visitedCountries)
        .setOrigin("DE")
        .setRequestPadding(ByteString.copyFrom("PaddingString".getBytes()))
        .build();
  }

  public static TemporaryExposureKey buildTemporaryExposureKey(
      String keyData, int rollingStartIntervalNumber, Integer transmissionRiskLevel, ReportType reportType,
      Integer daysSinceOnsetOfSymptoms) {
    Builder builder = TemporaryExposureKey.newBuilder()
        .setKeyData(ByteString.copyFromUtf8(keyData))
        .setRollingStartIntervalNumber(rollingStartIntervalNumber);
    if (transmissionRiskLevel != null) {
      builder.setTransmissionRiskLevel(transmissionRiskLevel);
    }
    builder.setReportType(reportType);
    if (daysSinceOnsetOfSymptoms != null) {
      builder.setDaysSinceOnsetOfSymptoms(daysSinceOnsetOfSymptoms);
    }
    return builder.build();
  }

  public static TemporaryExposureKey buildTemporaryExposureKeyWithoutDSOS(
      String keyData, int rollingStartIntervalNumber, int transmissionRiskLevel, ReportType reportType) {
    return TemporaryExposureKey.newBuilder()
        .setKeyData(ByteString.copyFromUtf8(keyData))
        .setRollingStartIntervalNumber(rollingStartIntervalNumber)
        .setTransmissionRiskLevel(transmissionRiskLevel)
        .setReportType(reportType)
        .build();
  }

  public static TemporaryExposureKey buildTemporaryExposureKeyWithoutTRL(
      String keyData, int rollingStartIntervalNumber, ReportType reportType, int daysSinceOnsetOfSymptoms) {
    return TemporaryExposureKey.newBuilder()
        .setKeyData(ByteString.copyFromUtf8(keyData))
        .setRollingStartIntervalNumber(rollingStartIntervalNumber)
        .setReportType(reportType)
        .setDaysSinceOnsetOfSymptoms(daysSinceOnsetOfSymptoms)
        .build();
  }

  public static TemporaryExposureKey buildTemporaryExposureKeyWithoutDSOSAndTRL(
      String keyData, int rollingStartIntervalNumber, ReportType reportType) {
    return TemporaryExposureKey.newBuilder()
        .setKeyData(ByteString.copyFromUtf8(keyData))
        .setRollingStartIntervalNumber(rollingStartIntervalNumber)
        .setReportType(reportType)
        .build();
  }

  public static TemporaryExposureKey buildTemporaryExposureKeyWithFlexibleRollingPeriod(
      String keyData, int rollingStartIntervalNumber, int transmissionRiskLevel, int rollingPeriod) {
    return TemporaryExposureKey.newBuilder()
        .setKeyData(ByteString.copyFromUtf8(keyData))
        .setRollingStartIntervalNumber(rollingStartIntervalNumber)
        .setTransmissionRiskLevel(transmissionRiskLevel)
        .setRollingPeriod(rollingPeriod).build();
  }

  public static int createRollingStartIntervalNumber(Integer daysAgo) {
    return Math.toIntExact(LocalDate
        .ofInstant(Instant.now(), UTC)
        .minusDays(daysAgo).atStartOfDay()
        .toEpochSecond(UTC) / (60 * 10));
  }
}
