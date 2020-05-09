package app.coronawarn.server.services.distribution.parameters.validation;

import app.coronawarn.server.common.protocols.internal.RiskLevel;
import app.coronawarn.server.common.protocols.internal.RiskScoreParameters;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public class ParameterFileValidator {

  private RiskScoreParameters file;

  private ValidationResult errors;

  public ParameterFileValidator(RiskScoreParameters file) {
    this.file = file;
  }

  public ValidationResult validate() throws ValidationFailedException {
    this.errors = new ValidationResult();

    validateWeights();

    try {
      validateParameterRiskLevels("duration", file.getDuration());
      validateParameterRiskLevels("transmission", file.getTransmission());
      validateParameterRiskLevels("daysSinceLastExposure", file.getDaysSinceLastExposure());
      validateParameterRiskLevels("attenuation", file.getAttenuation());
    } catch (IntrospectionException e) {
      throw new ValidationFailedException("Unable to check risk levels", e);
    }

    return errors;
  }

  private void validateParameterRiskLevels(String name, Object o) throws IntrospectionException {
    var bean = Introspector.getBeanInfo(o.getClass());

    Arrays.stream(bean.getPropertyDescriptors())
        .filter(pd -> pd.getPropertyType() == RiskLevel.class)
        .forEach(pd -> validateScore(pd, o, name));
  }

  private void validateScore(PropertyDescriptor pd, Object o, String parameter)
      throws ValidationFailedException {
    try {
      RiskLevel level = (RiskLevel) pd.getReadMethod().invoke(o);

      if (level == RiskLevel.UNRECOGNIZED) {
        this.errors.add(new RiskLevelValidationError(parameter, pd.getName()));
      }
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new ValidationFailedException("Unable to read property " + pd.getName(), e);
    }
  }

  private void validateWeights() {
    validateWeight(file.getTransmissionWeight(), "transmission");
    validateWeight(file.getDurationWeight(), "duration");
    validateWeight(file.getAttenuationWeight(), "attenuation");
  }

  private void validateWeight(double weight, String name) {
    if (isOutOfRange(ParameterSpec.WEIGHT_MIN, ParameterSpec.WEIGHT_MAX, weight)) {
      this.errors.add(new WeightValidationError(name, weight));
    }
  }

  private boolean isOutOfRange(int min, int max, double x) {
    return x < min || x > max;
  }
}
