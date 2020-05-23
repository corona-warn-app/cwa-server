package app.coronawarn.server.services.distribution.assembly.exposureconfig.validation;

import app.coronawarn.server.common.protocols.internal.ApplicationConfiguration;

public class ApplicationConfigurationValidator extends ConfigurationValidator {

  private final ApplicationConfiguration config;

  public ApplicationConfigurationValidator(ApplicationConfiguration config) {
    this.config = config;
  }

  @Override
  public ValidationResult validate() {
    this.errors = new ValidationResult();

    validateMinRisk();

    ValidationResult exposureResult = new ExposureConfigurationValidator(config.getExposureConfig()).validate();
    ValidationResult riskScoreResult = new RiskScoreClassificationValidator(config.getRiskScoreClasses()).validate();

    return errors.with(exposureResult).with(riskScoreResult);
  }

  private void validateMinRisk() {
    var minLevel = this.config.getMinRiskLevel();

    if (minLevel < 0 || minLevel > RiskScoreClassificationValidator.RISK_SCORE_VALUE_RANGE - 1) {
      this.errors.add(new MinimumRiskLevelValidationError(minLevel));
    }
  }
}
