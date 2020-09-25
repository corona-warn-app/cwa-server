package app.coronawarn.server.services.download.normalization;


import app.coronawarn.server.common.persistence.domain.normalization.DiagnosisKeyNormalizer;
import app.coronawarn.server.common.persistence.domain.normalization.NormalizableFields;
import java.util.Map;

/**
 * This class is used to derive days since onset of symptoms.
 */
public class FederationKeyNormalizer implements DiagnosisKeyNormalizer {

  private final Map<Integer, Integer> dsosAndTrl;

  /**
   * Constructor for this class.
   *
   * @param dsosAndTrl A map containing integer key-pair values of days since onset of symptoms and transmission risk
   *                   level.
   */
  public FederationKeyNormalizer(Map<Integer, Integer> dsosAndTrl) {
    this.dsosAndTrl = dsosAndTrl;
  }

  @Override
  public NormalizableFields normalize(NormalizableFields fieldsAndValues) {
    validateNormalizableFields(fieldsAndValues);
    int trl = dsosAndTrl.getOrDefault(fieldsAndValues.getDaysSinceOnsetOfSymptoms(), 1);
    return NormalizableFields.of(trl, fieldsAndValues.getDaysSinceOnsetOfSymptoms());
  }

  private void validateNormalizableFields(NormalizableFields fieldsAndValues) {
    if (fieldsAndValues.getDaysSinceOnsetOfSymptoms() == null) {
      throw new IllegalArgumentException("Days since onset of symptoms is missing!");
    }
  }

}
