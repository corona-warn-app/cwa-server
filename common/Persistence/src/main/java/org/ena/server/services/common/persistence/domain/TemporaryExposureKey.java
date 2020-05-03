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
@Table(name = "exposure_keys")
public class TemporaryExposureKey {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * The randomly generated Temporary Exposure Key information.
   */
  private byte[] keyData;

  /**
   * A number describing when a key starts. It is equal to startTimeOfKeySinceEpochInSecs / (60 *
   * 10).
   */
  private long rollingStartNumber;

  /**
   * A number describing how long a key is valid. It is expressed in increments of 10 minutes (e.g.
   * 144 for 24 hrs).
   */
  private long rollingDuration;

  /**
   * Risk of transmission associated with the person this key came from.
   */
  private int transmissionRiskLevel;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public byte[] getKeyData() {
    return keyData;
  }

  public void setKeyData(byte[] keyData) {
    this.keyData = keyData;
  }

  public long getRollingStartNumber() {
    return rollingStartNumber;
  }

  public void setRollingStartNumber(long rollingStartNumber) {
    this.rollingStartNumber = rollingStartNumber;
  }

  public long getRollingDuration() {
    return rollingDuration;
  }

  public void setRollingDuration(long rollingDuration) {
    this.rollingDuration = rollingDuration;
  }

  public int getTransmissionRiskLevel() {
    return transmissionRiskLevel;
  }

  public void setTransmissionRiskLevel(int transmissionRiskLevel) {
    this.transmissionRiskLevel = transmissionRiskLevel;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TemporaryExposureKey that = (TemporaryExposureKey) o;
    return id == that.id &&
        rollingStartNumber == that.rollingStartNumber &&
        rollingDuration == that.rollingDuration &&
        transmissionRiskLevel == that.transmissionRiskLevel &&
        Arrays.equals(keyData, that.keyData);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(id, rollingStartNumber, rollingDuration, transmissionRiskLevel);
    result = 31 * result + Arrays.hashCode(keyData);
    return result;
  }
}
