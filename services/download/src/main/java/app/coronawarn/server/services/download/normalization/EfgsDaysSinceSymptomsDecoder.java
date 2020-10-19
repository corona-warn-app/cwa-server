package app.coronawarn.server.services.download.normalization;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.domain.validation.DaysSinceSymptomsRangeSpecification;

/**
 * Additional information is encoded in the DSOS integer that is present in the keys downloaded from
 * EFGS:
 * <ul>
 * <li>Number of days since reference point</li>
 * <li>Reference Point/li>
 * <ul>
 * <li>Onset of symptoms</li>
 * <li>End of interval</li>
 * <li>Submission of Keys</li>
 * </ul>
 * <li>Symptom Status/li>
 * <ul>
 * <li>Symptomatic</li>
 * <li>Asymptomatic</li>
 * <li>Unknown</li>
 * </ul>
 * <li>Range/Date Type/li>
 * <ul>
 * <li>Specific date</li>
 * <li>Date range/li>
 * <li>Unknown</li>
 * </ul>
 * <li>Duration of range</li>
 * </ul>
 *
 * <p>This class encapsulates the logic to extract the given DSOS value value corresponding to "number
 * of days" described above. This number falls in the [-14..14] range, conforming to the Exposure
 * Notification spec, and will be persisted in the CWA {@link DiagnosisKey} table.
 */
public class EfgsDaysSinceSymptomsDecoder {

  /**
   * Extract the given EFGS value to an EN daysSinceOnsetSymptoms value.
   */
  public int decode(Integer originalDaysSinceSymptoms) {
    DaysSinceSymptomsRangeSpecification dsosSpec =
        DaysSinceSymptomsRangeSpecification.findRangeSpecification(originalDaysSinceSymptoms)
            .orElseThrow(() -> new IllegalArgumentException("Days since onset of symptoms value "
                + originalDaysSinceSymptoms + " cannot be decoded"));

    int decodedDsos = originalDaysSinceSymptoms - dsosSpec.computeOffset(originalDaysSinceSymptoms);
    validate(decodedDsos);
    return decodedDsos;
  }

  private void validate(int decodedDsos) {
    if (!DaysSinceSymptomsRangeSpecification.ExposureNotificationAcceptedRange
        .accept(decodedDsos)) {
      throw new DecodedDsosNotInExposureNotificationFrameworkRange(decodedDsos);
    }
  }
}
