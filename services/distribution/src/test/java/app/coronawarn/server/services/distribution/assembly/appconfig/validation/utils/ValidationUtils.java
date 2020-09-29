package app.coronawarn.server.services.distribution.assembly.appconfig.validation.utils;

import app.coronawarn.server.services.distribution.assembly.appconfig.validation.ValidationError;
import app.coronawarn.server.services.distribution.assembly.appconfig.validation.ValidationError.ErrorType;
import app.coronawarn.server.services.distribution.assembly.appconfig.validation.ValidationResult;
import java.util.Arrays;

public class ValidationUtils {

  public static ValidationError buildError(String parameter, Object value, ErrorType reason) {
    return new ValidationError(parameter, value, reason);
  }

  public static ValidationResult buildExpectedResult(ValidationError... errors) {
    var validationResult = new ValidationResult();
    Arrays.stream(errors).forEach(validationResult::add);

    return validationResult;
  }

}