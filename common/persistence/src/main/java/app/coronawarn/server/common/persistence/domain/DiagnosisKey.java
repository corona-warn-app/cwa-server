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

import static java.time.ZoneOffset.UTC;

import app.coronawarn.server.common.persistence.domain.DiagnosisKeyBuilders.Builder;
import app.coronawarn.server.common.persistence.domain.validation.ValidRollingStartNumber;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import org.hibernate.validator.constraints.Range;

/**
 * A key generated for advertising over a window of time.
 */
@Entity
@Table(name = "diagnosis_key")
public class DiagnosisKey {
  private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Size(min = 16, max = 16, message = "Key data must be a byte array of length 16.")
  private byte[] keyData;

  @ValidRollingStartNumber
  private long rollingStartNumber;

  @Min(value = 1L, message = "Rolling period must be greater than 0.")
  private long rollingPeriod;

  @Range(min = 0, max = 8, message = "Risk level must be between 0 and 8.")
  private int transmissionRiskLevel;

  private long submissionTimestamp;

  protected DiagnosisKey() {
  }

  /**
   * Should be called by builders.
   */
  DiagnosisKey(byte[] keyData, long rollingStartNumber, long rollingPeriod,
      int transmissionRiskLevel, long submissionTimestamp) {
    this.keyData = keyData;
    this.rollingStartNumber = rollingStartNumber;
    this.rollingPeriod = rollingPeriod;
    this.transmissionRiskLevel = transmissionRiskLevel;
    this.submissionTimestamp = submissionTimestamp;
  }

  /**
   * Returns a DiagnosisKeyBuilder instance. A {@link DiagnosisKey} can then be build by either providing the required
   * member values or by passing the respective protocol buffer object.
   *
   * @return DiagnosisKeyBuilder instance.
   */
  public static Builder builder() {
    return new DiagnosisKeyBuilder();
  }

  public Long getId() {
    return id;
  }

  /**
   * Returns the diagnosis key.
   */
  public byte[] getKeyData() {
    return keyData;
  }

  /**
   * Returns a number describing when a key starts. It is equal to startTimeOfKeySinceEpochInSecs / (60 * 10).
   */
  public long getRollingStartNumber() {
    return rollingStartNumber;
  }

  /**
   * Returns a number describing how long a key is valid. It is expressed in increments of 10 minutes (e.g. 144 for 24
   * hours).
   */
  public long getRollingPeriod() {
    return rollingPeriod;
  }

  /**
   * Returns the risk of transmission associated with the person this key came from.
   */
  public int getTransmissionRiskLevel() {
    return transmissionRiskLevel;
  }

  /**
   * Returns the timestamp associated with the submission of this {@link DiagnosisKey} as hours since epoch.
   */
  public long getSubmissionTimestamp() {
    return submissionTimestamp;
  }

  /**
   * Checks if this diagnosis key falls into the period between now, and the retention threshold.
   *
   * @param daysToRetain the number of days before a key is outdated
   * @return true, if the rolling start number is in the time span between now, and the given days to retain
   * @throws IllegalArgumentException if {@code daysToRetain} is negative.
   */
  public boolean isYoungerThanRetentionThreshold(int daysToRetain) {
    if (daysToRetain < 0) {
      throw new IllegalArgumentException("Retention threshold must be greater or equal to 0.");
    }
    long threshold = LocalDateTime
        .ofInstant(Instant.now(), UTC)
        .minusDays(daysToRetain)
        .toEpochSecond(UTC) / (60 * 10);

    return this.rollingStartNumber >= threshold;
  }

  /**
   * Gets any constraint violations that this key might incorporate.
   *
   * <p><ul>
   * <li>Risk level must be between 0 and 8
   * <li>Rolling start number must be greater than 0
   * <li>Rolling start number cannot be in the future
   * <li>Rolling period must be positive number
   * <li>Key data must be byte array of length 16
   * </ul>
   *
   * @return A set of constraint violations of this key.
   */
  public Set<ConstraintViolation<DiagnosisKey>> validate() {
    return VALIDATOR.validate(this);
  }

  @Override
  public String toString() {
    return "DiagnosisKey{"
        + "id=" + id
        + ", keyData=" + Arrays.toString(keyData)
        + ", rollingStartNumber=" + rollingStartNumber
        + ", rollingPeriod=" + rollingPeriod
        + ", transmissionRiskLevel=" + transmissionRiskLevel
        + ", submissionTimestamp=" + submissionTimestamp
        + '}';
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
    return rollingStartNumber == that.rollingStartNumber
        && rollingPeriod == that.rollingPeriod
        && transmissionRiskLevel == that.transmissionRiskLevel
        && submissionTimestamp == that.submissionTimestamp
        && Objects.equals(id, that.id)
        && Arrays.equals(keyData, that.keyData);
  }

  @Override
  public int hashCode() {
    int result = Objects
        .hash(id, rollingStartNumber, rollingPeriod, transmissionRiskLevel, submissionTimestamp);
    result = 31 * result + Arrays.hashCode(keyData);
    return result;
  }
}
