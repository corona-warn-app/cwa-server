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

import app.coronawarn.server.common.protocols.internal.RiskLevel;
import app.coronawarn.server.common.protocols.internal.RiskScoreParameters;
import app.coronawarn.server.services.distribution.assembly.appconfig.validation.WeightValidationError.ErrorType;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.Arrays;

/**
 * The Exposure Configuration Validator checks the values of a given RiskScoreParameters instance. Validation is
 * performed according to the Apple/Google spec.<br>
 * <br>
 * Weights must be in the range of 0.001 to 100.<br> Scores must be in the range of 1 to 8.<br>
 */
public class ExposureConfigurationValidator extends AppConfigurationValidator {

  private final RiskScoreParameters config;

  public ExposureConfigurationValidator(RiskScoreParameters config) {
    this.config = config;
  }

  /**
   * Triggers the validation of the configuration.
   *
   * @return the ValidationResult instance, containing information about possible errors.
   * @throws ValidationExecutionException in case the validation could not be performed
   */
  @Override
  public ValidationResult validate() {
    this.errors = new ValidationResult();

    validateWeights();

    try {
      validateParameterRiskLevels("duration", config.getDuration());
      validateParameterRiskLevels("transmission", config.getTransmission());
      validateParameterRiskLevels("daysSinceLastExposure", config.getDaysSinceLastExposure());
      validateParameterRiskLevels("attenuation", config.getAttenuation());
    } catch (IntrospectionException e) {
      throw new ValidationExecutionException("Unable to check risk levels", e);
    }

    return errors;
  }

  private void validateParameterRiskLevels(String name, Object object)
      throws IntrospectionException {
    BeanInfo bean = Introspector.getBeanInfo(object.getClass());

    Arrays.stream(bean.getPropertyDescriptors())
        .filter(propertyDescriptor -> propertyDescriptor.getPropertyType() == RiskLevel.class)
        .forEach(propertyDescriptor -> validateScore(propertyDescriptor, object, name));
  }

  private void validateScore(
      PropertyDescriptor propertyDescriptor, Object object, String parameter) {
    try {
      RiskLevel level = (RiskLevel) propertyDescriptor.getReadMethod().invoke(object);

      if (level == RiskLevel.UNRECOGNIZED || level == RiskLevel.RISK_LEVEL_UNSPECIFIED) {
        this.errors.add(new RiskLevelValidationError(parameter, propertyDescriptor.getName()));
      }
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new ValidationExecutionException(
          "Unable to read property " + propertyDescriptor.getName(), e);
    }
  }

  private void validateWeights() {
    validateWeight(config.getTransmissionWeight(), "transmission");
    validateWeight(config.getDurationWeight(), "duration");
    validateWeight(config.getAttenuationWeight(), "attenuation");
  }

  private void validateWeight(double weight, String name) {
    if (isOutOfRange(ParameterSpec.WEIGHT_MIN, ParameterSpec.WEIGHT_MAX, weight)) {
      this.errors.add(new WeightValidationError(name, weight, ErrorType.OUT_OF_RANGE));
    }

    if (!respectsMaximumDecimalPlaces(weight)) {
      this.errors.add(new WeightValidationError(name, weight, ErrorType.TOO_MANY_DECIMAL_PLACES));
    }
  }

  private boolean respectsMaximumDecimalPlaces(double weight) {
    BigDecimal bd = new BigDecimal(String.valueOf(weight));

    return bd.scale() <= ParameterSpec.WEIGHT_MAX_DECIMALS;
  }


  private boolean isOutOfRange(double min, double max, double x) {
    return x < min || x > max;
  }
}
