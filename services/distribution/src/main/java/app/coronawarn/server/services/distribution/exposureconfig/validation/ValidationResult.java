package app.coronawarn.server.services.distribution.exposureconfig.validation;

import java.util.HashSet;

/**
 * The result of a validation run for Exposure Configurations. Find details about possible
 * errors in this collection.
 */
public class ValidationResult extends HashSet<ValidationError> {

  /**
   * Checks whether this validation result instance has at least one error.
   *
   * @return true if yes, false otherwise
   */
  public boolean hasErrors() {
    return !this.isEmpty();
  }

  /**
   * Checks whether this validation result instance has no errors.
   *
   * @return true if yes, false otherwise
   */
  public boolean isSuccessful() {
    return !hasErrors();
  }
}
