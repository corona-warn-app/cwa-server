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
import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.ParameterSpec.ATTENUATION_DURATION_WEIGHT_MAX;
import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.ParameterSpec.ATTENUATION_DURATION_WEIGHT_MIN;
import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.WeightValidationError.ErrorType.OUT_OF_RANGE;
import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.WeightValidationError.ErrorType.TOO_MANY_DECIMAL_PLACES;

import app.coronawarn.server.common.protocols.internal.AttenuationDuration;
import java.math.BigDecimal;

/**
 * The AttenuationDurationValidator validates the values of an associated {@link AttenuationDuration} instance.
 */
public class AttenuationDurationValidator extends ConfigurationValidator {

  private final AttenuationDuration attenuationDuration;

  public AttenuationDurationValidator(AttenuationDuration attenuationDuration) {
    this.attenuationDuration = attenuationDuration;
  }

  @Override
  public ValidationResult validate() {
    errors = new ValidationResult();

    validateThresholds();
    validateWeights();

    return errors;
  }

  private void validateThresholds() {
    int lower = attenuationDuration.getThresholds().getLower();
    int upper = attenuationDuration.getThresholds().getUpper();

    checkThresholdBound("lower", lower);
    checkThresholdBound("upper", upper);

    if (lower > upper) {
      String parameters = "attenuation-duration.thresholds.lower, attenuation-duration.thresholds.upper";
      String values = lower + ", " + upper;
      this.errors.add(new GeneralValidationError(parameters, values, MIN_GREATER_THAN_MAX));
    }
  }

  private void checkThresholdBound(String thresholdLabel, int thresholdValue) {
    if (thresholdValue < ATTENUATION_DURATION_THRESHOLD_MIN || thresholdValue > ATTENUATION_DURATION_THRESHOLD_MAX) {
      this.errors.add(new GeneralValidationError(
          "attenuation-duration.thresholds." + thresholdLabel, thresholdValue, VALUE_OUT_OF_BOUNDS));
    }
  }

  private void validateWeights() {
    checkWeight("low", attenuationDuration.getWeights().getLow());
    checkWeight("mid", attenuationDuration.getWeights().getMid());
    checkWeight("high", attenuationDuration.getWeights().getHigh());
  }

  private void checkWeight(String weightLabel, double weightValue) {
    if (weightValue < ATTENUATION_DURATION_WEIGHT_MIN || weightValue > ATTENUATION_DURATION_WEIGHT_MAX) {
      this.errors.add(new WeightValidationError(
          "attenuation-duration.weights." + weightLabel, weightValue, OUT_OF_RANGE));
    }

    if (BigDecimal.valueOf(weightValue).scale() > ParameterSpec.ATTENUATION_DURATION_WEIGHT_MAX_DECIMALS) {
      this.errors.add(new WeightValidationError(
          "attenuation-duration.weights." + weightLabel, weightValue, TOO_MANY_DECIMAL_PLACES));
    }
  }
}
