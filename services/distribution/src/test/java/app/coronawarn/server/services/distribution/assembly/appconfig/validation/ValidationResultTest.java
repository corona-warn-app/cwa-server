

package app.coronawarn.server.services.distribution.assembly.appconfig.validation;

import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.ValidationError.ErrorType.INVALID_URL;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ValidationResultTest {

  private static final ValidationError EXP_VALIDATION_ERROR = new ValidationError("expSrc", "expValue", INVALID_URL);
  private ValidationResult emptyValidationResult;

  @BeforeEach
  void setUpEmptyResult() {
    emptyValidationResult = new ValidationResult();
  }

  @Test
  void hasErrorsReturnsFalseIfNoErrors() {
    assertThat(emptyValidationResult.hasErrors()).isFalse();
  }

  @Test
  void hasErrorsReturnsTrueIfErrors() {
    emptyValidationResult.add(EXP_VALIDATION_ERROR);
    assertThat(emptyValidationResult.hasErrors()).isTrue();
  }

  @Test
  void toStringReturnsCorrectErrorInformation() {
    emptyValidationResult.add(EXP_VALIDATION_ERROR);
    assertThat(emptyValidationResult).hasToString("[" + EXP_VALIDATION_ERROR.toString() + "]");
  }

  @Test
  void toStringReturnsCorrectErrorInformationIfEmpty() {
    assertThat(emptyValidationResult).hasToString("[]");
  }
}
