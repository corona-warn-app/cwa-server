package app.coronawarn.server.services.distribution.assembly.appconfig.validation;

import java.util.Objects;

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
