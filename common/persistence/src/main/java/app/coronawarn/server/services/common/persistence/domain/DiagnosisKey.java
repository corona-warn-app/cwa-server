package app.coronawarn.server.services.common.persistence.domain;

import java.util.Arrays;
import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

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
  private long rollingPeriod;
  private int transmissionRiskLevel;

  /**
   * Should be called by builders.
   */
  DiagnosisKey(
      byte[] keyData, long rollingStartNumber, long rollingPeriod, int transmissionRiskLevel) {
    this.keyData = keyData;
    this.rollingStartNumber = rollingStartNumber;
    this.rollingPeriod = rollingPeriod;
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
   * @return number describing how long a key is valid. It is expressed in increments of 10 minutes
   * (e.g. 144 for 24 hours).
   */
  public long getRollingPeriod() {
    return rollingPeriod;
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
    return rollingStartNumber == that.rollingStartNumber &&
        rollingPeriod == that.rollingPeriod &&
        transmissionRiskLevel == that.transmissionRiskLevel &&
        Objects.equals(id, that.id) &&
        Arrays.equals(keyData, that.keyData);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(id, rollingStartNumber, rollingPeriod, transmissionRiskLevel);
    result = 31 * result + Arrays.hashCode(keyData);
    return result;
  }
}
