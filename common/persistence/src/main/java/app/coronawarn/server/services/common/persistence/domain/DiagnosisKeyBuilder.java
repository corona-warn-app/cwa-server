package app.coronawarn.server.services.common.persistence.domain;

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

  public RollingStartNumberBuilder keyData(byte[] keyData) {
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

interface Builder {

  /**
   * @param keyData generated diagnosis key.
   * @return this Builder instance.
   */
  RollingStartNumberBuilder keyData(byte[] keyData);

  /**
   * @param protoBufObject ProtocolBuffer object associated with the temporary exposure key.
   * @return this Builder instance.
   */
  FinalBuilder fromProtoBuf(Key protoBufObject);
}

interface RollingStartNumberBuilder {

  /**
   * @param rollingStartNumber number describing when a key starts. It is equal to
   *                           startTimeOfKeySinceEpochInSecs / (60 * 10).
   * @return this Builder instance.
   */
  RollingPeriodBuilder withRollingStartNumber(long rollingStartNumber);
}

interface RollingPeriodBuilder {

  /**
   * @param rollingPeriod Number describing how long a key is valid. It is expressed in increments
   *                      of 10 minutes (e.g. 144 for 24 hours).
   * @return this Builder instance.
   */
  TransmissionRiskLevelBuilder withRollingPeriod(long rollingPeriod);
}

interface TransmissionRiskLevelBuilder {

  /**
   * @param transmissionRiskLevel risk of transmission associated with the person this key came
   *                              from.
   * @return this Builder instance.
   */
  FinalBuilder withTransmissionRiskLevel(int transmissionRiskLevel);
}

interface FinalBuilder {

  /**
   * @return {@link DiagnosisKey} instance
   */
  DiagnosisKey build();
}
