package app.coronawarn.server.services.distribution.parameters.validation;

import java.util.Objects;

public class WeightValidationError implements ValidationError {

  private String parameter;

  private double givenValue;

  public WeightValidationError(String parameter, double givenValue) {
    this.parameter = parameter;
    this.givenValue = givenValue;
  }

  public String getParameter() {
    return parameter;
  }

  public double getGivenValue() {
    return givenValue;
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
    return Double.compare(that.getGivenValue(), getGivenValue()) == 0 &&
        Objects.equals(getParameter(), that.getParameter());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getParameter(), getGivenValue());
  }

  @Override
  public String toString() {
    return "WeightValidationError{" +
        "parameter='" + parameter + '\'' +
        ", givenValue=" + givenValue +
        '}';
  }
}
