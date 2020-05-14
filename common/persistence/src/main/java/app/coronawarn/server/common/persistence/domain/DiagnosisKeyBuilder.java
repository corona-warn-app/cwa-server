package app.coronawarn.server.common.persistence.domain;

import static app.coronawarn.server.common.persistence.domain.DiagnosisKeyBuilders.Builder;
import static app.coronawarn.server.common.persistence.domain.DiagnosisKeyBuilders.FinalBuilder;
import static app.coronawarn.server.common.persistence.domain.DiagnosisKeyBuilders.RollingPeriodBuilder;
import static app.coronawarn.server.common.persistence.domain.DiagnosisKeyBuilders.RollingStartNumberBuilder;
import static app.coronawarn.server.common.persistence.domain.DiagnosisKeyBuilders.TransmissionRiskLevelBuilder;

import app.coronawarn.server.common.protocols.external.exposurenotification.Key;
import java.time.Instant;

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
  private long submissionTimestamp = -1L;

  DiagnosisKeyBuilder() {
  }

  @Override
  public RollingStartNumberBuilder withKeyData(byte[] keyData) {
    this.keyData = keyData;
    return this;
  }

  @Override
  public RollingPeriodBuilder withRollingStartNumber(long rollingStartNumber) {
    this.rollingStartNumber = rollingStartNumber;
    return this;
  }

  @Override
  public TransmissionRiskLevelBuilder withRollingPeriod(long rollingPeriod) {
    this.rollingPeriod = rollingPeriod;
    return this;
  }

  @Override
  public FinalBuilder withTransmissionRiskLevel(int transmissionRiskLevel) {
    this.transmissionRiskLevel = transmissionRiskLevel;
    return this;
  }

  @Override
  public DiagnosisKeyBuilder fromProtoBuf(Key protoBufObject) {
    this.keyData = protoBufObject.getKeyData().toByteArray();
    this.rollingStartNumber = protoBufObject.getRollingStartNumber();
    this.rollingPeriod = protoBufObject.getRollingPeriod();
    this.transmissionRiskLevel = protoBufObject.getTransmissionRiskLevel();
    return this;
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
