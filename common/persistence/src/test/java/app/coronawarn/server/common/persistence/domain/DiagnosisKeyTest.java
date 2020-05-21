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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.Charset;
import java.time.Instant;
import java.time.LocalDateTime;
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
