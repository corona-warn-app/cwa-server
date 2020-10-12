

package app.coronawarn.server.services.download.normalization;


import app.coronawarn.server.common.persistence.domain.config.TekFieldDerivations;
import app.coronawarn.server.common.persistence.domain.normalization.DiagnosisKeyNormalizer;
import app.coronawarn.server.common.persistence.domain.normalization.NormalizableFields;
import app.coronawarn.server.services.download.config.DownloadServiceConfig;

/**
 * This class is used to derive transmission risk level using the days since onset of symptoms.
 */
public class FederationKeyNormalizer implements DiagnosisKeyNormalizer {

  private final TekFieldDerivations tekFieldDerivations;

  /**
   * Constructor for this class.
   *
   * @param config A {@link DownloadServiceConfig} object.
   */
  public FederationKeyNormalizer(DownloadServiceConfig config) {
    this.tekFieldDerivations = config.getTekFieldDerivations();
  }

  @Override
  public NormalizableFields normalize(NormalizableFields fieldsAndValues) {
    validateNormalizableFields(fieldsAndValues);
    int trl = tekFieldDerivations.deriveTransmissionRiskLevelFromDaysSinceSymptoms(
        fieldsAndValues.getDaysSinceOnsetOfSymptoms());
    return NormalizableFields.of(trl, fieldsAndValues.getDaysSinceOnsetOfSymptoms());
  }

  private void validateNormalizableFields(NormalizableFields fieldsAndValues) {
    if (fieldsAndValues.getDaysSinceOnsetOfSymptoms() == null) {
      throw new IllegalArgumentException("Days since onset of symptoms is missing!");
    }
  }

}
