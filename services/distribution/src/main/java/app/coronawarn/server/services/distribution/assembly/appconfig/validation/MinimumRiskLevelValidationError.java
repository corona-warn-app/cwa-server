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
 * Represents a minimum risk level error, in case the value is out of bounds.
 */
public class MinimumRiskLevelValidationError implements ValidationError {

  private int riskLevelProvided;

  public MinimumRiskLevelValidationError(int riskLevelProvided) {
    this.riskLevelProvided = riskLevelProvided;
  }

  public int getRiskLevelProvided() {
    return riskLevelProvided;
  }

  @Override
  public String toString() {
    return "MinimumRiskLevelValidationError{"
        + "riskLevelProvided="
        + riskLevelProvided
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
    MinimumRiskLevelValidationError that = (MinimumRiskLevelValidationError) o;
    return getRiskLevelProvided() == that.getRiskLevelProvided();
  }

  @Override
  public int hashCode() {
    return Objects.hash(getRiskLevelProvided());
  }
}
