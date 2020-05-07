package app.coronawarn.server.services.common.persistence.domain;

import app.coronawarn.server.common.protocols.generated.ExposureKeys.TemporaryExposureKey;

/**
 * An instance of this builder can be retrieved by calling {@link DiagnosisKey#builder()}.
 * A {@link DiagnosisKey} can then be build by either providing the required member values or by
 * passing the respective protocol buffer object.
 */
public class DiagnosisKeyBuilder implements Builder, RollingStartNumberBuilder,
    TransmissionRiskLevelBuilder, FinalBuilder {
  private byte[] keyData;
  private long rollingStartNumber;
  private int transmissionRiskLevel;

  DiagnosisKeyBuilder() {
  }

  public RollingStartNumberBuilder keyData(byte[] keyData) {
    this.keyData = keyData;
    return this;
  }

  public TransmissionRiskLevelBuilder rollingStartNumber(long rollingStartNumber) {
    this.rollingStartNumber = rollingStartNumber;
    return this;
  }

  public FinalBuilder transmissionRiskLevel(int transmissionRiskLevel) {
    this.transmissionRiskLevel = transmissionRiskLevel;
    return this;
  }

  public DiagnosisKeyBuilder fromProtoBuf(TemporaryExposureKey protoBufObject) {
    this.keyData = protoBufObject.getKeyData().toByteArray();
    this.rollingStartNumber = protoBufObject.getRollingStartNumber();
    this.transmissionRiskLevel = protoBufObject.getRiskLevelValue();
    return this;
  }

  public DiagnosisKey build() {
    return new DiagnosisKey(this.keyData, this.rollingStartNumber,
        this.transmissionRiskLevel);
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
  FinalBuilder fromProtoBuf(TemporaryExposureKey protoBufObject);
}

interface RollingStartNumberBuilder {

  /**
   * @param rollingStartNumber number describing when a key starts. It is equal to
   *                           startTimeOfKeySinceEpochInSecs / (60 * 10).
   * @return this Builder instance.
   */
  TransmissionRiskLevelBuilder rollingStartNumber(long rollingStartNumber);
}

interface TransmissionRiskLevelBuilder {

  /**
   * @param transmissionRiskLevel risk of transmission associated with the person this key came
   *                              from.
   * @return this Builder instance.
   */
  FinalBuilder transmissionRiskLevel(int transmissionRiskLevel);
}

interface FinalBuilder {

  /**
   * @return {@link DiagnosisKey} instance
   */
  DiagnosisKey build();
}
