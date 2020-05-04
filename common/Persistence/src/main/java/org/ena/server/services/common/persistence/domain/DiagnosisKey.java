package org.ena.server.services.common.persistence.domain;

import java.util.Arrays;
import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Table;
import javax.persistence.Id;
import org.ena.server.common.protocols.generated.ExposureKeys;

/**
 * A key generated for advertising over a window of time.
 */
@Entity
@Table(name = "diagnosis_key")
public class DiagnosisKey {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * The randomly generated diagnosis key.
   */
  private byte[] keyData;

  /**
   * A number describing when a key starts. It is equal to startTimeOfKeySinceEpochInSecs / (60 *
   * 10).
   */
  private long rollingStartNumber;

  /**
   * Risk of transmission associated with the person this key came from.
   */
  private int transmissionRiskLevel;

  DiagnosisKey(byte[] keyData, long rollingStartNumber, int transmissionRiskLevel) {
  }

  /**
   * Returns a new DiagnosisKeyBuilder
   *
   * @return
   */
  public static DiagnosisKeyBuilder builder() {
    return new Builder();
  }

  public Long getId() {
    return id;
  }

  public byte[] getKeyData() {
    return keyData;
  }

  public long getRollingStartNumber() {
    return rollingStartNumber;
  }

  public int getTransmissionRiskLevel() {
    return transmissionRiskLevel;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DiagnosisKey that = (DiagnosisKey) o;
    return id == that.id &&
        rollingStartNumber == that.rollingStartNumber &&
        transmissionRiskLevel == that.transmissionRiskLevel &&
        Arrays.equals(keyData, that.keyData);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(id, rollingStartNumber, transmissionRiskLevel);
    result = 31 * result + Arrays.hashCode(keyData);
    return result;
  }

  public static class Builder implements DiagnosisKeyBuilder, RollingStartNumberBuilder,
      TransmissionRiskLevelBuilder, FinalBuilder {

    private byte[] keyData;
    private long rollingStartNumber;
    private int transmissionRiskLevel;

    private Builder() {
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

    public Builder fromProtoBuf(ExposureKeys.TemporaryExposureKey protoBufObject) {
      return this;
    }

    public DiagnosisKey build() {
      return new DiagnosisKey(this.keyData, this.rollingStartNumber,
          this.transmissionRiskLevel);
    }
  }

  public interface DiagnosisKeyBuilder {
    RollingStartNumberBuilder keyData(byte[] keyData);
    FinalBuilder fromProtoBuf(ExposureKeys.TemporaryExposureKey protoBufObject);
  }

  public interface RollingStartNumberBuilder {
    TransmissionRiskLevelBuilder rollingStartNumber(long rollingStartNumber);
  }

  public interface TransmissionRiskLevelBuilder {
    FinalBuilder transmissionRiskLevel(int transmissionRiskLevel);
  }

  public interface FinalBuilder {
    DiagnosisKey build();
  }
}
