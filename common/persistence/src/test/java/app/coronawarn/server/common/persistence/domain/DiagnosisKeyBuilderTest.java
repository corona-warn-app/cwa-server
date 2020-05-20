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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import app.coronawarn.server.common.persistence.exception.InvalidDiagnosisKeyException;
import app.coronawarn.server.common.protocols.external.exposurenotification.Key;
import com.google.protobuf.ByteString;
import java.nio.charset.Charset;
import java.time.Instant;
import org.junit.jupiter.api.Test;

public class DiagnosisKeyBuilderTest {

  private final byte[] expKeyData = "16-bytelongarray".getBytes(Charset.defaultCharset());
  private final long expRollingStartNumber = 73800;
  private final long expRollingPeriod = 144;
  private final int expTransmissionRiskLevel = 1;
  private final long expSubmissionTimestamp = 2L;

  @Test
  public void buildFromProtoBufObjWithSubmissionTimestamp() {
    Key protoBufObj = Key
        .newBuilder()
        .setKeyData(ByteString.copyFrom(this.expKeyData))
        .setRollingStartNumber(Long.valueOf(this.expRollingStartNumber).intValue())
        .setRollingPeriod(Long.valueOf(this.expRollingPeriod).intValue())
        .setTransmissionRiskLevel(this.expTransmissionRiskLevel)
        .build();

    DiagnosisKey actDiagnosisKey = DiagnosisKey.builder()
        .fromProtoBuf(protoBufObj)
        .withSubmissionTimestamp(this.expSubmissionTimestamp)
        .build();

    assertDiagnosisKeyEquals(actDiagnosisKey, this.expSubmissionTimestamp);
  }

  @Test
  public void buildFromProtoBufObjWithoutSubmissionTimestamp() {
    Key protoBufObj = Key
        .newBuilder()
        .setKeyData(ByteString.copyFrom(this.expKeyData))
        .setRollingStartNumber(Long.valueOf(this.expRollingStartNumber).intValue())
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
  public void failsForInvalidKeyData() {
    assertKeyBuildingException("17--bytelongarray".getBytes(Charset.defaultCharset()),
        this.expRollingStartNumber, this.expRollingPeriod, this.expTransmissionRiskLevel,
        "Key data must be byte array of length 16, but is 17.");
  }

  @Test
  public void failsForInvalidRollingStartNumber() {
    assertKeyBuildingException(this.expKeyData, 0, this.expRollingPeriod,
        this.expTransmissionRiskLevel, "Rolling start number must be greater than 0.");
  }

  @Test
  public void failsForInvalidRollingPeriod() {
    assertKeyBuildingException(this.expKeyData, this.expRollingStartNumber,
        0, this.expTransmissionRiskLevel, "Rolling period must be positive number, but is 0.");
  }

  @Test
  public void failsForInvalidTransmissionRiskLevel() {
    assertKeyBuildingException(this.expKeyData, this.expRollingStartNumber,
        this.expRollingPeriod, 10, "Risk level 10 is not allowed. Must be between 0 and 8.");
  }

  private void assertKeyBuildingException(byte[] expKeyData, long expRollingStartNumber,
      long expRollingPeriod, int transmissionRiskLevel, String message) {

    Throwable actual = catchThrowable(() -> DiagnosisKey.builder()
        .withKeyData(expKeyData)
        .withRollingStartNumber(expRollingStartNumber)
        .withRollingPeriod(expRollingPeriod)
        .withTransmissionRiskLevel(transmissionRiskLevel)
        .build());

    assertThat(actual)
        .isInstanceOf(InvalidDiagnosisKeyException.class)
        .hasMessage(message);
  }


  private void assertDiagnosisKeyEquals(DiagnosisKey actDiagnosisKey) {
    assertDiagnosisKeyEquals(actDiagnosisKey, getCurrentHoursSinceEpoch());
  }

  private long getCurrentHoursSinceEpoch() {
    return Instant.now().getEpochSecond() / 3600L;
  }

  private void assertDiagnosisKeyEquals(DiagnosisKey actDiagnosisKey, long expSubmissionTimestamp) {
    assertThat(actDiagnosisKey.getSubmissionTimestamp()).isEqualTo(expSubmissionTimestamp);
    assertThat(actDiagnosisKey.getKeyData()).isEqualTo(this.expKeyData);
    assertThat(actDiagnosisKey.getRollingStartNumber()).isEqualTo(this.expRollingStartNumber);
    assertThat(actDiagnosisKey.getRollingPeriod()).isEqualTo(this.expRollingPeriod);
    assertThat(actDiagnosisKey.getTransmissionRiskLevel()).isEqualTo(this.expTransmissionRiskLevel);
  }

}
