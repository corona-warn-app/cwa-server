

package app.coronawarn.server.services.distribution.assembly.appconfig.validation;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * The result of a validation run for Exposure Configurations. Find details about possible errors in this collection.
 */
public class ValidationResult {

  private Set<ValidationError> errors = new HashSet<>();

  /**
   * Adds a {@link ValidationError} to this {@link ValidationResult}.
   *
   * @param error The {@link ValidationError} that shall be added.
   * @return true if this {@link ValidationResult} did not already contain the specified {@link ValidationError}.
   */
  public boolean add(ValidationError error) {
    return this.errors.add(error);
  }

  /**
   * Checks whether this validation result instance has at least one error.
   *
   * @return true if yes, false otherwise
   */
  public boolean hasErrors() {
    return !this.errors.isEmpty();
  }

  @Override
  public String toString() {
    return errors.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ValidationResult that = (ValidationResult) o;
    return Objects.equals(errors, that.errors);
  }

  public boolean hasError(ValidationError error) {
    return this.errors.contains(error);
  }

  @Override
  public int hashCode() {
    return Objects.hash(errors);
  }

  /**
   * Adds all validation errors of the given result to this one, effectively merging them.
   *
   * @param other the other validation result to merge
   * @return this instance
   */
  public ValidationResult with(ValidationResult other) {
    this.errors.addAll(other.errors);

    return this;
  }
}
