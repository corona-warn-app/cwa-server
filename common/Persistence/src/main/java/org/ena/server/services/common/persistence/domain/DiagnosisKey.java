package org.ena.server.services.common.persistence.domain;

import java.util.Arrays;
import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Table;
import javax.persistence.Id;

/**
 * A key generated for advertising over a window of time.
 */
@Entity
@Table(name = "diagnosis_key")
public class DiagnosisKey {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private byte[] keyData;
  private long rollingStartNumber;
  private int transmissionRiskLevel;
  //TODO add creation date

  /**
   * Should be called by builders.
   */
  DiagnosisKey(byte[] keyData, long rollingStartNumber, int transmissionRiskLevel) {
    this.keyData = keyData;
    this.rollingStartNumber = rollingStartNumber;
    this.transmissionRiskLevel = transmissionRiskLevel;
  }

  /**
   * Returns a DiagnosisKeyBuilder instance. A {@link DiagnosisKey} can then be build by either
   * providing the required member values or by passing the respective protocol buffer object.
   *
   * @return DiagnosisKeyBuilder instance.
   */
  public static DiagnosisKeyBuilder builder() {
    return new DiagnosisKeyBuilder();
  }

  public Long getId() {
    return id;
  }

  /**
   * @return generated diagnosis key.
   */
  public byte[] getKeyData() {
    return keyData;
  }

  /**
   * @return number describing when a key starts. It is equal to startTimeOfKeySinceEpochInSecs /
   * (60 * 10).
   */
  public long getRollingStartNumber() {
    return rollingStartNumber;
  }

  /**
   * @return risk of transmission associated with the person this key came from.
   */
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
}
