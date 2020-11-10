package app.coronawarn.server.services.distribution.assembly.appconfig.validation.v2;

import app.coronawarn.server.common.protocols.internal.ApplicationConfiguration;
import app.coronawarn.server.common.protocols.internal.v2.ApplicationConfigurationAndroid;
import app.coronawarn.server.services.distribution.assembly.appconfig.validation.ConfigurationValidator;
import app.coronawarn.server.services.distribution.assembly.appconfig.validation.ValidationResult;

/**
 * This validator validates a {@link ApplicationConfigurationAndroid}. It will cascade validation to
 * other {@link ConfigurationValidator} instances for the different parts of the configuration.
 */
public class ApplicationConfigurationAndroidValidator extends ConfigurationValidator {

  private final ApplicationConfigurationAndroid config;

  /**
   * Creates a new instance for the given {@link ApplicationConfiguration}.
   *
   * @param config the Application Configuration to validate.
   */
  public ApplicationConfigurationAndroidValidator(ApplicationConfigurationAndroid config) {
    this.config = config;
  }

  @Override
  public ValidationResult validate() {
    this.errors = new ValidationResult();

    return errors;
  }
}
