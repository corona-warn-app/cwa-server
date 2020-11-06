package app.coronawarn.server.services.distribution.assembly.transformation;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.services.distribution.config.TransmissionRiskLevelEncoding;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * This component is used to adapt the information contained within keys coming via EFGS to the
 * requirements of the CWA client with respect to the ENF version that its using. This is needed
 * because some of the fields might carry information alligned with EFGS spec that is not fully
 * compliant with ENF spec, or simply not compliant with the needs of the CWA client (i.e we need to
 * adapt how infectiousness/transmission risk gets calculated).
 *
 * <p>One important thing to note is that these field adaptations are performed on the fly prior to
 * distribution. These changes are not persistent, such that no previous EFGS provided information
 * is lost.
 */
@Component
public class EnfParameterAdapter {

  private TransmissionRiskLevelEncoding trlEncoding;

  public EnfParameterAdapter(TransmissionRiskLevelEncoding trlEncoding) {
    this.trlEncoding = trlEncoding;
  }

  /**
   * Given the collection of keys, create another one with all original elements but with ENF
   * related content transformations applied.
   */
  public Collection<DiagnosisKey> adaptKeys(Collection<DiagnosisKey> diagnosisKeys) {
    return diagnosisKeys.stream().map(this::adapt).collect(Collectors.toList());
  }

  private DiagnosisKey adapt(DiagnosisKey diagnosisKey) {
    return DiagnosisKey.builder().withKeyData(diagnosisKey.getKeyData())
        .withRollingStartIntervalNumber(diagnosisKey.getRollingStartIntervalNumber())
        .withTransmissionRiskLevel(diagnosisKey.getTransmissionRiskLevel())
        .withRollingPeriod(diagnosisKey.getRollingPeriod())
        .withCountryCode(diagnosisKey.getOriginCountry())
        .withReportType(trlEncoding
            .getReportTypeForTransmissionRiskLevel(diagnosisKey.getTransmissionRiskLevel()))
        .withVisitedCountries(new HashSet<>(diagnosisKey.getVisitedCountries()))
        .withConsentToFederation(diagnosisKey.isConsentToFederation())
        .withDaysSinceOnsetOfSymptoms(trlEncoding
            .getDaysSinceSymptomsForTransmissionRiskLevel(diagnosisKey.getTransmissionRiskLevel()))
        .withSubmissionTimestamp(diagnosisKey.getSubmissionTimestamp()).build();
  }
}
