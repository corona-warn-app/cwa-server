/*-
 * ---license-start
 * Corona-Warn-App
 * ---
 * Copyright (C) 2020 SAP SE and all other contributors
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ---license-end
 */

package app.coronawarn.server.services.distribution.assembly.appconfig.validation;

import java.util.Objects;

/**
 * {@link ValidationError} instances hold information about a errors that occurred during app configuration validation.
 */
public class ValidationError {

  private final String errorSource;
  private final Object value;
  private final ErrorType reason;

  /**
   * Creates a {@link ValidationError} that stores the specified validation error source, erroneous value and a reason
   * for the error to occur.
   *
   * @param errorSource A label that describes the property associated with this validation error.
   * @param value       The value that caused the validation error.
   * @param reason      A validation error specifier.
   */
  public ValidationError(String errorSource, Object value, ErrorType reason) {
    this.errorSource = errorSource;
    this.value = value;
    this.reason = reason;
  }

  @Override
  public String toString() {
    return "GeneralValidationError{"
        + "errorType=" + reason
        + ", parameter='" + errorSource + '\''
        + ", givenValue=" + value
        + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ValidationError that = (ValidationError) o;
    return Objects.equals(errorSource, that.errorSource)
        && Objects.equals(value, that.value)
        && Objects.equals(reason, that.reason);
  }

  @Override
  public int hashCode() {
    return Objects.hash(errorSource, value, reason);
  }

  public enum ErrorType {
    BLANK_LABEL,
    MIN_GREATER_THAN_MAX,
    VALUE_OUT_OF_BOUNDS,
    INVALID_URL,
    INVALID_PARTITIONING,
    TOO_MANY_DECIMAL_PLACES,
    MISSING_ENTRY
  }
}
