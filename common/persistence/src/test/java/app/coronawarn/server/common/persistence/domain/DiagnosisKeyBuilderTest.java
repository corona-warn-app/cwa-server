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

package app.coronawarn.server.common.persistence.domain;

import static app.coronawarn.server.common.persistence.domain.validation.ValidSubmissionTimestampValidator.SECONDS_PER_HOUR;
import static app.coronawarn.server.common.persistence.service.DiagnosisKeyServiceTestHelper.buildDiagnosisKeyForSubmissionTimestamp;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.catchThrowable;

import app.coronawarn.server.common.persistence.exception.InvalidDiagnosisKeyException;
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import app.coronawarn.server.common.protocols.external.exposurenotification.TemporaryExposureKey;
import com.google.protobuf.ByteString;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DiagnosisKeyBuilderTest {

  private final byte[] expKeyData = "16-bytelongarray".getBytes(StandardCharsets.US_ASCII);
  private final int expRollingStartIntervalNumber = 73800;
  private final int expTransmissionRiskLevel = 1;
  private final long expSubmissionTimestamp = 2L;
  private final boolean expConsentToFederation = false;
  private final String originCountry = "DE";
  private final List<String> visitedCountries = Arrays.asList("DE");
  private final ReportType reportType = ReportType.CONFIRMED_CLINICAL_DIAGNOSIS;
  private final int daysSinceOnsetOfSymptoms = 2;


  @Test
  void buildFromProtoBufObjWithSubmissionTimestamp() {
    TemporaryExposureKey protoBufObj = TemporaryExposureKey
        .newBuilder()
        .setKeyData(ByteString.copyFrom(expKeyData))
        .setRollingStartIntervalNumber(expRollingStartIntervalNumber)
        .setRollingPeriod(DiagnosisKey.MAX_ROLLING_PERIOD)
        .setTransmissionRiskLevel(expTransmissionRiskLevel)
        .setReportType(reportType)
        .setDaysSinceOnsetOfSymptoms(daysSinceOnsetOfSymptoms)
        .build();

    DiagnosisKey actDiagnosisKey = DiagnosisKey.builder()
        .fromTemporaryExposureKey(protoBufObj)
        .withSubmissionTimestamp(expSubmissionTimestamp)
        .withReportType(reportType)
        .withDaysSinceOnsetOfSymptoms(daysSinceOnsetOfSymptoms)
        .withConsentToFederation(expConsentToFederation)
        .withCountryCode(originCountry)
        .withVisitedCountries(visitedCountries)
        .build();

    assertDiagnosisKeyEquals(actDiagnosisKey, expSubmissionTimestamp);
  }

  @Test
  void buildFromProtoBufObjWithoutSubmissionTimestamp() {
    TemporaryExposureKey protoBufObj = TemporaryExposureKey
        .newBuilder()
        .setKeyData(ByteString.copyFrom(expKeyData))
        .setRollingStartIntervalNumber(expRollingStartIntervalNumber)
        .setRollingPeriod(DiagnosisKey.MAX_ROLLING_PERIOD)
        .setTransmissionRiskLevel(expTransmissionRiskLevel)
        .setReportType(reportType)
        .setDaysSinceOnsetOfSymptoms(daysSinceOnsetOfSymptoms)
        .build();

    DiagnosisKey actDiagnosisKey = DiagnosisKey.builder()
        .fromTemporaryExposureKey(protoBufObj)
        .withReportType(reportType)
        .withDaysSinceOnsetOfSymptoms(daysSinceOnsetOfSymptoms)
        .withConsentToFederation(expConsentToFederation)
        .withCountryCode(originCountry)
        .withVisitedCountries(visitedCountries)
        .build();

    assertDiagnosisKeyEquals(actDiagnosisKey);
  }

  @Test
  void buildSuccessivelyWithSubmissionTimestamp() {
    DiagnosisKey actDiagnosisKey = DiagnosisKey.builder()
        .withKeyData(expKeyData)
        .withRollingStartIntervalNumber(expRollingStartIntervalNumber)
        .withTransmissionRiskLevel(expTransmissionRiskLevel)
        .withSubmissionTimestamp(expSubmissionTimestamp)
        .withReportType(reportType)
        .withDaysSinceOnsetOfSymptoms(daysSinceOnsetOfSymptoms)
        .withConsentToFederation(expConsentToFederation)
        .withCountryCode(originCountry)
        .withVisitedCountries(visitedCountries)
        .build();

    assertDiagnosisKeyEquals(actDiagnosisKey, expSubmissionTimestamp);
  }

  @Test
  void buildSuccessivelyWithoutSubmissionTimestamp() {
    DiagnosisKey actDiagnosisKey = DiagnosisKey.builder()
        .withKeyData(expKeyData)
        .withRollingStartIntervalNumber(expRollingStartIntervalNumber)
        .withTransmissionRiskLevel(expTransmissionRiskLevel)
        .withReportType(reportType)
        .withDaysSinceOnsetOfSymptoms(daysSinceOnsetOfSymptoms)
        .withConsentToFederation(expConsentToFederation)
        .withCountryCode(originCountry)
        .withVisitedCountries(visitedCountries)
        .build();

    assertDiagnosisKeyEquals(actDiagnosisKey);
  }

  @Test
  void buildSuccessivelyWithRollingPeriod() {
    DiagnosisKey actDiagnosisKey = DiagnosisKey.builder()
        .withKeyData(expKeyData)
        .withRollingStartIntervalNumber(expRollingStartIntervalNumber)
        .withTransmissionRiskLevel(expTransmissionRiskLevel)
        .withSubmissionTimestamp(expSubmissionTimestamp)
        .withRollingPeriod(DiagnosisKey.MAX_ROLLING_PERIOD)
        .withReportType(reportType)
        .withDaysSinceOnsetOfSymptoms(daysSinceOnsetOfSymptoms)
        .withConsentToFederation(expConsentToFederation)
        .withCountryCode(originCountry)
        .withVisitedCountries(visitedCountries)
        .build();

    assertDiagnosisKeyEquals(actDiagnosisKey, expSubmissionTimestamp);
  }

  @ParameterizedTest
  @ValueSource(ints = {4200, 441552})
  void rollingStartIntervalNumberDoesNotThrowForValid(int validRollingStartIntervalNumber) {
    assertThatCode(() -> keyWithRollingStartIntervalNumber(validRollingStartIntervalNumber)).doesNotThrowAnyException();
  }

  @Test
  void rollingStartIntervalNumberCannotBeInFuture() {
    assertThat(catchThrowable(() -> keyWithRollingStartIntervalNumber(Integer.MAX_VALUE)))
        .isInstanceOf(InvalidDiagnosisKeyException.class)
        .hasMessage(
            "[Rolling start interval number must be greater 0 and cannot be in the future. Invalid Value: "
                + Integer.MAX_VALUE + "]");

    long tomorrow = LocalDate
        .ofInstant(Instant.now(), ZoneOffset.UTC)
        .plusDays(1).atStartOfDay()
        .toEpochSecond(ZoneOffset.UTC);

    assertThat(catchThrowable(() -> keyWithRollingStartIntervalNumber((int) tomorrow)))
        .isInstanceOf(InvalidDiagnosisKeyException.class)
        .hasMessage(
            String.format(
                "[Rolling start interval number must be greater 0 and cannot be in the future. Invalid Value: %s]",
                tomorrow));
  }

  @Test
  void failsForInvalidRollingStartIntervalNumber() {
    assertThat(
        catchThrowable(() -> DiagnosisKey.builder()
            .withKeyData(expKeyData)
            .withRollingStartIntervalNumber(0)
            .withTransmissionRiskLevel(expTransmissionRiskLevel).build()
        )
    ).isInstanceOf(InvalidDiagnosisKeyException.class);
  }

  @Test
  void failsForInvalidOriginCountry() {
    assertThat(
        catchThrowable(() -> DiagnosisKey.builder()
            .withKeyData(expKeyData)
            .withRollingStartIntervalNumber(expRollingStartIntervalNumber)
            .withTransmissionRiskLevel(expTransmissionRiskLevel)
            .withCountryCode("DER")
            .build()
        )
    ).isInstanceOf(InvalidDiagnosisKeyException.class);
  }

  @ParameterizedTest
  @ValueSource(strings = {"DER","xx","De","dE","DE,FRE"})
  void failsForInvalidVisitedCountries(String visitedCountries) {
    assertThat(
        catchThrowable(() -> DiagnosisKey.builder()
            .withKeyData(expKeyData)
            .withRollingStartIntervalNumber(expRollingStartIntervalNumber)
            .withTransmissionRiskLevel(expTransmissionRiskLevel)
            .withVisitedCountries(Arrays.asList(visitedCountries))
            .build()
        )
    ).isInstanceOf(InvalidDiagnosisKeyException.class);
  }

  @ParameterizedTest
  @ValueSource(ints = {9, -1})
  void transmissionRiskLevelMustBeInRange(int invalidRiskLevel) {
    assertThat(catchThrowable(() -> keyWithRiskLevel(invalidRiskLevel)))
        .isInstanceOf(InvalidDiagnosisKeyException.class)
        .hasMessage(
            "[Risk level must be between 0 and 8. Invalid Value: " + invalidRiskLevel + "]");
  }

  @ParameterizedTest
  @ValueSource(ints = {0, 8})
  void transmissionRiskLevelDoesNotThrowForValid(int validRiskLevel) {
    assertThatCode(() -> keyWithRiskLevel(validRiskLevel)).doesNotThrowAnyException();
  }

  @ParameterizedTest
  @ValueSource(ints = {-15, -17, 18, 25})
  void daysSinceOnsetSyptomsMustBeInRange(int invalidDsos) {
    assertThat(catchThrowable(() -> keyWithDsos(invalidDsos)))
        .isInstanceOf(InvalidDiagnosisKeyException.class)
        .hasMessage(
            "[Days since onset of symptoms value must be between -14 and 14. Invalid Value: " + invalidDsos + "]");
  }

  @ParameterizedTest
  @ValueSource(ints = {0, 8, -14, 14})
  void daysSinceOnsetSyptomsValidationDoesNotThrowForValid(int validDsos) {
    assertThatCode(() -> keyWithDsos(validDsos)).doesNotThrowAnyException();
  }

  @ParameterizedTest
  @ValueSource(ints = {-3, 145})
  void rollingPeriodMustBeExpectedValue(int invalidRollingPeriod) {
    assertThat(catchThrowable(() -> keyWithRollingPeriod(invalidRollingPeriod)))
        .isInstanceOf(InvalidDiagnosisKeyException.class)
        .hasMessage("[Rolling period must be between " + DiagnosisKey.MIN_ROLLING_PERIOD + " and "
            + DiagnosisKey.MAX_ROLLING_PERIOD
            + ". Invalid Value: " + invalidRollingPeriod + "]");
  }

  @ParameterizedTest
  @ValueSource(ints = {DiagnosisKey.MIN_ROLLING_PERIOD, 100, DiagnosisKey.MAX_ROLLING_PERIOD})
  void rollingPeriodDoesNotThrowForValid(int validRollingPeriod) {
    assertThatCode(() -> keyWithRollingPeriod(validRollingPeriod)).doesNotThrowAnyException();
  }

  @ParameterizedTest
  @ValueSource(strings = {"17--bytelongarray", "", "1"})
  void keyDataMustHaveValidLength(String invalidKeyString) {
    assertThat(
        catchThrowable(() -> keyWithKeyData(invalidKeyString.getBytes(StandardCharsets.US_ASCII))))
        .isInstanceOf(InvalidDiagnosisKeyException.class);
  }

  @Test
  void keyDataDoesNotThrowOnValid() {
    assertThatCode(() -> keyWithKeyData("16-bytelongarray".getBytes(StandardCharsets.US_ASCII)))
        .doesNotThrowAnyException();
  }

  @ParameterizedTest
  @ValueSource(longs = {-1L, Long.MAX_VALUE})
  void submissionTimestampMustBeValid(long submissionTimestamp) {
    assertThat(
        catchThrowable(() -> buildDiagnosisKeyForSubmissionTimestamp(submissionTimestamp)))
        .isInstanceOf(InvalidDiagnosisKeyException.class);
  }

  @Test
  void submissionTimestampMustNotBeInTheFuture() {
    assertThat(catchThrowable(
        () -> buildDiagnosisKeyForSubmissionTimestamp(getCurrentHoursSinceEpoch() + 1)))
        .isInstanceOf(InvalidDiagnosisKeyException.class);
    assertThat(catchThrowable(() -> buildDiagnosisKeyForSubmissionTimestamp(
        Instant.now().getEpochSecond() /* accidentally forgot to divide by SECONDS_PER_HOUR */)))
        .isInstanceOf(InvalidDiagnosisKeyException.class);
  }

  @Test
  void submissionTimestampDoesNotThrowOnValid() {
    assertThatCode(() -> buildDiagnosisKeyForSubmissionTimestamp(0L)).doesNotThrowAnyException();
    assertThatCode(() -> buildDiagnosisKeyForSubmissionTimestamp(getCurrentHoursSinceEpoch()))
        .doesNotThrowAnyException();
    assertThatCode(
        () -> buildDiagnosisKeyForSubmissionTimestamp(
            Instant.now().minus(Duration.ofHours(2)).getEpochSecond() / SECONDS_PER_HOUR))
        .doesNotThrowAnyException();
  }

  @Test
  void testKeyHasDefaultForReportType() {
    TemporaryExposureKey protoBufObj = TemporaryExposureKey
        .newBuilder()
        .setKeyData(ByteString.copyFrom(expKeyData))
        .setRollingStartIntervalNumber(expRollingStartIntervalNumber)
        .setRollingPeriod(DiagnosisKey.MAX_ROLLING_PERIOD)
        .setTransmissionRiskLevel(expTransmissionRiskLevel)
        .build();

    DiagnosisKey actDiagnosisKey = DiagnosisKey.builder().fromTemporaryExposureKey(protoBufObj).build();

    assertThat(actDiagnosisKey.getReportType()).isEqualTo(reportType);
  }

  private DiagnosisKey keyWithKeyData(byte[] expKeyData) {
    return DiagnosisKey.builder()
        .withKeyData(expKeyData)
        .withRollingStartIntervalNumber(expRollingStartIntervalNumber)
        .withTransmissionRiskLevel(expTransmissionRiskLevel).build();
  }

  private DiagnosisKey keyWithRollingStartIntervalNumber(int expRollingStartIntervalNumber) {
    return DiagnosisKey.builder()
        .withKeyData(expKeyData)
        .withRollingStartIntervalNumber(expRollingStartIntervalNumber)
        .withTransmissionRiskLevel(expTransmissionRiskLevel).build();
  }

  private DiagnosisKey keyWithRollingPeriod(int expRollingPeriod) {
    return DiagnosisKey.builder()
        .withKeyData(expKeyData)
        .withRollingStartIntervalNumber(expRollingStartIntervalNumber)
        .withTransmissionRiskLevel(expTransmissionRiskLevel)
        .withRollingPeriod(expRollingPeriod).build();
  }

  private DiagnosisKey keyWithRiskLevel(int expTransmissionRiskLevel) {
    return DiagnosisKey.builder()
        .withKeyData(expKeyData)
        .withRollingStartIntervalNumber(expRollingStartIntervalNumber)
        .withTransmissionRiskLevel(expTransmissionRiskLevel).build();
  }

  private DiagnosisKey keyWithDsos(int dsos) {
    return DiagnosisKey.builder()
        .withKeyData(expKeyData)
        .withRollingStartIntervalNumber(expRollingStartIntervalNumber)
        .withTransmissionRiskLevel(expTransmissionRiskLevel)
        .withDaysSinceOnsetOfSymptoms(dsos).build();
  }

  private void assertDiagnosisKeyEquals(DiagnosisKey actDiagnosisKey) {
    assertDiagnosisKeyEquals(actDiagnosisKey, getCurrentHoursSinceEpoch());
  }

  private long getCurrentHoursSinceEpoch() {
    return Instant.now().getEpochSecond() / SECONDS_PER_HOUR;
  }

  private void assertDiagnosisKeyEquals(DiagnosisKey actDiagnosisKey, long expSubmissionTimestamp) {
    assertThat(actDiagnosisKey.getKeyData()).isEqualTo(expKeyData);
    assertThat(actDiagnosisKey.getRollingStartIntervalNumber()).isEqualTo(expRollingStartIntervalNumber);
    assertThat(actDiagnosisKey.getRollingPeriod()).isEqualTo(DiagnosisKey.MAX_ROLLING_PERIOD);
    assertThat(actDiagnosisKey.getTransmissionRiskLevel()).isEqualTo(expTransmissionRiskLevel);
    assertThat(actDiagnosisKey.getSubmissionTimestamp()).isEqualTo(expSubmissionTimestamp);
    assertThat(actDiagnosisKey.getReportType()).isEqualTo(reportType);
    assertThat(actDiagnosisKey.getDaysSinceOnsetOfSymptoms()).isEqualTo(daysSinceOnsetOfSymptoms);
    assertThat(actDiagnosisKey.isConsentToFederation()).isEqualTo(expConsentToFederation);
    assertThat(actDiagnosisKey.getOriginCountry()).isEqualTo(originCountry);
    assertThat(actDiagnosisKey.getVisitedCountries()).isEqualTo(visitedCountries);
  }
}
