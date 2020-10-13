

package app.coronawarn.server.services.distribution.assembly.appconfig.validation;

import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.ExposureConfigurationValidator.CONFIG_PREFIX;
import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.utils.ValidationUtils.buildError;
import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.ValidationError.ErrorType.TOO_MANY_DECIMAL_PLACES;
import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.ValidationError.ErrorType.VALUE_OUT_OF_BOUNDS;
import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.common.protocols.internal.RiskLevel;
import app.coronawarn.server.common.protocols.internal.RiskScoreParameters;
import app.coronawarn.server.services.distribution.assembly.appconfig.UnableToLoadFileException;
import app.coronawarn.server.services.distribution.assembly.appconfig.YamlLoader;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ExposureConfigurationValidatorTest {

  private static final ValidationResult SUCCESS = new ValidationResult();

  @ParameterizedTest
  @MethodSource("createOkTests")
  void ok(TestWithExpectedResult test) throws UnableToLoadFileException {
    assertThat(getResultForTest(test)).isEqualTo(SUCCESS);
  }

  @ParameterizedTest
  @MethodSource("createFailedTests")
  void fails(TestWithExpectedResult test) throws UnableToLoadFileException {
    assertThat(getResultForTest(test)).isEqualTo(test.result);
  }

  private ValidationResult getResultForTest(TestWithExpectedResult test) throws UnableToLoadFileException {
    var config = YamlLoader.loadYamlIntoProtobufBuilder(test.path(), RiskScoreParameters.Builder.class).build();
    var validator = new ExposureConfigurationValidator(config);
    return validator.validate();
  }

  private static Stream<Arguments> createOkTests() {
    return Stream.of(
        AllOk(),
        PartlyFilled(),
        WeightOk()
    ).map(Arguments::of);
  }

  private static Stream<Arguments> createFailedTests() {
    return Stream.of(
        ScoreTooHigh(),
        WeightNegative(),
        WeightTooHigh()
    ).map(Arguments::of);
  }

  public static TestWithExpectedResult AllOk() {
    return new TestWithExpectedResult("all_ok.yaml");
  }

  public static TestWithExpectedResult PartlyFilled() {
    return new TestWithExpectedResult("partly_filled.yaml");
  }

  public static TestWithExpectedResult WeightOk() {
    return new TestWithExpectedResult("weight_ok.yaml");
  }

  public static TestWithExpectedResult WeightNegative() {
    return new TestWithExpectedResult("weight_negative.yaml")
        .with(buildError(CONFIG_PREFIX + "transmission", -10d, VALUE_OUT_OF_BOUNDS))
        .with(buildError(CONFIG_PREFIX + "attenuation", 0.0001, TOO_MANY_DECIMAL_PLACES))
        .with(buildError(CONFIG_PREFIX + "attenuation", 0.0001, VALUE_OUT_OF_BOUNDS))
        .with(buildError(CONFIG_PREFIX + "duration", .0, VALUE_OUT_OF_BOUNDS));
  }

  public static TestWithExpectedResult WeightTooHigh() {
    return new TestWithExpectedResult("weight_too_high.yaml")
        .with(buildError(CONFIG_PREFIX + "duration", 99999999d, VALUE_OUT_OF_BOUNDS))
        .with(buildError(CONFIG_PREFIX + "attenuation", 100.001d, VALUE_OUT_OF_BOUNDS))
        .with(buildError(CONFIG_PREFIX + "transmission", 101d, VALUE_OUT_OF_BOUNDS));
  }

  public static TestWithExpectedResult ScoreTooHigh() {
    return new TestWithExpectedResult("score_too_high.yaml")
        .with(buildError(CONFIG_PREFIX + "transmission.appDefined1", RiskLevel.UNRECOGNIZED, VALUE_OUT_OF_BOUNDS))
        .with(buildError(CONFIG_PREFIX + "transmission.appDefined2", RiskLevel.UNRECOGNIZED, VALUE_OUT_OF_BOUNDS));
  }
}
