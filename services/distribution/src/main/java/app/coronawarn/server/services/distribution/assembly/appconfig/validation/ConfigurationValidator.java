

package app.coronawarn.server.services.distribution.assembly.appconfig.validation;

/**
 * Classes that extend {@link ConfigurationValidator} validate the values of an associated {@link
 * com.google.protobuf.Message} instance.
 */
public abstract class ConfigurationValidator {

  protected ValidationResult errors;

  /**
   * Performs a validation of the associated {@link com.google.protobuf.Message} instance and returns information about
   * validation failures.
   *
   * @return The ValidationResult instance, containing information about possible errors.
   */
  public abstract ValidationResult validate();
}
