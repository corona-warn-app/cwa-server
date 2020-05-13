package app.coronawarn.server.common.persistence.domain;

import static app.coronawarn.server.common.persistence.domain.DiagnosisKeyBuilders.Builder;
import static app.coronawarn.server.common.persistence.domain.DiagnosisKeyBuilders.FinalBuilder;
import static app.coronawarn.server.common.persistence.domain.DiagnosisKeyBuilders.RollingPeriodBuilder;
import static app.coronawarn.server.common.persistence.domain.DiagnosisKeyBuilders.RollingStartNumberBuilder;
import static app.coronawarn.server.common.persistence.domain.DiagnosisKeyBuilders.TransmissionRiskLevelBuilder;

import app.coronawarn.server.common.protocols.external.exposurenotification.Key;

/**
 * An instance of this builder can be retrieved by calling {@link DiagnosisKey#builder()}. A {@link
 * DiagnosisKey} can then be build by either providing the required member values or by passing the
 * respective protocol buffer object.
 */
public class DiagnosisKeyBuilder implements Builder, RollingStartNumberBuilder,
    RollingPeriodBuilder, TransmissionRiskLevelBuilder, FinalBuilder {

  private byte[] keyData;
  private long rollingStartNumber;
  private long rollingPeriod;
  private int transmissionRiskLevel;

  DiagnosisKeyBuilder() {
  }

  public RollingStartNumberBuilder withKeyData(byte[] keyData) {
    this.keyData = keyData;
    return this;
  }

  public RollingPeriodBuilder withRollingStartNumber(long rollingStartNumber) {
    this.rollingStartNumber = rollingStartNumber;
    return this;
  }

  public TransmissionRiskLevelBuilder withRollingPeriod(long rollingPeriod) {
    this.rollingPeriod = rollingPeriod;
    return this;
  }

  public FinalBuilder withTransmissionRiskLevel(int transmissionRiskLevel) {
    this.transmissionRiskLevel = transmissionRiskLevel;
    return this;
  }

  public DiagnosisKeyBuilder fromProtoBuf(Key protoBufObject) {
    this.keyData = protoBufObject.getKeyData().toByteArray();
    this.rollingStartNumber = protoBufObject.getRollingStartNumber();
    this.rollingPeriod = protoBufObject.getRollingPeriod();
    this.transmissionRiskLevel = protoBufObject.getTransmissionRiskLevel();
    return this;
  }

  public DiagnosisKey build() {
    return new DiagnosisKey(
        this.keyData, this.rollingStartNumber, this.rollingPeriod, this.transmissionRiskLevel);
  }
}
