package app.coronawarn.server.common.persistence.domain;

import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload.SubmissionType;
import java.util.Objects;
import java.util.Set;

/**
 * This entity is mapped to a table which serves as data source for the uploading of diagnosis keys to the Federation
 * Gateway Service. The underlying table is filled by replicating diagnosis keys on insert (currently via database
 * triggers).
 *
 * <p>The entity is almost equivalent to the {@link DiagnosisKey} in content, but carries some additional specific
 * metadata, and could diverge in the future.
 */
public class FederationUploadKey extends DiagnosisKey {

  /**
   * Create a new instance of an upload key by copying the properties of the given source diagnosis key.
   *
   * @param diagnosisKeySource the {@link DiagnosisKey}
   * @return FederationUploadKey new {@link FederationUploadKey} created from DiagnosisKey
   */
  public static FederationUploadKey from(final DiagnosisKey diagnosisKeySource) {
    return new FederationUploadKey(diagnosisKeySource.getKeyData(), diagnosisKeySource.getSubmissionType(),
        diagnosisKeySource.getRollingStartIntervalNumber(), diagnosisKeySource.getRollingPeriod(),
        diagnosisKeySource.getTransmissionRiskLevel(), diagnosisKeySource.getSubmissionTimestamp(),
        diagnosisKeySource.isConsentToFederation(), diagnosisKeySource.getOriginCountry(),
        diagnosisKeySource.getVisitedCountries(), diagnosisKeySource.getReportType(),
        diagnosisKeySource.getDaysSinceOnsetOfSymptoms(), null);
  }

  private String batchTag;

  /**
   * Constructor.
   * 
   * @param keyData - keyData
   * @param submissionType - submissionType
   * @param rollingStartIntervalNumber - rollingStartIntervalNumber
   * @param rollingPeriod - rollingPeriod
   * @param transmissionRiskLevel - transmissionRiskLevel
   * @param submissionTimestamp - submissionTimestamp
   * @param consentToFederation - consentToFederation
   * @param originCountry - originCountry
   * @param visitedCountries - visitedCountries
   * @param reportType - reportType
   * @param daysSinceOnsetOfSymptoms - daysSinceOnsetOfSymptoms
   * @param batchTag - batchTag
   */
  public FederationUploadKey(final byte[] keyData, final SubmissionType submissionType,
      final int rollingStartIntervalNumber, final int rollingPeriod, final int transmissionRiskLevel,
      final long submissionTimestamp, final boolean consentToFederation, final String originCountry,
      final Set<String> visitedCountries, final ReportType reportType, final Integer daysSinceOnsetOfSymptoms,
      final String batchTag) {
    super(keyData, submissionType, rollingStartIntervalNumber, rollingPeriod, transmissionRiskLevel,
        submissionTimestamp, consentToFederation, originCountry, visitedCountries, reportType,
        daysSinceOnsetOfSymptoms);
    this.batchTag = batchTag;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final FederationUploadKey that = (FederationUploadKey) o;
    return super.equals(o) && Objects.equals(batchTag, that.batchTag);
  }

  public String getBatchTag() {
    return batchTag;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), batchTag);
  }

  public void setBatchTag(final String batchTag) {
    this.batchTag = batchTag;
  }

  @Override
  public String toString() {
    return "FederationUploadKey [BatchTag=" + getBatchTag() + ", SubmissionType=" + getSubmissionType()
        + ", RollingStartIntervalNumber=" + getRollingStartIntervalNumber() + ", RollingPeriod=" + getRollingPeriod()
        + ", TransmissionRiskLevel=" + getTransmissionRiskLevel() + ", SubmissionTimestamp=" + getSubmissionTimestamp()
        + ", isConsentToFederation=" + isConsentToFederation() + ", OriginCountry=" + getOriginCountry()
        + ", VisitedCountries=" + getVisitedCountries() + ", ReportType=" + getReportType()
        + ", DaysSinceOnsetOfSymptoms=" + getDaysSinceOnsetOfSymptoms() + "]";
  }
}
