package app.coronawarn.server.services.distribution.assembly.exposureconfig.validation;

import java.util.Objects;

/**
 * Defines a validation errors for risk levels, used by the parameter scores.
 */
public class RiskLevelValidationError implements ValidationError {

  private final String parameter;

  private final String riskLevel;

  public RiskLevelValidationError(String parameter, String riskLevel) {
    this.parameter = parameter;
    this.riskLevel = riskLevel;
  }

  public String getParameter() {
    return parameter;
  }

  public String getRiskLevel() {
    return riskLevel;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RiskLevelValidationError that = (RiskLevelValidationError) o;
    return Objects.equals(getParameter(), that.getParameter())
        && Objects.equals(getRiskLevel(), that.getRiskLevel());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getParameter(), getRiskLevel());
  }

  @Override
  public String toString() {
    return "RiskLevelValidationError{" +
        "parameter='" + parameter + '\'' +
        ", riskLevel='" + riskLevel + '\'' +
        '}';
  }
}
