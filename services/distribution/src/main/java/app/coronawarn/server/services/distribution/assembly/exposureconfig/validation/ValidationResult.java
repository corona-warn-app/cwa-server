/*
 * Corona-Warn-App
 *
 * Deutsche Telekom AG, SAP SE and all other contributors /
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

package app.coronawarn.server.services.distribution.assembly.exposureconfig.validation;

import java.util.HashSet;

/**
 * The result of a validation run for Exposure Configurations. Find details about possible errors in this collection.
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
