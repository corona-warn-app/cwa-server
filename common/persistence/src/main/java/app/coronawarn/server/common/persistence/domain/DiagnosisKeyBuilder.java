

package app.coronawarn.server.common.persistence.domain;

import static app.coronawarn.server.common.persistence.domain.DiagnosisKeyBuilders.Builder;
import static app.coronawarn.server.common.persistence.domain.DiagnosisKeyBuilders.FinalBuilder;
import static app.coronawarn.server.common.persistence.domain.DiagnosisKeyBuilders.RollingStartIntervalNumberBuilder;
import static app.coronawarn.server.common.persistence.domain.DiagnosisKeyBuilders.TransmissionRiskLevelBuilder;
import static app.coronawarn.server.common.persistence.domain.validation.ValidSubmissionTimestampValidator.SECONDS_PER_HOUR;

import app.coronawarn.server.common.persistence.domain.normalization.DiagnosisKeyNormalizer;
import app.coronawarn.server.common.persistence.domain.normalization.NormalizableFields;
import app.coronawarn.server.common.persistence.exception.InvalidDiagnosisKeyException;
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import app.coronawarn.server.common.protocols.external.exposurenotification.TemporaryExposureKey;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An instance of this builder can be retrieved by calling {@link DiagnosisKey#builder()}. A {@link DiagnosisKey} can
 * then be build by either providing the required member values or by passing the respective protocol buffer object.
 */
public class DiagnosisKeyBuilder implements
    Builder, RollingStartIntervalNumberBuilder, TransmissionRiskLevelBuilder, FinalBuilder {

  private static final Logger logger = LoggerFactory.getLogger(DiagnosisKeyBuilder.class);

  private byte[] keyData;
  private int rollingStartIntervalNumber;
  private int rollingPeriod = DiagnosisKey.MAX_ROLLING_PERIOD;
  private Integer transmissionRiskLevel;
  private Long submissionTimestamp = null;
  private String countryCode;
  private Set<String> visitedCountries;
  private ReportType reportType;
  private boolean consentToFederation;
  private Integer daysSinceOnsetOfSymptoms;
  private DiagnosisKeyNormalizer fieldNormalizer;

  DiagnosisKeyBuilder() {
  }

  @Override
  public RollingStartIntervalNumberBuilder withKeyData(byte[] keyData) {
    this.keyData = keyData;
    return this;
  }

  @Override
  public TransmissionRiskLevelBuilder withRollingStartIntervalNumber(int rollingStartIntervalNumber) {
    this.rollingStartIntervalNumber = rollingStartIntervalNumber;
    return this;
  }

  @Override
  public FinalBuilder withTransmissionRiskLevel(Integer transmissionRiskLevel) {
    this.transmissionRiskLevel = transmissionRiskLevel;
    return this;
  }

  @Override
  public FinalBuilder fromTemporaryExposureKeyAndMetadata(TemporaryExposureKey protoBufObject,
      List<String> visitedCountries, String originCountry, boolean consentToFederation) {
    return this
        .withKeyData(protoBufObject.getKeyData().toByteArray())
        .withRollingStartIntervalNumber(protoBufObject.getRollingStartIntervalNumber())
        .withTransmissionRiskLevel(
            protoBufObject.hasTransmissionRiskLevel() ? protoBufObject.getTransmissionRiskLevel() : null)
        .withRollingPeriod(protoBufObject.getRollingPeriod())
        .withReportType(protoBufObject.getReportType()).withDaysSinceOnsetOfSymptoms(
            protoBufObject.hasDaysSinceOnsetOfSymptoms() ? protoBufObject.getDaysSinceOnsetOfSymptoms() : null)
        .withVisitedCountries(new HashSet<>(visitedCountries))
        .withCountryCode(originCountry)
        .withConsentToFederation(consentToFederation);
  }

  @Override
  public FinalBuilder fromFederationDiagnosisKey(
      app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKey federationDiagnosisKey) {
    return this
        .withKeyData(federationDiagnosisKey.getKeyData().toByteArray())
        .withRollingStartIntervalNumber(federationDiagnosisKey.getRollingStartIntervalNumber())
        .withTransmissionRiskLevel(federationDiagnosisKey.getTransmissionRiskLevel())
        .withRollingPeriod(federationDiagnosisKey.getRollingPeriod())
        .withCountryCode(federationDiagnosisKey.getOrigin())
        .withReportType(federationDiagnosisKey.getReportType())
        .withVisitedCountries(new HashSet<>(federationDiagnosisKey.getVisitedCountriesList()))
        .withDaysSinceOnsetOfSymptoms(federationDiagnosisKey.getDaysSinceOnsetOfSymptoms());
  }

  @Override
  public FinalBuilder withSubmissionTimestamp(long submissionTimestamp) {
    this.submissionTimestamp = submissionTimestamp;
    return this;
  }

  @Override
  public FinalBuilder withRollingPeriod(int rollingPeriod) {
    this.rollingPeriod = rollingPeriod;
    return this;
  }

  @Override
  public FinalBuilder withConsentToFederation(boolean consentToFederation) {
    this.consentToFederation = consentToFederation;
    return this;
  }

  @Override
  public FinalBuilder withCountryCode(String countryCode) {
    this.countryCode = countryCode;
    return this;
  }

  @Override
  public FinalBuilder withVisitedCountries(Set<String> visitedCountries) {
    this.visitedCountries = visitedCountries;
    return this;
  }

  @Override
  public FinalBuilder withReportType(ReportType reportType) {
    this.reportType = reportType;
    return this;
  }

  @Override
  public FinalBuilder withDaysSinceOnsetOfSymptoms(Integer daysSinceOnsetOfSymptoms) {
    this.daysSinceOnsetOfSymptoms = daysSinceOnsetOfSymptoms;
    return this;
  }

  @Override
  public FinalBuilder withFieldNormalization(DiagnosisKeyNormalizer fieldNormalizer) {
    this.fieldNormalizer = fieldNormalizer;
    return this;
  }

  @Override
  public DiagnosisKey build() {
    if (submissionTimestamp == null) {
      // hours since epoch
      submissionTimestamp = Instant.now().getEpochSecond() / SECONDS_PER_HOUR;
    }

    if (visitedCountries == null || visitedCountries.isEmpty()) {
      visitedCountries = new HashSet<>();
      visitedCountries.add(countryCode);
    }

    NormalizableFields normalizedValues = normalizeValues();

    var diagnosisKey = new DiagnosisKey(
        keyData, rollingStartIntervalNumber, rollingPeriod,
        normalizedValues.getTransmissionRiskLevel(),
        submissionTimestamp,
        consentToFederation, countryCode, enhanceVisitedCountriesWithOriginCountry(), reportType,
        normalizedValues.getDaysSinceOnsetOfSymptoms());

    return throwIfValidationFails(diagnosisKey);
  }

  private Set<String> enhanceVisitedCountriesWithOriginCountry() {
    Set<String> enhancedVisitedCountries = new HashSet<>();

    if (visitedCountries == null) {
      visitedCountries = new HashSet<>();
    }

    enhancedVisitedCountries.addAll(visitedCountries);
    enhancedVisitedCountries.add(countryCode);
    return enhancedVisitedCountries;
  }

  private DiagnosisKey throwIfValidationFails(DiagnosisKey diagnosisKey) {
    Set<ConstraintViolation<DiagnosisKey>> violations = diagnosisKey.validate();

    if (!violations.isEmpty()) {
      String violationsMessage = violations.stream()
          .map(violation -> String.format("%s Invalid Value: %s", violation.getMessage(), violation.getInvalidValue()))
          .collect(Collectors.toList()).toString();
      logger.debug(violationsMessage);
      throw new InvalidDiagnosisKeyException(violationsMessage);
    }

    return diagnosisKey;
  }

  /**
   * If a {@link DiagnosisKeyNormalizer} object was configured in this builder, apply normalization where possibile, and
   * return a container with the new values. Otherwise return a container with the original unchanged values. For boxed
   * types, primitive zero like values will be chosen if they have not been provided by the client of the builder.
   */
  private NormalizableFields normalizeValues() {
    if (fieldNormalizer != null) {
      return fieldNormalizer.normalize(NormalizableFields.of(transmissionRiskLevel, daysSinceOnsetOfSymptoms));
    }
    return NormalizableFields.of(Objects.isNull(transmissionRiskLevel) ? 0 : transmissionRiskLevel,
        Objects.isNull(daysSinceOnsetOfSymptoms) ? 0 : daysSinceOnsetOfSymptoms);
  }
}
