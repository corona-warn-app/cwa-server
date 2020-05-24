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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.Charset;
import java.time.Instant;
import java.time.LocalDateTime;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.*;


class DiagnosisKeyTest {

  final static byte[] expKeyData = "testKey111111111".getBytes(Charset.defaultCharset());
  final static int expRollingStartIntervalNumber = 1;
  final static int expRollingPeriod = 2;
  final static int expTransmissionRiskLevel = 3;
  final static long expSubmissionTimestamp = 4L;
  final static DiagnosisKey diagnosisKey = new DiagnosisKey(expKeyData, expRollingStartIntervalNumber,
      expRollingPeriod, expTransmissionRiskLevel, expSubmissionTimestamp);

  @Test
<<<<<<< HEAD
  void testRollingStartNumberGetter() {
    assertThat(diagnosisKey.getRollingStartNumber()).isEqualTo(expRollingStartNumber);
=======
  public void testRollingStartNumberGetter() {
    assertThat(diagnosisKey.getRollingStartNumber()).isEqualTo(expRollingStartIntervalNumber);
>>>>>>> Variable name and type changed
  }

  @Test
  void testRollingPeriodGetter() {
    assertThat(diagnosisKey.getRollingPeriod()).isEqualTo(expRollingPeriod);
  }

  @Test
  void testTransmissionRiskLevelGetter() {
    assertThat(diagnosisKey.getTransmissionRiskLevel()).isEqualTo(expTransmissionRiskLevel);
  }

  @Test
  void testSubmissionTimestampGetter() {
    assertThat(diagnosisKey.getSubmissionTimestamp()).isEqualTo(expSubmissionTimestamp);
  }

  @Test
<<<<<<< HEAD
  void testIsYoungerThanRetentionThreshold() {
    long fiveDaysAgo = LocalDateTime
        .ofInstant(Instant.now(), UTC)
        .minusDays(5).minusMinutes(10)
        .toEpochSecond(UTC) / (60 * 10);
=======
  public void testIsYoungerThanRetentionThreshold() {
    int fiveDaysAgo = (int) (LocalDateTime
            .ofInstant(Instant.now(), UTC)
            .minusDays(5).minusMinutes(10)
            .toEpochSecond(UTC) / (60 * 10));
>>>>>>> Variable name and type changed
    DiagnosisKey diagnosisKeyFiveDays = new DiagnosisKey(expKeyData, fiveDaysAgo,
        expRollingPeriod, expTransmissionRiskLevel, expSubmissionTimestamp);

    assertThat(diagnosisKeyFiveDays.isYoungerThanRetentionThreshold(4)).isFalse();
    assertThat(diagnosisKeyFiveDays.isYoungerThanRetentionThreshold(5)).isFalse();
    assertThat(diagnosisKeyFiveDays.isYoungerThanRetentionThreshold(6)).isTrue();
  }

  @DisplayName("Test retention threshold accepts positive value")
  @ValueSource(ints = {0, 1, Integer.MAX_VALUE})
  @ParameterizedTest
  void testRetentionThresholdAcceptsPositiveValue(int daysToRetain) {
    assertThatCode(() -> diagnosisKey.isYoungerThanRetentionThreshold(daysToRetain))
        .doesNotThrowAnyException();
  }

  @DisplayName("Test retention threshold rejects negative value")
  @ValueSource(ints = {Integer.MIN_VALUE, -1})
  @ParameterizedTest
  void testRetentionThresholdRejectsNegativeValue(int daysToRetain) {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> diagnosisKey.isYoungerThanRetentionThreshold(daysToRetain));
  }
}
