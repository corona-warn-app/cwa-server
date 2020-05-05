package org.ena.server.services.common.persistence.domain;

import org.ena.server.common.protocols.generated.ExposureKeys;

/**
 * An instance of this builder can be retrieved by calling {@link DiagnosisKey#builder()}.
 *
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

  /**
   * @param keyData generated diagnosis key.
   * @return this Builder instance.
   */
  public RollingStartNumberBuilder keyData(byte[] keyData) {
    this.keyData = keyData;
    return this;
  }

  /**
   * @param rollingStartNumber number describing when a key starts. It is equal to
   *                           startTimeOfKeySinceEpochInSecs / (60 * 10).
   * @return this Builder instance.
   */
  public TransmissionRiskLevelBuilder rollingStartNumber(long rollingStartNumber) {
    this.rollingStartNumber = rollingStartNumber;
    return this;
  }

  /**
   * @param transmissionRiskLevel risk of transmission associated with the person this key came
   *                              from.
   * @return this Builder instance.
   */
  public FinalBuilder transmissionRiskLevel(int transmissionRiskLevel) {
    this.transmissionRiskLevel = transmissionRiskLevel;
    return this;
  }

  /**
   * @param protoBufObject
   * @return this Builder instance.
   */
  public DiagnosisKeyBuilder fromProtoBuf(ExposureKeys.TemporaryExposureKey protoBufObject) {
    return this;
  }

  /**
   * @return {@link DiagnosisKey} instance
   */
  public DiagnosisKey build() {
    return new DiagnosisKey(this.keyData, this.rollingStartNumber,
        this.transmissionRiskLevel);
  }
}

interface Builder {
  RollingStartNumberBuilder keyData(byte[] keyData);
  FinalBuilder fromProtoBuf(ExposureKeys.TemporaryExposureKey protoBufObject);
}

interface RollingStartNumberBuilder {
  TransmissionRiskLevelBuilder rollingStartNumber(long rollingStartNumber);
}

interface TransmissionRiskLevelBuilder {
  FinalBuilder transmissionRiskLevel(int transmissionRiskLevel);
}

interface FinalBuilder {
  DiagnosisKey build();
}
