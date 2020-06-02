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

public class WeightValidationError implements ValidationError {

  private final ErrorType errorType;

  private final String parameter;

  private final double givenValue;

  /**
   * Constructs a {@link WeightValidationError} for a specific error occurrence.
   *
   * @param parameter  The name of the exposure configuration parameter.
   * @param givenValue The value of the exposure configuration parameter.
   * @param errorType  An error specifier.
   */
  public WeightValidationError(String parameter, double givenValue, ErrorType errorType) {
    this.parameter = parameter;
    this.givenValue = givenValue;
    this.errorType = errorType;
  }

  public String getParameter() {
    return parameter;
  }

  public double getGivenValue() {
    return givenValue;
  }

  public ErrorType getErrorType() {
    return errorType;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    WeightValidationError that = (WeightValidationError) o;
    return Double.compare(that.getGivenValue(), getGivenValue()) == 0
        && getErrorType() == that.getErrorType()
        && Objects.equals(getParameter(), that.getParameter());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getErrorType(), getParameter(), getGivenValue());
  }

  @Override
  public String toString() {
    return "WeightValidationError{"
        + "errorType=" + errorType
        + ", parameter='" + parameter + '\''
        + ", givenValue=" + givenValue
        + '}';
  }

  public enum ErrorType {
    OUT_OF_RANGE,
    TOO_MANY_DECIMAL_PLACES
  }
}
