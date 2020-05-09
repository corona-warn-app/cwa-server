package app.coronawarn.server.services.distribution.parameters.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import app.coronawarn.server.services.distribution.parameters.ParameterFileProvider;
import app.coronawarn.server.services.distribution.parameters.UnableToLoadFileException;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ParameterFileValidatorTest {

  private static final ValidationResult SUCCESS = new ValidationResult();

  @ParameterizedTest
  @MethodSource("createOkTests")
  public void ok(String filePath) throws UnableToLoadFileException {
    var scores = new ParameterFileProvider().readFile(filePath);
    var validator = new ParameterFileValidator(scores);
    var result = validator.validate();

    assertEquals(SUCCESS, result);
  }

  @ParameterizedTest
  @MethodSource("createFailedTests")
  public void fails(TestWithResult test) throws UnableToLoadFileException {
    var scores = new ParameterFileProvider().readFile(test.path());
    var validator = new ParameterFileValidator(scores);
    var actualResult = validator.validate();

    assertEquals(test.result, actualResult);
  }

  @Test
  public void emptyFileThrowsLoadFailure() {
    assertThrows(UnableToLoadFileException.class, () -> {
      new ParameterFileProvider().readFile("parameters/empty.yaml");
    });
  }

  private static Stream<Arguments> createOkTests() {
    return Stream.of(
        "parameters/all_ok.yaml",
        "parameters/partly_filled.yaml")
        .map(Arguments::of);
  }

  private static Stream<Arguments> createFailedTests() {
    return Stream.of(
        TestWithResult.ScoreTooHigh(),
        TestWithResult.ScoreNegative(),
        TestWithResult.WeightNegative(),
        TestWithResult.WeightTooHigh()
    ).map(Arguments::of);
  }

}
