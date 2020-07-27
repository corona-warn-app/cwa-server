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

/**
 * Validates a risk score value according to Exposure Notification API by Google/Apple.
 */
public class RiskScoreValidator {

  private RiskScoreValidator() {

  }

  /**
   * Validates the bounds of a risk score value.
   *
   * @param value the risk score value
   * @return true if is in bounds, false otherwise
   */
  public static boolean isInBounds(int value) {
    return ParameterSpec.RISK_SCORE_MIN <= value && value <= ParameterSpec.RISK_SCORE_MAX;
  }
}
