package app.coronawarn.server.services.distribution.assembly.appconfig.validation.v2;

import app.coronawarn.server.common.protocols.internal.ApplicationConfiguration;
import app.coronawarn.server.common.protocols.internal.v2.ApplicationConfigurationIOS;
import app.coronawarn.server.services.distribution.assembly.appconfig.validation.ConfigurationValidator;
import app.coronawarn.server.services.distribution.assembly.appconfig.validation.ValidationResult;

/**
 * This validator validates a {@link ApplicationConfigurationIOS}. It will cascade validation to
 * other {@link ConfigurationValidator} instances for the different parts of the configuration.
 */
public class ApplicationConfigurationIosValidator extends ConfigurationValidator {

  private final ApplicationConfigurationIOS config;

  /**
   * Creates a new instance for the given {@link ApplicationConfiguration}.
   *
   * @param config the Application Configuration to validate.
   */
  public ApplicationConfigurationIosValidator(ApplicationConfigurationIOS config) {
    this.config = config;
  }

  @Override
  public ValidationResult validate() {
    this.errors = new ValidationResult();

    return errors;
  }
}
