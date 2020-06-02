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

import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.GeneralValidationError.ErrorType.MIN_GREATER_THAN_MAX;
import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.GeneralValidationError.ErrorType.VALUE_OUT_OF_BOUNDS;
import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.ParameterSpec.ATTENUATION_DURATION_THRESHOLD_MAX;
import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.ParameterSpec.ATTENUATION_DURATION_THRESHOLD_MIN;

import app.coronawarn.server.common.protocols.internal.ApplicationConfiguration;
import app.coronawarn.server.common.protocols.internal.RiskScoreClassification;
import app.coronawarn.server.common.protocols.internal.RiskScoreParameters;

/**
 * This validator validates a {@link ApplicationConfiguration}. It will re-use the {@link ConfigurationValidator} from
 * the sub-configurations of {@link RiskScoreParameters} and {@link RiskScoreClassification}.
 */
public class ApplicationConfigurationValidator extends ConfigurationValidator {

  private final ApplicationConfiguration config;

  /**
   * Creates a new instance for the given {@link ApplicationConfiguration}.
   *
   * @param config the Application Configuration to validate.
   */
  public ApplicationConfigurationValidator(ApplicationConfiguration config) {
    this.config = config;
  }

  @Override
  public ValidationResult validate() {
    this.errors = new ValidationResult();

    validateMinRisk();
    validateAttenuationDurationThresholds();

    ValidationResult exposureResult = new ExposureConfigurationValidator(config.getExposureConfig()).validate();
    ValidationResult riskScoreResult = new RiskScoreClassificationValidator(config.getRiskScoreClasses()).validate();

    return errors.with(exposureResult).with(riskScoreResult);
  }

  private void validateMinRisk() {
    int minLevel = this.config.getMinRiskScore();

    if (!RiskScoreValidator.isInBounds(minLevel)) {
      this.errors.add(new MinimumRiskLevelValidationError(minLevel));
    }
  }

  private void validateAttenuationDurationThresholds() {
    int lower = config.getAttenuationDurationThresholds().getLower();
    int upper = config.getAttenuationDurationThresholds().getUpper();

    checkThresholdBound("lower", lower);
    checkThresholdBound("upper", upper);

    if (lower > upper) {
      String parameters = "attenuationDurationThreshold.lower, attenuationDurationThreshold.upper";
      String values = lower + ", " + upper;
      this.errors.add(new GeneralValidationError(parameters, values, MIN_GREATER_THAN_MAX));
    }
  }

  private void checkThresholdBound(String boundLabel, int boundValue) {
    if (boundValue < ATTENUATION_DURATION_THRESHOLD_MIN || boundValue > ATTENUATION_DURATION_THRESHOLD_MAX) {
      this.errors.add(
          new GeneralValidationError("attenuationDurationThreshold." + boundLabel, boundValue, VALUE_OUT_OF_BOUNDS));
    }
  }
}
