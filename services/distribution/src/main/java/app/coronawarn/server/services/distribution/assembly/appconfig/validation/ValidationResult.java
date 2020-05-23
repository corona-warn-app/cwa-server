/*
 * Corona-Warn-App
 *
 * SAP SE and all other contributors /
 * copyright owners license this file to you under the Apache
 * License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

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

  @Override
  public int hashCode() {
    return Objects.hash(errors);
  }
}
