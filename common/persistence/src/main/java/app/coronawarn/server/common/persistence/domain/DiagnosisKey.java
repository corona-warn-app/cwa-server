

package app.coronawarn.server.common.persistence.domain;

import static java.time.ZoneOffset.UTC;

import app.coronawarn.server.common.persistence.domain.DiagnosisKeyBuilders.Builder;
import app.coronawarn.server.common.persistence.domain.validation.ValidCountries;
import app.coronawarn.server.common.persistence.domain.validation.ValidCountry;
import app.coronawarn.server.common.persistence.domain.validation.ValidRollingStartIntervalNumber;
import app.coronawarn.server.common.persistence.domain.validation.ValidSubmissionTimestamp;
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.Size;
import org.hibernate.validator.constraints.Range;
import org.springframework.data.annotation.Id;

/**
 * A key generated for advertising over a window of time.
 */
public class DiagnosisKey {

  public static final long ROLLING_PERIOD_MINUTES_INTERVAL = 10;

  /**
   * According to "Setting Up an Exposure Notification Server" by Apple, exposure notification servers are expected to
   * reject any diagnosis keys that do not have a rolling period of a certain fixed value. See
   * https://developer.apple.com/documentation/exposurenotification/setting_up_an_exposure_notification_server
   */
  public static final int KEY_DATA_LENGTH = 16;
  public static final int MIN_ROLLING_PERIOD = 0;
  public static final int MAX_ROLLING_PERIOD = 144;
  public static final int MIN_DAYS_SINCE_ONSET_OF_SYMPTOMS = -14;
  public static final int MAX_DAYS_SINCE_ONSET_OF_SYMPTOMS = 4000;
  public static final int MIN_TRANSMISSION_RISK_LEVEL = 1;
  public static final int MAX_TRANSMISSION_RISK_LEVEL = 8;

  private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

  @Id
  @Size(min = KEY_DATA_LENGTH, max = KEY_DATA_LENGTH, message = "Key data must be a byte array of length "
      + KEY_DATA_LENGTH + ".")
  private final byte[] keyData;

  @ValidRollingStartIntervalNumber
  private final int rollingStartIntervalNumber;

  @Range(min = MIN_ROLLING_PERIOD, max = MAX_ROLLING_PERIOD,
      message = "Rolling period must be between " + MIN_ROLLING_PERIOD + " and " + MAX_ROLLING_PERIOD + ".")
  private final int rollingPeriod;

  @Range(min = MIN_TRANSMISSION_RISK_LEVEL, max = MAX_TRANSMISSION_RISK_LEVEL,
      message = "Risk level must be between " + MIN_TRANSMISSION_RISK_LEVEL + " and " + MAX_TRANSMISSION_RISK_LEVEL
          + ".")
  private final int transmissionRiskLevel;

  @ValidSubmissionTimestamp
  private final long submissionTimestamp;

  private final boolean consentToFederation;

  @ValidCountry
  private final String originCountry;

  @ValidCountries
  private final Set<String> visitedCountries;

  private final ReportType reportType;

  @Range(min = MIN_DAYS_SINCE_ONSET_OF_SYMPTOMS, max = MAX_DAYS_SINCE_ONSET_OF_SYMPTOMS,
      message = "Days since onset of symptoms value must be between " + MIN_DAYS_SINCE_ONSET_OF_SYMPTOMS + " and "
          + MAX_DAYS_SINCE_ONSET_OF_SYMPTOMS + ".")
  private final int daysSinceOnsetOfSymptoms;

  /**
   * Should be called by builders.
   */
  DiagnosisKey(byte[] keyData, int rollingStartIntervalNumber, int rollingPeriod,
      int transmissionRiskLevel, long submissionTimestamp,
      boolean consentToFederation, String originCountry, Set<String> visitedCountries,
      ReportType reportType, Integer daysSinceOnsetOfSymptoms) {
    this.keyData = keyData;
    this.rollingStartIntervalNumber = rollingStartIntervalNumber;
    this.rollingPeriod = rollingPeriod;
    this.transmissionRiskLevel = transmissionRiskLevel;
    this.submissionTimestamp = submissionTimestamp;
    this.consentToFederation = consentToFederation;
    this.originCountry = originCountry;
    this.visitedCountries = visitedCountries == null ? new HashSet<>() : visitedCountries;
    this.reportType = reportType;
    // Workaround to avoid exception on loading old DiagnosisKeys after migration to EFGS
    this.daysSinceOnsetOfSymptoms = daysSinceOnsetOfSymptoms == null ? 0 : daysSinceOnsetOfSymptoms;
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

  /**
   * Returns the diagnosis key.
   */
  public byte[] getKeyData() {
    return keyData;
  }

  /**
   * Returns a number describing when a key starts. It is equal to startTimeOfKeySinceEpochInSecs / (60 * 10).
   */
  public int getRollingStartIntervalNumber() {
    return rollingStartIntervalNumber;
  }

  /**
   * Returns a number describing how long a key is valid. It is expressed in increments of 10 minutes (e.g. 144 for 24
   * hours).
   */
  public int getRollingPeriod() {
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

  public boolean isConsentToFederation() {
    return consentToFederation;
  }

  public String getOriginCountry() {
    return originCountry;
  }

  public Set<String> getVisitedCountries() {
    return visitedCountries;
  }

  public ReportType getReportType() {
    return reportType;
  }

  public int getDaysSinceOnsetOfSymptoms() {
    return daysSinceOnsetOfSymptoms;
  }

  /**
   * Checks if this diagnosis key falls into the period between now, and the retention threshold.
   *
   * @param daysToRetain the number of days before a key is outdated
   * @return true, if the rolling start interval number is within the time between now, and the given days to retain
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

    return this.rollingStartIntervalNumber >= threshold;
  }

  /**
   * Gets any constraint violations that this key might incorporate.
   *
   * <p><ul>
   * <li>Risk level must be between 0 and 8
   * <li>Rolling start interval number must be greater than 0
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
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DiagnosisKey that = (DiagnosisKey) o;
    return rollingStartIntervalNumber == that.rollingStartIntervalNumber
        && rollingPeriod == that.rollingPeriod
        && transmissionRiskLevel == that.transmissionRiskLevel
        && submissionTimestamp == that.submissionTimestamp
        && Arrays.equals(keyData, that.keyData)
        && Objects.equals(originCountry, that.originCountry)
        && Objects.equals(visitedCountries, that.visitedCountries)
        && reportType == that.reportType
        && daysSinceOnsetOfSymptoms == that.daysSinceOnsetOfSymptoms;
  }

  @Override
  public int hashCode() {
    int result = Objects
        .hash(rollingStartIntervalNumber, rollingPeriod, transmissionRiskLevel, submissionTimestamp, originCountry,
            visitedCountries, reportType, daysSinceOnsetOfSymptoms);
    result = 31 * result + Arrays.hashCode(keyData);
    return result;
  }

  @Override
  public String toString() {
    return "DiagnosisKey{"
        + "keyData=HIDDEN"
        + ", rollingStartIntervalNumber=" + rollingStartIntervalNumber
        + ", rollingPeriod=" + rollingPeriod
        + ", transmissionRiskLevel=" + transmissionRiskLevel
        + ", submissionTimestamp=" + submissionTimestamp
        + ", consentToFederation=" + consentToFederation
        + ", originCountry=" + originCountry
        + ", visitedCountries=" + visitedCountries
        + ", reportType=" + reportType
        + ", daysSinceOnsetOfSymptoms=" + daysSinceOnsetOfSymptoms
        + '}';
  }
}
