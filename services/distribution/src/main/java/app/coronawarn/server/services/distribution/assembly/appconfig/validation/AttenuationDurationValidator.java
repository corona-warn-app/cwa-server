

package app.coronawarn.server.services.distribution.assembly.appconfig.validation;

import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.ParameterSpec.ATTENUATION_DURATION_THRESHOLD_MAX;
import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.ParameterSpec.ATTENUATION_DURATION_THRESHOLD_MIN;
import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.ParameterSpec.ATTENUATION_DURATION_WEIGHT_MAX;
import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.ParameterSpec.ATTENUATION_DURATION_WEIGHT_MAX_DECIMALS;
import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.ParameterSpec.ATTENUATION_DURATION_WEIGHT_MIN;
import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.ParameterSpec.DEFAULT_BUCKET_OFFSET_MAX;
import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.ParameterSpec.DEFAULT_BUCKET_OFFSET_MIN;
import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.ParameterSpec.RISK_SCORE_NORMALIZATION_DIVISOR_MAX;
import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.ParameterSpec.RISK_SCORE_NORMALIZATION_DIVISOR_MIN;
import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.ValidationError.ErrorType.MIN_GREATER_THAN_MAX;
import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.ValidationError.ErrorType.TOO_MANY_DECIMAL_PLACES;
import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.ValidationError.ErrorType.VALUE_OUT_OF_BOUNDS;

import app.coronawarn.server.common.protocols.internal.AttenuationDuration;
import java.math.BigDecimal;

/**
 * The AttenuationDurationValidator validates the values of an associated {@link AttenuationDuration} instance.
 */
public class AttenuationDurationValidator extends ConfigurationValidator {

  public static final String CONFIG_PREFIX = "attenuation-duration.";

  private final AttenuationDuration attenuationDuration;

  public AttenuationDurationValidator(AttenuationDuration attenuationDuration) {
    this.attenuationDuration = attenuationDuration;
  }

  @Override
  public ValidationResult validate() {
    errors = new ValidationResult();

    validateThresholds();
    validateWeights();
    validateDefaultBucketOffset();
    validateRiskScoreNormalizationDivisor();

    return errors;
  }

  private void validateThresholds() {
    int lower = attenuationDuration.getThresholds().getLower();
    int upper = attenuationDuration.getThresholds().getUpper();

    checkValueRange("thresholds.lower", lower, ATTENUATION_DURATION_THRESHOLD_MIN, ATTENUATION_DURATION_THRESHOLD_MAX);
    checkValueRange("thresholds.upper", upper, ATTENUATION_DURATION_THRESHOLD_MIN, ATTENUATION_DURATION_THRESHOLD_MAX);

    if (lower > upper) {
      String parameters = CONFIG_PREFIX + "thresholds.[lower + upper]";
      String values = lower + ", " + upper;
      this.errors.add(new ValidationError(parameters, values, MIN_GREATER_THAN_MAX));
    }
  }

  private void checkValueRange(String parameterLabel, int value, int min, int max) {
    if (value < min || value > max) {
      this.errors.add(new ValidationError(
          CONFIG_PREFIX + parameterLabel, value, VALUE_OUT_OF_BOUNDS));
    }
  }

  private void validateWeights() {
    checkWeight("low", attenuationDuration.getWeights().getLow());
    checkWeight("mid", attenuationDuration.getWeights().getMid());
    checkWeight("high", attenuationDuration.getWeights().getHigh());
  }

  private void checkWeight(String weightLabel, double weightValue) {
    if (weightValue < ATTENUATION_DURATION_WEIGHT_MIN || weightValue > ATTENUATION_DURATION_WEIGHT_MAX) {
      this.errors.add(new ValidationError(
          CONFIG_PREFIX + "weights." + weightLabel, weightValue, VALUE_OUT_OF_BOUNDS));
    }

    if (BigDecimal.valueOf(weightValue).scale() > ATTENUATION_DURATION_WEIGHT_MAX_DECIMALS) {
      this.errors.add(new ValidationError(
          CONFIG_PREFIX + "weights." + weightLabel, weightValue, TOO_MANY_DECIMAL_PLACES));
    }
  }

  private void validateDefaultBucketOffset() {
    int bucketOffset = attenuationDuration.getDefaultBucketOffset();
    checkValueRange("default-bucket-offset", bucketOffset, DEFAULT_BUCKET_OFFSET_MIN, DEFAULT_BUCKET_OFFSET_MAX);
  }

  private void validateRiskScoreNormalizationDivisor() {
    int riskScoreNormalizationDivisor = attenuationDuration.getRiskScoreNormalizationDivisor();
    checkValueRange("risk-score-normalization-divisor", riskScoreNormalizationDivisor,
        RISK_SCORE_NORMALIZATION_DIVISOR_MIN, RISK_SCORE_NORMALIZATION_DIVISOR_MAX);
  }
}
