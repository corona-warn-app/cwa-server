/*-
 * ---license-start
 * Corona-Warn-App
 * ---
 * Copyright (C) 2020 SAP SE and all other contributors
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ---license-end
 */

package app.coronawarn.server.common.persistence.domain;

import static java.time.ZoneOffset.UTC;

import app.coronawarn.server.common.persistence.domain.DiagnosisKeyBuilders.Builder;
import app.coronawarn.server.common.persistence.domain.validation.ValidRollingStartIntervalNumber;
import app.coronawarn.server.common.persistence.domain.validation.ValidSubmissionTimestamp;
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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

  /**
   * According to "Setting Up an Exposure Notification Server" by Apple, exposure notification servers are expected to
   * reject any diagnosis keys that do not have a rolling period of a certain fixed value. See
   * https://developer.apple.com/documentation/exposurenotification/setting_up_an_exposure_notification_server
   */
  public static final int MIN_ROLLING_PERIOD = 0;
  public static final int MAX_ROLLING_PERIOD = 144;

  private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

  @Id
  @Size(min = 16, max = 16, message = "Key data must be a byte array of length 16.")
  private final byte[] keyData;

  @ValidRollingStartIntervalNumber
  private final int rollingStartIntervalNumber;

  @Range(min = MIN_ROLLING_PERIOD, max = MAX_ROLLING_PERIOD,
      message = "Rolling period must be between " + MIN_ROLLING_PERIOD + " and " + MAX_ROLLING_PERIOD + ".")
  private final int rollingPeriod;

  @Range(min = 0, max = 8, message = "Risk level must be between 0 and 8.")
  private final int transmissionRiskLevel;

  @ValidSubmissionTimestamp
  private final long submissionTimestamp;

  private final boolean consentToFederation;

  @Size(max = 2)
  private final String originCountry;

  private final List<String> visitedCountries;

  private final ReportType reportType;

  private final int daysSinceOnsetOfSymptoms;

  /**
   * Should be called by builders.
   */
  DiagnosisKey(byte[] keyData, int rollingStartIntervalNumber, int rollingPeriod,
      int transmissionRiskLevel, long submissionTimestamp,
      boolean consentToFederation, @Size String originCountry, List<String> visitedCountries,
      ReportType reportType, int daysSinceOnsetOfSymptoms) {
    this.keyData = keyData;
    this.rollingStartIntervalNumber = rollingStartIntervalNumber;
    this.rollingPeriod = rollingPeriod;
    this.transmissionRiskLevel = transmissionRiskLevel;
    this.submissionTimestamp = submissionTimestamp;
    this.consentToFederation = consentToFederation;
    this.originCountry = originCountry;
    this.visitedCountries = visitedCountries == null ? Collections.emptyList() : visitedCountries;
    this.reportType = reportType;
    this.daysSinceOnsetOfSymptoms = daysSinceOnsetOfSymptoms;
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

  public List<String> getVisitedCountries() {
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
}
