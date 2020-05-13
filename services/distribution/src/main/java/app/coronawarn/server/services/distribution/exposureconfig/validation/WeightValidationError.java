package app.coronawarn.server.services.distribution.exposureconfig.validation;

import java.util.Objects;

public class WeightValidationError implements ValidationError {

  private final ErrorType errorType;

  private final String parameter;

  private final double givenValue;

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
    return "WeightValidationError{" +
        "errorType=" + errorType +
        ", parameter='" + parameter + '\'' +
        ", givenValue=" + givenValue +
        '}';
  }

  public enum ErrorType {
    OUT_OF_RANGE,
    TOO_MANY_DECIMAL_PLACES
  }
}
