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

import static java.time.ZoneOffset.UTC;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.coronawarn.server.common.persistence.exception.InvalidDiagnosisKeyException;
import java.nio.charset.Charset;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class DiagnosisKeyTest {

  final static byte[] expKeyData = "testKey111111111".getBytes(Charset.defaultCharset());
  final static long expRollingStartNumber = 1L;
  final static long expRollingPeriod = 2L;
  final static int expTransmissionRiskLevel = 3;
  final static long expSubmissionTimestamp = 4L;
  final static DiagnosisKey diagnosisKey = new DiagnosisKey(expKeyData, expRollingStartNumber,
      expRollingPeriod, expTransmissionRiskLevel, expSubmissionTimestamp);

  @Test
  public void testRollingStartNumberGetter() {
    assertEquals(expRollingStartNumber, diagnosisKey.getRollingStartNumber());
  }

  @Test
  public void testRollingPeriodGetter() {
    assertEquals(expRollingPeriod, diagnosisKey.getRollingPeriod());
  }

  @Test
  public void testTransmissionRiskLevelGetter() {
    assertEquals(expTransmissionRiskLevel, diagnosisKey.getTransmissionRiskLevel());
  }

  @Test
  public void testSubmissionTimestampGetter() {
    assertEquals(expSubmissionTimestamp, diagnosisKey.getSubmissionTimestamp());
  }

  @Test
  public void transmissionRiskLevelMustBeInRange() {
    checkMessage(assertThrows(
        InvalidDiagnosisKeyException.class, keyWithRiskLevel(9)::validate),
        "[Risk level must be between 0 and 8. Invalid Value: 9]");

    checkMessage(assertThrows(
        InvalidDiagnosisKeyException.class, keyWithRiskLevel(-1)::validate),
        "[Risk level must be between 0 and 8. Invalid Value: -1]");
  }

  @Test
  public void transmissionRiskLevelDoesNotThrowForValid() {
    Assertions.assertThatCode(keyWithRiskLevel(0)::validate).doesNotThrowAnyException();
    Assertions.assertThatCode(keyWithRiskLevel(8)::validate).doesNotThrowAnyException();
  }

  @Test
  public void rollingStartNumberDoesNotThrowForValid() {
    Assertions.assertThatCode(keyWithRollingStartNumber(4200L)::validate).doesNotThrowAnyException();

    // Timestamp: 05/16/2020 @ 00:00 in hours
    Assertions.assertThatCode(keyWithRollingStartNumber(441552L)::validate).doesNotThrowAnyException();
  }

  @Test
  public void rollingStartNumberCannotBeInFuture() {
    checkMessage(assertThrows(
        InvalidDiagnosisKeyException.class,
        keyWithRollingStartNumber(1904169600L)::validate),
        "[Rolling start number must be greater 0 and cannot be in the future. Invalid Value: 1904169600]");

    long tomorrow = LocalDate
        .ofInstant(Instant.now(), ZoneOffset.UTC)
        .plusDays(1).atStartOfDay()
        .toEpochSecond(ZoneOffset.UTC);

    checkMessage(assertThrows(
        InvalidDiagnosisKeyException.class, keyWithRollingStartNumber(tomorrow)::validate),
        String.format("[Rolling start number must be greater 0 and cannot be in the future. Invalid Value: %s]",
            tomorrow));

  }

  @Test
  public void rollingPeriodMustBeLargerThanZero() {
    checkMessage(assertThrows(
        InvalidDiagnosisKeyException.class, keyWithRollingPeriod(0)::validate),
        "[Rolling period must be greater than 0. Invalid Value: 0]");

    checkMessage(assertThrows(
        InvalidDiagnosisKeyException.class, keyWithRollingPeriod(-3L)::validate),
        "[Rolling period must be greater than 0. Invalid Value: -3]");
  }

  @Test
  public void rollingPeriodDoesNotThrowForValid() {
    Assertions.assertThatCode(keyWithRollingPeriod(144L)::validate).doesNotThrowAnyException();
  }

  @Test
  public void keyDataMustHaveValidLength() {
    assertThrows(
        InvalidDiagnosisKeyException.class,
        keyWithKeyData("17--bytelongarray".getBytes(Charset.defaultCharset()))::validate);

    assertThrows(
        InvalidDiagnosisKeyException.class,
        keyWithKeyData("".getBytes(Charset.defaultCharset()))::validate);

    assertThrows(
        InvalidDiagnosisKeyException.class,
        keyWithKeyData("1".getBytes(Charset.defaultCharset()))::validate);
  }

  @Test
  public void keyDataDoesNotThrowOnValid() {
    Assertions.assertThatCode(keyWithKeyData("16-bytelongarray".getBytes(Charset.defaultCharset()))::validate)
        .doesNotThrowAnyException();
  }

  private DiagnosisKey keyWithKeyData(byte[] expKeyData) {
    return new DiagnosisKey(expKeyData, expRollingStartNumber,
        expRollingPeriod, expTransmissionRiskLevel, expSubmissionTimestamp);
  }

  private DiagnosisKey keyWithRollingStartNumber(long expRollingStartNumber) {
    return new DiagnosisKey(expKeyData, expRollingStartNumber,
        expRollingPeriod, expTransmissionRiskLevel, expSubmissionTimestamp);
  }

  private DiagnosisKey keyWithRollingPeriod(long expRollingPeriod) {
    return new DiagnosisKey(expKeyData, expRollingStartNumber,
        expRollingPeriod, expTransmissionRiskLevel, expSubmissionTimestamp);
  }

  private DiagnosisKey keyWithRiskLevel(int expTransmissionRiskLevel) {
    return new DiagnosisKey(expKeyData, expRollingStartNumber,
        expRollingPeriod, expTransmissionRiskLevel, expSubmissionTimestamp);
  }

  private void checkMessage(InvalidDiagnosisKeyException ex, String message) {
    assertEquals(ex.getMessage(), message);
  }

  @Test
  public void testIsYoungerThanRetentionThreshold() {
    long fiveDaysAgo = LocalDateTime
        .ofInstant(Instant.now(), UTC)
        .minusDays(5).minusMinutes(10)
        .toEpochSecond(UTC) / (60 * 10);
    DiagnosisKey diagnosisKeyFiveDays = new DiagnosisKey(expKeyData, fiveDaysAgo,
        expRollingPeriod, expTransmissionRiskLevel, expSubmissionTimestamp);

    assertFalse(diagnosisKeyFiveDays.isYoungerThanRetentionThreshold(4L));
    assertFalse(diagnosisKeyFiveDays.isYoungerThanRetentionThreshold(5L));
    assertTrue(diagnosisKeyFiveDays.isYoungerThanRetentionThreshold(6L));
  }

}
