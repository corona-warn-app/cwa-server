package app.coronawarn.server.services.distribution.assembly.appconfig.validation;

import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.ParameterSpec.RISK_SCORE_MAX;
import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.ParameterSpec.RISK_SCORE_MIN;
import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.RiskScoreClassificationValidatorTest.buildError;
import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.ValidationError.ErrorType.VALUE_OUT_OF_BOUNDS;
import static app.coronawarn.server.services.distribution.common.Helpers.loadApplicationConfiguration;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import app.coronawarn.server.common.protocols.internal.ApplicationConfiguration;
import app.coronawarn.server.common.shared.exception.UnableToLoadFileException;
import app.coronawarn.server.services.distribution.assembly.appconfig.ApplicationConfigurationPublicationConfig;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.util.Arrays;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@EnableConfigurationProperties(value = DistributionServiceConfig.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DistributionServiceConfig.class, ApplicationConfigurationPublicationConfig.class},
    initializers = ConfigDataApplicationContextInitializer.class)
class ApplicationConfigurationValidatorTest {

  private static final ValidationResult SUCCESS = new ValidationResult();
  private static final TestWithExpectedResult.Builder TEST_BUILDER = new TestWithExpectedResult.Builder("configtests/");

  @ParameterizedTest
  @MethodSource("createOkTests")
  void ok(TestWithExpectedResult test) throws UnableToLoadFileException {
    assertThat(getResultForTest(test)).isEqualTo(SUCCESS);
  }

  @ParameterizedTest
  @MethodSource("createNegativeTests")
  void negative(TestWithExpectedResult test) throws UnableToLoadFileException {
    assertThat(getResultForTest(test)).isEqualTo(test.result);
  }

  @Test
  void circular() {
    assertThatExceptionOfType(UnableToLoadFileException.class)
        .isThrownBy(() -> loadApplicationConfiguration("configtests/app-config_circular.yaml"));
  }

  private ValidationResult getResultForTest(TestWithExpectedResult test) throws UnableToLoadFileException {
    var config = loadApplicationConfiguration(test.path());
    var validator = new ApplicationConfigurationValidator(config);
    return validator.validate();
  }

  private static Stream<Arguments> createOkTests() {
    return Stream.of(allOk()).map(Arguments::of);
  }

  private static Stream<Arguments> createNegativeTests() {
    return Stream.of(
        minRiskThresholdOutOfBoundsNegative(),
        minRiskThresholdOutOfBoundsPositive()
    ).map(Arguments::of);
  }

  private static TestWithExpectedResult allOk() {
    return TEST_BUILDER.build("app-config_ok.yaml");
  }

  private static TestWithExpectedResult minRiskThresholdOutOfBoundsNegative() {
    return TEST_BUILDER.build("app-config_mrs_negative.yaml")
        .with(buildError("min-risk-score", RISK_SCORE_MIN - 1, VALUE_OUT_OF_BOUNDS));
  }

  private static TestWithExpectedResult minRiskThresholdOutOfBoundsPositive() {
    return TEST_BUILDER.build("app-config_mrs_oob.yaml")
        .with(buildError("min-risk-score", RISK_SCORE_MAX + 1, VALUE_OUT_OF_BOUNDS));
  }

  private ConfigurationValidator buildApplicationConfigurationValidator(
      DistributionServiceConfig distributionServiceConfig)
      throws UnableToLoadFileException {
    ApplicationConfigurationPublicationConfig applicationConfigurationPublicationConfig = new ApplicationConfigurationPublicationConfig();
    ApplicationConfiguration appConfig = applicationConfigurationPublicationConfig
        .createMainConfiguration(distributionServiceConfig);

    return new ApplicationConfigurationValidator(appConfig);
  }

  public static ValidationResult buildExpectedResult(ValidationError... errors) {
    var validationResult = new ValidationResult();
    Arrays.stream(errors).forEach(validationResult::add);
    return validationResult;
  }
}
