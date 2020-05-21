/*
 * Corona-Warn-App
 *
 * SAP SE and all other contributors /
 * copyright owners license this file to you under the Apache
 * License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package app.coronawarn.server.common.persistence.domain;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import app.coronawarn.server.common.persistence.exception.InvalidDiagnosisKeyException;
import app.coronawarn.server.common.protocols.external.exposurenotification.TemporaryExposureKey;
import com.google.protobuf.ByteString;
import java.nio.charset.Charset;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class DiagnosisKeyBuilderTest {

  private final byte[] expKeyData = "16-bytelongarray".getBytes(Charset.defaultCharset());
  private final long expRollingStartNumber = 73800;
  private final long expRollingPeriod = 144;
  private final int expTransmissionRiskLevel = 1;
  private final long expSubmissionTimestamp = 2L;

  @Test
  public void buildFromProtoBufObjWithSubmissionTimestamp() {
    TemporaryExposureKey protoBufObj = TemporaryExposureKey
        .newBuilder()
        .setKeyData(ByteString.copyFrom(this.expKeyData))
        .setRollingStartIntervalNumber(Long.valueOf(this.expRollingStartNumber).intValue())
        .setRollingPeriod(Long.valueOf(this.expRollingPeriod).intValue())
        .setTransmissionRiskLevel(this.expTransmissionRiskLevel)
        .build();

    DiagnosisKey actDiagnosisKey = null;
    actDiagnosisKey = DiagnosisKey.builder()
        .fromProtoBuf(protoBufObj)
        .withSubmissionTimestamp(this.expSubmissionTimestamp)
        .build();

    assertDiagnosisKeyEquals(actDiagnosisKey, this.expSubmissionTimestamp);
  }

  @Test
  public void buildFromProtoBufObjWithoutSubmissionTimestamp() {
    TemporaryExposureKey protoBufObj = TemporaryExposureKey
        .newBuilder()
        .setKeyData(ByteString.copyFrom(this.expKeyData))
        .setRollingStartIntervalNumber(Long.valueOf(this.expRollingStartNumber).intValue())
        .setRollingPeriod(Long.valueOf(this.expRollingPeriod).intValue())
        .setTransmissionRiskLevel(this.expTransmissionRiskLevel)
        .build();

    DiagnosisKey actDiagnosisKey = DiagnosisKey.builder().fromProtoBuf(protoBufObj).build();

    assertDiagnosisKeyEquals(actDiagnosisKey);
  }

  @Test
  public void buildSuccessivelyWithSubmissionTimestamp() {
    DiagnosisKey actDiagnosisKey = DiagnosisKey.builder()
        .withKeyData(this.expKeyData)
        .withRollingStartNumber(this.expRollingStartNumber)
        .withRollingPeriod(this.expRollingPeriod)
        .withTransmissionRiskLevel(this.expTransmissionRiskLevel)
        .withSubmissionTimestamp(this.expSubmissionTimestamp).build();

    assertDiagnosisKeyEquals(actDiagnosisKey, this.expSubmissionTimestamp);
  }

  @Test
  public void buildSuccessivelyWithoutSubmissionTimestamp() {
    DiagnosisKey actDiagnosisKey = DiagnosisKey.builder()
        .withKeyData(this.expKeyData)
        .withRollingStartNumber(this.expRollingStartNumber)
        .withRollingPeriod(this.expRollingPeriod)
        .withTransmissionRiskLevel(this.expTransmissionRiskLevel).build();

    assertDiagnosisKeyEquals(actDiagnosisKey);
  }

  @Test
  public void rollingStartNumberDoesNotThrowForValid() {
    Assertions.assertThatCode(() -> keyWithRollingStartNumber(4200L)).doesNotThrowAnyException();

    // Timestamp: 05/16/2020 @ 00:00 in hours
    Assertions.assertThatCode(() -> keyWithRollingStartNumber(441552L)).doesNotThrowAnyException();
  }

  @Test
  public void rollingStartNumberCannotBeInFuture() {
    checkMessage(assertThrows(
        InvalidDiagnosisKeyException.class,
        () -> keyWithRollingStartNumber(Long.MAX_VALUE)),
        "[Rolling start number must be greater 0 and cannot be in the future. Invalid Value: " + Long.MAX_VALUE + "]");

    long tomorrow = LocalDate
        .ofInstant(Instant.now(), ZoneOffset.UTC)
        .plusDays(1).atStartOfDay()
        .toEpochSecond(ZoneOffset.UTC);

    checkMessage(assertThrows(
        InvalidDiagnosisKeyException.class, () -> keyWithRollingStartNumber(tomorrow)),
        String.format("[Rolling start number must be greater 0 and cannot be in the future. Invalid Value: %s]",
            tomorrow));

  }

  @Test
  public void failsForInvalidRollingStartNumber() {
    assertThrows(
        InvalidDiagnosisKeyException.class, () ->
            DiagnosisKey.builder()
                .withKeyData(this.expKeyData)
                .withRollingStartNumber(0L)
                .withRollingPeriod(this.expRollingPeriod)
                .withTransmissionRiskLevel(this.expTransmissionRiskLevel).build()
    );
  }

  @ParameterizedTest
  @ValueSource(ints = {9, -1})
  public void transmissionRiskLevelMustBeInRange(int invalidRiskLevel) {
    checkMessage(assertThrows(
        InvalidDiagnosisKeyException.class, () -> keyWithRiskLevel(invalidRiskLevel)),
        "[Risk level must be between 0 and 8. Invalid Value: " + invalidRiskLevel + "]");
  }

  @ParameterizedTest
  @ValueSource(ints = {0, 8})
  public void transmissionRiskLevelDoesNotThrowForValid(int validRiskLevel) {
    Assertions.assertThatCode(() -> keyWithRiskLevel(validRiskLevel)).doesNotThrowAnyException();
  }

  @ParameterizedTest
  @ValueSource(longs = {0L, -3L})
  public void rollingPeriodMustBeLargerThanZero(long invalidRollingPeriod) {
    checkMessage(assertThrows(
        InvalidDiagnosisKeyException.class, () -> keyWithRollingPeriod(invalidRollingPeriod)),
        "[Rolling period must be greater than 0. Invalid Value: " + invalidRollingPeriod + "]");
  }

  @Test
  public void rollingPeriodDoesNotThrowForValid() {
    Assertions.assertThatCode(() -> keyWithRollingPeriod(144L)).doesNotThrowAnyException();
  }

  @ParameterizedTest
  @ValueSource(strings = {"17--bytelongarray", "", "1"})
  public void keyDataMustHaveValidLength(String invalidKeyString) {
    assertThrows(
        InvalidDiagnosisKeyException.class,
        () -> keyWithKeyData(invalidKeyString.getBytes(Charset.defaultCharset())));
  }

  @Test
  public void keyDataDoesNotThrowOnValid() {
    Assertions.assertThatCode(() -> keyWithKeyData("16-bytelongarray".getBytes(Charset.defaultCharset())))
        .doesNotThrowAnyException();
  }

  private DiagnosisKey keyWithKeyData(byte[] expKeyData) {
    return DiagnosisKey.builder()
        .withKeyData(expKeyData)
        .withRollingStartNumber(expRollingStartNumber)
        .withRollingPeriod(expRollingPeriod)
        .withTransmissionRiskLevel(expTransmissionRiskLevel).build();
  }

  private DiagnosisKey keyWithRollingStartNumber(long expRollingStartNumber) {
    return DiagnosisKey.builder()
        .withKeyData(expKeyData)
        .withRollingStartNumber(expRollingStartNumber)
        .withRollingPeriod(expRollingPeriod)
        .withTransmissionRiskLevel(expTransmissionRiskLevel).build();
  }

  private DiagnosisKey keyWithRollingPeriod(long expRollingPeriod) {
    return DiagnosisKey.builder()
        .withKeyData(expKeyData)
        .withRollingStartNumber(expRollingStartNumber)
        .withRollingPeriod(expRollingPeriod)
        .withTransmissionRiskLevel(expTransmissionRiskLevel).build();
  }

  private DiagnosisKey keyWithRiskLevel(int expTransmissionRiskLevel) {
    return DiagnosisKey.builder()
        .withKeyData(expKeyData)
        .withRollingStartNumber(expRollingStartNumber)
        .withRollingPeriod(expRollingPeriod)
        .withTransmissionRiskLevel(expTransmissionRiskLevel).build();
  }

  private void assertDiagnosisKeyEquals(DiagnosisKey actDiagnosisKey) {
    assertDiagnosisKeyEquals(actDiagnosisKey, getCurrentHoursSinceEpoch());
  }

  private long getCurrentHoursSinceEpoch() {
    return Instant.now().getEpochSecond() / 3600L;
  }

  private void assertDiagnosisKeyEquals(DiagnosisKey actDiagnosisKey, long expSubmissionTimestamp) {
    assertEquals(expSubmissionTimestamp, actDiagnosisKey.getSubmissionTimestamp());
    assertArrayEquals(this.expKeyData, actDiagnosisKey.getKeyData());
    assertEquals(this.expRollingStartNumber, actDiagnosisKey.getRollingStartNumber());
    assertEquals(this.expRollingPeriod, actDiagnosisKey.getRollingPeriod());
    assertEquals(this.expTransmissionRiskLevel, actDiagnosisKey.getTransmissionRiskLevel());
  }

  private void checkMessage(InvalidDiagnosisKeyException ex, String message) {
    assertEquals(ex.getMessage(), message);
  }
}
