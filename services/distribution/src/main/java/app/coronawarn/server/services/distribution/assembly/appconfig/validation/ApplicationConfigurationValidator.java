

package app.coronawarn.server.services.distribution.assembly.appconfig.validation;

import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.ValidationError.ErrorType.VALUE_OUT_OF_BOUNDS;

import app.coronawarn.server.common.protocols.internal.ApplicationConfiguration;
import app.coronawarn.server.common.protocols.internal.RiskScoreClassification;
import app.coronawarn.server.common.protocols.internal.RiskScoreParameters;

/**
 * This validator validates a {@link ApplicationConfiguration}. It will re-use the {@link ConfigurationValidator} from
 * the sub-configurations of {@link RiskScoreParameters} and {@link RiskScoreClassification}.
 */
public class ApplicationConfigurationValidator extends ConfigurationValidator {

  private final ApplicationConfiguration config;

  /**
   * Creates a new instance for the given {@link ApplicationConfiguration}.
   *
   * @param config the Application Configuration to validate.
   */
  public ApplicationConfigurationValidator(ApplicationConfiguration config) {
    this.config = config;
  }

  @Override
  public ValidationResult validate() {
    this.errors = new ValidationResult();

    validateMinRisk();

    errors.with(new ExposureConfigurationValidator(config.getExposureConfig()).validate());
    errors.with(new RiskScoreClassificationValidator(config.getRiskScoreClasses()).validate());
    errors.with(new ApplicationVersionConfigurationValidator(config.getAppVersion()).validate());
    errors.with(new AttenuationDurationValidator(config.getAttenuationDuration()).validate());

    return errors;
  }

  private void validateMinRisk() {
    int minLevel = this.config.getMinRiskScore();

    if (!RiskScoreValidator.isWithinBounds(minLevel)) {
      this.errors.add(new ValidationError("min-risk-score", minLevel, VALUE_OUT_OF_BOUNDS));
    }
  }
}
