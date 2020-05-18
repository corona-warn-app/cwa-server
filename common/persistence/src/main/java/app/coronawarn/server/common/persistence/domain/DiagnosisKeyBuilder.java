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

import static app.coronawarn.server.common.persistence.domain.DiagnosisKeyBuilders.Builder;
import static app.coronawarn.server.common.persistence.domain.DiagnosisKeyBuilders.FinalBuilder;
import static app.coronawarn.server.common.persistence.domain.DiagnosisKeyBuilders.RollingPeriodBuilder;
import static app.coronawarn.server.common.persistence.domain.DiagnosisKeyBuilders.RollingStartNumberBuilder;
import static app.coronawarn.server.common.persistence.domain.DiagnosisKeyBuilders.TransmissionRiskLevelBuilder;

import app.coronawarn.server.common.protocols.external.exposurenotification.Key;
import java.time.Instant;

/**
 * An instance of this builder can be retrieved by calling {@link DiagnosisKey#builder()}. A {@link DiagnosisKey} can
 * then be build by either providing the required member values or by passing the respective protocol buffer object.
 */
public class DiagnosisKeyBuilder implements Builder, RollingStartNumberBuilder,
    RollingPeriodBuilder, TransmissionRiskLevelBuilder, FinalBuilder {

  private byte[] keyData;
  private long rollingStartNumber;
  private long rollingPeriod;
  private int transmissionRiskLevel;
  private long submissionTimestamp = -1L;

  DiagnosisKeyBuilder() {
  }

  @Override
  public RollingStartNumberBuilder withKeyData(byte[] keyData) {
    DiagnosisKeyValidator.validateKeyData(keyData);
    this.keyData = keyData;
    return this;
  }

  @Override
  public RollingPeriodBuilder withRollingStartNumber(long rollingStartNumber) {
    DiagnosisKeyValidator.validateRollingStartNumber(rollingStartNumber);
    this.rollingStartNumber = rollingStartNumber;
    return this;
  }

  @Override
  public TransmissionRiskLevelBuilder withRollingPeriod(long rollingPeriod) {
    DiagnosisKeyValidator.validateRollingPeriod(rollingPeriod);
    this.rollingPeriod = rollingPeriod;
    return this;
  }

  @Override
  public FinalBuilder withTransmissionRiskLevel(int transmissionRiskLevel) {
    DiagnosisKeyValidator.validateTransmissionRiskLevel(transmissionRiskLevel);
    this.transmissionRiskLevel = transmissionRiskLevel;
    return this;
  }

  @Override
  public FinalBuilder fromProtoBuf(Key protoBufObject) {
    return this
        .withKeyData(protoBufObject.getKeyData().toByteArray())
        .withRollingStartNumber(protoBufObject.getRollingStartNumber())
        .withRollingPeriod(protoBufObject.getRollingPeriod())
        .withTransmissionRiskLevel(protoBufObject.getTransmissionRiskLevel());
  }

  @Override
  public FinalBuilder withSubmissionTimestamp(long submissionTimestamp) {
    this.submissionTimestamp = submissionTimestamp;
    return this;
  }

  @Override
  public DiagnosisKey build() {
    if (submissionTimestamp < 0) {
      // hours since epoch
      submissionTimestamp = Instant.now().getEpochSecond() / 3600L;
    }

    return new DiagnosisKey(this.keyData, this.rollingStartNumber, this.rollingPeriod,
        this.transmissionRiskLevel, submissionTimestamp);
  }
}
