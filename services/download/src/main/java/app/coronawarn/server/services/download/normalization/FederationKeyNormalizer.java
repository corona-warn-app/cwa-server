package app.coronawarn.server.services.download.normalization;

import app.coronawarn.server.common.persistence.domain.normalization.DiagnosisKeyNormalizer;
import app.coronawarn.server.common.persistence.domain.normalization.NormalizableFields;
import java.util.Map;

public class FederationKeyNormalizer implements DiagnosisKeyNormalizer {

  private final Map<Integer, Integer> dsosAndTrl;

  /**
   * @param dsosAndTrl A map containing integer key-pair values of days since onset of symptoms and transmission risk
   *                   level.
   */
  public FederationKeyNormalizer(Map<Integer, Integer> dsosAndTrl) {
    this.dsosAndTrl = dsosAndTrl;
  }

  @Override
  public NormalizableFields normalize(NormalizableFields fieldsAndValues) {

    int trl = dsosAndTrl.getOrDefault(fieldsAndValues.getDaysSinceOnsetOfSymptoms(), 1);
    return NormalizableFields.of(trl,
        fieldsAndValues.getDaysSinceOnsetOfSymptoms());
  }

}
