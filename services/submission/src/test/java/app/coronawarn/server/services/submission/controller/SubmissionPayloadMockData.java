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

package app.coronawarn.server.services.submission.controller;

import static app.coronawarn.server.common.protocols.external.exposurenotification.ReportType.CONFIRMED_CLINICAL_DIAGNOSIS;
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
        .addAllVisitedCountries(List.of("FR"))
        .setOrigin("DE")
        .build();
  }

  public static SubmissionPayload buildPayload(Collection<TemporaryExposureKey> keys, boolean consentToFederation) {
    return SubmissionPayload.newBuilder()
        .addAllKeys(keys)
        .addAllVisitedCountries(List.of("FR"))
        .setOrigin("DE")
        .setConsentToFederation(consentToFederation)
        .build();
  }

  public static SubmissionPayload buildInvalidPayload(TemporaryExposureKey key) {
    Collection<TemporaryExposureKey> keys = Stream.of(key).collect(Collectors.toCollection(ArrayList::new));
    return buildInvalidPayload(keys);
  }

  public static SubmissionPayload buildInvalidPayload(Collection<TemporaryExposureKey> keys) {
    return SubmissionPayload.newBuilder()
        .addAllKeys(keys)
        .addAllVisitedCountries(List.of("FR"))
        .setOrigin("DE3")
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

  public static Collection<TemporaryExposureKey> buildMultipleKeys(SubmissionServiceConfig config) {
    int rollingStartIntervalNumber1 = createRollingStartIntervalNumber(config.getRetentionDays() - 1);
    int rollingStartIntervalNumber2 = rollingStartIntervalNumber1 + DiagnosisKey.MAX_ROLLING_PERIOD;
    int rollingStartIntervalNumber3 = rollingStartIntervalNumber2 + DiagnosisKey.MAX_ROLLING_PERIOD;
    return Stream.of(
        buildTemporaryExposureKey(VALID_KEY_DATA_1, rollingStartIntervalNumber1, 3, CONFIRMED_CLINICAL_DIAGNOSIS, 1),
        buildTemporaryExposureKey(VALID_KEY_DATA_2, rollingStartIntervalNumber3, 6, CONFIRMED_CLINICAL_DIAGNOSIS, 1),
        buildTemporaryExposureKey(VALID_KEY_DATA_3, rollingStartIntervalNumber2, 8, CONFIRMED_CLINICAL_DIAGNOSIS, 1))
        .collect(Collectors.toCollection(ArrayList::new));
  }

  public static Collection<TemporaryExposureKey> buildMultipleKeysWithoutDSOS(SubmissionServiceConfig config) {
    int rollingStartIntervalNumber1 = createRollingStartIntervalNumber(config.getRetentionDays() - 1);
    int rollingStartIntervalNumber2 = rollingStartIntervalNumber1 + DiagnosisKey.MAX_ROLLING_PERIOD;
    int rollingStartIntervalNumber3 = rollingStartIntervalNumber2 + DiagnosisKey.MAX_ROLLING_PERIOD;
    return Stream.of(
        buildTemporaryExposureKeyWithoutDSOS(VALID_KEY_DATA_1, rollingStartIntervalNumber1, 3, CONFIRMED_CLINICAL_DIAGNOSIS),
        buildTemporaryExposureKeyWithoutDSOS(VALID_KEY_DATA_2, rollingStartIntervalNumber3, 6, CONFIRMED_CLINICAL_DIAGNOSIS),
        buildTemporaryExposureKeyWithoutDSOS(VALID_KEY_DATA_3, rollingStartIntervalNumber2, 8, CONFIRMED_CLINICAL_DIAGNOSIS))
        .collect(Collectors.toCollection(ArrayList::new));
  }

  public static Collection<TemporaryExposureKey> buildMultipleKeysWithoutTRL(SubmissionServiceConfig config) {
    int rollingStartIntervalNumber1 = createRollingStartIntervalNumber(config.getRetentionDays() - 1);
    int rollingStartIntervalNumber2 = rollingStartIntervalNumber1 + DiagnosisKey.MAX_ROLLING_PERIOD;
    int rollingStartIntervalNumber3 = rollingStartIntervalNumber2 + DiagnosisKey.MAX_ROLLING_PERIOD;
    return Stream.of(
        buildTemporaryExposureKeyWithoutTRL(VALID_KEY_DATA_1, rollingStartIntervalNumber1, CONFIRMED_CLINICAL_DIAGNOSIS, 8),
        buildTemporaryExposureKeyWithoutTRL(VALID_KEY_DATA_2, rollingStartIntervalNumber3, CONFIRMED_CLINICAL_DIAGNOSIS, 10),
        buildTemporaryExposureKeyWithoutTRL(VALID_KEY_DATA_3, rollingStartIntervalNumber2, CONFIRMED_CLINICAL_DIAGNOSIS, 14))
        .collect(Collectors.toCollection(ArrayList::new));
  }

  public static Collection<TemporaryExposureKey> buildMultipleKeysWithoutDSOSAndTRL(SubmissionServiceConfig config) {
    int rollingStartIntervalNumber1 = createRollingStartIntervalNumber(config.getRetentionDays() - 1);
    int rollingStartIntervalNumber2 = rollingStartIntervalNumber1 + DiagnosisKey.MAX_ROLLING_PERIOD;
    int rollingStartIntervalNumber3 = rollingStartIntervalNumber2 + DiagnosisKey.MAX_ROLLING_PERIOD;
    return Stream.of(
        buildTemporaryExposureKeyWithoutDSOSAndTRL(VALID_KEY_DATA_1, rollingStartIntervalNumber1, CONFIRMED_CLINICAL_DIAGNOSIS),
        buildTemporaryExposureKeyWithoutDSOSAndTRL(VALID_KEY_DATA_2, rollingStartIntervalNumber3, CONFIRMED_CLINICAL_DIAGNOSIS),
        buildTemporaryExposureKeyWithoutDSOSAndTRL(VALID_KEY_DATA_3, rollingStartIntervalNumber2, CONFIRMED_CLINICAL_DIAGNOSIS))
        .collect(Collectors.toCollection(ArrayList::new));
  }

  public static SubmissionPayload buildPayloadWithInvalidKey() {
    TemporaryExposureKey invalidKey =
        buildTemporaryExposureKey(VALID_KEY_DATA_1, createRollingStartIntervalNumber(2), 999, ReportType.CONFIRMED_CLINICAL_DIAGNOSIS, 1);
    return buildPayload(invalidKey);
  }

  public static SubmissionPayload buildPayloadWithInvalidOriginCountry() {
    TemporaryExposureKey key =
        buildTemporaryExposureKey(VALID_KEY_DATA_1, createRollingStartIntervalNumber(2), 2, ReportType.CONFIRMED_CLINICAL_DIAGNOSIS, 1);
    return buildInvalidPayload(key);
  }

  public static SubmissionPayload buildPayloadWithVisitedCountries(List<String> visitedCountries) {
    TemporaryExposureKey key =
        buildTemporaryExposureKey(VALID_KEY_DATA_1, createRollingStartIntervalNumber(2), 2, ReportType.CONFIRMED_CLINICAL_DIAGNOSIS, 1);
    return SubmissionPayload.newBuilder()
        .addKeys(key)
        .addAllVisitedCountries(visitedCountries)
        .setOrigin("DE")
        .setRequestPadding(ByteString.copyFrom("PaddingString".getBytes()))
        .build();
  }

  public static TemporaryExposureKey buildTemporaryExposureKey(
      String keyData, int rollingStartIntervalNumber, Integer transmissionRiskLevel, ReportType reportType, Integer daysSinceOnsetOfSymptoms){
    Builder builder = TemporaryExposureKey.newBuilder()
        .setKeyData(ByteString.copyFromUtf8(keyData))
        .setRollingStartIntervalNumber(rollingStartIntervalNumber);
    if(transmissionRiskLevel != null) {
      builder.setTransmissionRiskLevel(transmissionRiskLevel);
    }
    builder.setReportType(reportType);
    if (daysSinceOnsetOfSymptoms != null) {
      builder.setDaysSinceOnsetOfSymptoms(daysSinceOnsetOfSymptoms);
    }
    return builder.build();
  }

  public static TemporaryExposureKey buildTemporaryExposureKeyWithoutDSOS(
      String keyData, int rollingStartIntervalNumber, int transmissionRiskLevel, ReportType reportType){
    return TemporaryExposureKey.newBuilder()
        .setKeyData(ByteString.copyFromUtf8(keyData))
        .setRollingStartIntervalNumber(rollingStartIntervalNumber)
        .setTransmissionRiskLevel(transmissionRiskLevel)
        .setReportType(reportType)
        .build();
  }

  public static TemporaryExposureKey buildTemporaryExposureKeyWithoutTRL(
      String keyData, int rollingStartIntervalNumber, ReportType reportType, int daysSinceOnsetOfSymptoms){
    return TemporaryExposureKey.newBuilder()
        .setKeyData(ByteString.copyFromUtf8(keyData))
        .setRollingStartIntervalNumber(rollingStartIntervalNumber)
        .setReportType(reportType)
        .setDaysSinceOnsetOfSymptoms(daysSinceOnsetOfSymptoms)
        .build();
  }

  public static TemporaryExposureKey buildTemporaryExposureKeyWithoutDSOSAndTRL(
      String keyData, int rollingStartIntervalNumber, ReportType reportType){
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

  public static Collection<TemporaryExposureKey> buildPayloadWithOneKey() {
    return Collections.singleton(buildTemporaryExposureKey(VALID_KEY_DATA_1, createRollingStartIntervalNumber(1), 3,ReportType.CONFIRMED_CLINICAL_DIAGNOSIS,1));
  }

  private static SubmissionPayload buildPayloadWithPadding(Collection<TemporaryExposureKey> keys, byte[] bytes) {
    return SubmissionPayload.newBuilder()
        .addAllKeys(keys)
        .addAllVisitedCountries(List.of("FR"))
        .setOrigin("DE")
        .setRequestPadding(ByteString.copyFrom(bytes))
        .build();
  }
}
