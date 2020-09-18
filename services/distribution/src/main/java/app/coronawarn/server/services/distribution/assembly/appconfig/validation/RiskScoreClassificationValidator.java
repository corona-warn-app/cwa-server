

package app.coronawarn.server.services.distribution.assembly.appconfig.validation;

import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.ValidationError.ErrorType.BLANK_LABEL;
import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.ValidationError.ErrorType.INVALID_PARTITIONING;
import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.ValidationError.ErrorType.INVALID_URL;
import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.ValidationError.ErrorType.MIN_GREATER_THAN_MAX;
import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.ValidationError.ErrorType.VALUE_OUT_OF_BOUNDS;

import app.coronawarn.server.common.protocols.internal.RiskScoreClass;
import app.coronawarn.server.common.protocols.internal.RiskScoreClassification;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * The RiskScoreClassificationValidator validates the values of an associated {@link RiskScoreClassification} instance.
 */
public class RiskScoreClassificationValidator extends ConfigurationValidator {

  public static final String CONFIG_PREFIX = "risk-score-classes.";

  private final RiskScoreClassification riskScoreClassification;

  public RiskScoreClassificationValidator(RiskScoreClassification riskScoreClassification) {
    this.riskScoreClassification = riskScoreClassification;
  }

  /**
   * Performs a validation of the associated {@link RiskScoreClassification} instance and returns information about
   * validation failures.
   *
   * @return The ValidationResult instance, containing information about possible errors.
   */
  @Override
  public ValidationResult validate() {
    errors = new ValidationResult();

    validateValues();
    validateValueRangeCoverage();

    return errors;
  }

  private void validateValues() {
    for (RiskScoreClass riskScoreClass : riskScoreClassification.getRiskClassesList()) {
      int minRiskLevel = riskScoreClass.getMin();
      int maxRiskLevel = riskScoreClass.getMax();

      validateLabel(riskScoreClass.getLabel());
      validateRiskScoreValueBounds(minRiskLevel);
      validateRiskScoreValueBounds(maxRiskLevel);
      validateUrl(riskScoreClass.getUrl());

      if (minRiskLevel > maxRiskLevel) {
        errors.add(new ValidationError(
            CONFIG_PREFIX + "risk-classes.[minRiskLevel+maxRiskLevel]",
            minRiskLevel + ", " + maxRiskLevel, MIN_GREATER_THAN_MAX));
      }
    }
  }

  private void validateLabel(String label) {
    if (label.isBlank()) {
      errors.add(new ValidationError(CONFIG_PREFIX + "risk-classes.label", label, BLANK_LABEL));
    }
  }

  private void validateRiskScoreValueBounds(int value) {
    if (!RiskScoreValidator.isWithinBounds(value)) {
      errors.add(
          new ValidationError(CONFIG_PREFIX + "risk-classes.[minRiskLevel|maxRiskLevel]", value, VALUE_OUT_OF_BOUNDS));
    }
  }

  private void validateUrl(String url) {
    try {
      new URL(url.trim());
    } catch (MalformedURLException e) {
      errors.add(new ValidationError(CONFIG_PREFIX + "risk-classes.url", url, INVALID_URL));
    }
  }

  private void validateValueRangeCoverage() {
    int partitionSum = riskScoreClassification.getRiskClassesList().stream()
        .mapToInt(riskScoreClass -> (riskScoreClass.getMax() - riskScoreClass.getMin()))
        .sum();

    if (partitionSum != ParameterSpec.RISK_SCORE_MAX) {
      errors.add(new ValidationError(CONFIG_PREFIX + "risk-classes", partitionSum, INVALID_PARTITIONING));
    }
  }
}
