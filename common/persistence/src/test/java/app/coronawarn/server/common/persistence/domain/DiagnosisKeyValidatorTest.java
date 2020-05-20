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
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import app.coronawarn.server.common.persistence.exception.InvalidDiagnosisKeyException;
import java.nio.charset.Charset;
import java.time.Instant;
import java.time.LocalDate;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;

class DiagnosisKeyValidatorTest {

  private final Charset DEFAULT_CHAR_SET = Charset.defaultCharset();

  @Test
  public void transmissionRiskLevelMustBeInRange() {

    assertKeyValidation(
        () -> DiagnosisKeyValidator.validateTransmissionRiskLevel(9),
        "Risk level 9 is not allowed. Must be between 0 and 8.");

    assertKeyValidation(
        () -> DiagnosisKeyValidator.validateTransmissionRiskLevel(-1),
        "Risk level -1 is not allowed. Must be between 0 and 8.");
  }

  @Test
  public void transmissionRiskLevelDoesNotThrowForValid() {
    Assertions.assertThatCode(
        () -> DiagnosisKeyValidator.validateTransmissionRiskLevel(0))
        .doesNotThrowAnyException();

    Assertions.assertThatCode(
        () -> DiagnosisKeyValidator.validateTransmissionRiskLevel(8))
        .doesNotThrowAnyException();
  }

  @Test
  public void rollingStartNumberDoesNotThrowForValid() {
    Assertions.assertThatCode(
        () -> DiagnosisKeyValidator.validateRollingStartNumber(4200L))
        .doesNotThrowAnyException();

    // Timestamp: 05/16/2020 @ 00:00
    Assertions.assertThatCode(
        () -> DiagnosisKeyValidator.validateRollingStartNumber(1589587200L))
        .doesNotThrowAnyException();
  }

  @Test
  public void rollingStartNumberCannotBeInFuture() {
    assertKeyValidation(
        () -> DiagnosisKeyValidator.validateRollingStartNumber(1904169600L),
        "Rolling start cannot be in the future.");

    long tomorrow = LocalDate
        .ofInstant(Instant.now(), UTC)
        .plusDays(1).atStartOfDay()
        .toEpochSecond(UTC);

    assertKeyValidation(
        () -> DiagnosisKeyValidator.validateRollingStartNumber(tomorrow),
        "Rolling start cannot be in the future.");
  }

  @Test
  public void rollingPeriodMustBeLargerThanZero() {
    assertKeyValidation(
        () -> DiagnosisKeyValidator.validateRollingPeriod(0),
        "Rolling period must be positive number, but is 0.");

    assertKeyValidation(
        () -> DiagnosisKeyValidator.validateRollingPeriod(-3L),
        "Rolling period must be positive number, but is -3.");
  }

  @Test
  public void rollingPeriodDoesNotThrowForValid() {
    Assertions.assertThatCode(
        () -> DiagnosisKeyValidator.validateRollingPeriod(144L))
        .doesNotThrowAnyException();
  }

  @Test
  public void keyDataMustHaveValidLength() {
    assertKeyValidation(
        () -> DiagnosisKeyValidator
            .validateKeyData("17--bytelongarray".getBytes(DEFAULT_CHAR_SET)),
        "Key data must be byte array of length 16, but is 17.");

    assertKeyValidation(
        () -> DiagnosisKeyValidator.validateKeyData("".getBytes(DEFAULT_CHAR_SET)),
        "Key data must be byte array of length 16, but is 0.");

    assertKeyValidation(
        () -> DiagnosisKeyValidator.validateKeyData("1".getBytes(DEFAULT_CHAR_SET)),
        "Key data must be byte array of length 16, but is 1.");
  }

  private void assertKeyValidation(ThrowingCallable throwingCallable, String message) {
    assertThat(catchThrowable(throwingCallable))
        .isInstanceOf(InvalidDiagnosisKeyException.class)
        .hasMessage(message);
  }

  @Test
  public void keyDataDoesNotThrowOnValid() {
    Assertions.assertThatCode(
        () -> DiagnosisKeyValidator.validateKeyData(
            "16-bytelongarray".getBytes(DEFAULT_CHAR_SET)))
        .doesNotThrowAnyException();
  }

}
