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

package app.coronawarn.server.services.download.config;

import java.util.Map;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validate the values of the DownloadServiceConfig.
 */
public class DownloadServiceConfigValidator implements Validator {

  @Override
  public boolean supports(Class<?> type) {
    return type == DownloadServiceConfig.class;
  }

  @Override
  public void validate(Object configurationObject, Errors errors) {
    DownloadServiceConfig properties = (DownloadServiceConfig) configurationObject;
    validateTransmissionRiskLevelDerivationMap(errors, properties);
  }

  private void validateTransmissionRiskLevelDerivationMap(Errors errors, DownloadServiceConfig properties) {
    Map<Integer, Integer> transmissionRiskLevelFromDaysSinceOnsetOfSymptoms =
        properties.getTekFieldDerivations().getTransmissionRiskLevelFromDaysSinceOnsetOfSymptoms();

    transmissionRiskLevelFromDaysSinceOnsetOfSymptoms.forEach((daysSinceOnsetOfSymptoms, transmissionRiskLevel) -> {
      checkTransmissionRiskLevelInAllowedRange(transmissionRiskLevel, errors);
      checkDaysSinceOnsetOfSymptomsInAllowedRange(daysSinceOnsetOfSymptoms, errors);
    });
  }

  private void checkTransmissionRiskLevelInAllowedRange(Integer transmissionRiskLevel, Errors errors) {
    if (transmissionRiskLevel > 8 || transmissionRiskLevel < 1) {
      errors.rejectValue("tekFieldDerivations",
          "[" + transmissionRiskLevel + "]: transmissionRiskLevel value is not in the allowed range (1 to 8)");
    }
  }

  private void checkDaysSinceOnsetOfSymptomsInAllowedRange(Integer daysSinceOnsetOfSymptoms, Errors errors) {
    if (daysSinceOnsetOfSymptoms > 4000 || daysSinceOnsetOfSymptoms < -14) {
      errors.rejectValue("tekFieldDerivations",
          "[" + daysSinceOnsetOfSymptoms
              + "]: daysSinceOnsetOfSymptoms value is not in the allowed range (-14 to 4000)");
    }
  }
}
