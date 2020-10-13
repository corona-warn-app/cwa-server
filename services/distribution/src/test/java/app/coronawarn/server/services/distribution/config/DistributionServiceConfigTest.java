

package app.coronawarn.server.services.distribution.config;

import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.ParameterSpec.RISK_SCORE_MAX;
import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.ParameterSpec.RISK_SCORE_MIN;
import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.ValidationError.ErrorType.INVALID_VALUES;
import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.ValidationError.ErrorType.VALUE_OUT_OF_BOUNDS;
import static app.coronawarn.server.services.distribution.common.Helpers.loadApplicationConfiguration;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

import app.coronawarn.server.common.protocols.internal.ApplicationConfiguration;
import app.coronawarn.server.services.distribution.assembly.appconfig.ApplicationConfigurationPublicationConfig;
import app.coronawarn.server.services.distribution.assembly.appconfig.UnableToLoadFileException;
import java.util.Arrays;
import java.util.stream.Stream;

import app.coronawarn.server.services.distribution.assembly.appconfig.validation.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

@EnableConfigurationProperties(value = DistributionServiceConfig.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DistributionServiceConfig.class, ApplicationConfigurationPublicationConfig.class,
    ApplicationConfigurationValidatorTestConfiguration.class},
    initializers = ConfigFileApplicationContextInitializer.class)
@ActiveProfiles("applicationConfigurationValidatorTest")
class DistributionServiceConfigTest {

  private static final ValidationResult SUCCESS = new ValidationResult();
  private static final TestWithExpectedResult.Builder TEST_BUILDER = new TestWithExpectedResult.Builder("configtests/");

  @Autowired
  DistributionServiceConfig distributionServiceConfig;

  DistributionServiceConfigValidator distributionServiceConfigValidator = new DistributionServiceConfigValidator();


  @ParameterizedTest
  @ValueSource(strings = {"DE,FRE", "DE, ", " "})
  void failsOnInvalidSupportedCountries(String supportedCountries) {
    String[] supportedCountriesList = supportedCountries.split(",");
    when(distributionServiceConfig.getSupportedCountries()).thenReturn(supportedCountriesList);

    Errors errors = new BindException(distributionServiceConfig, "distributionServiceConfig");
    distributionServiceConfigValidator.validate(distributionServiceConfig, errors);

    assertThat(errors.getAllErrors()).hasSize(1);
  }

  @ParameterizedTest
  @ValueSource(strings = {"DE,FR", "DE"})
  void successOnValidSupportedCountries(String supportedCountries) throws UnableToLoadFileException {
    String[] supportedCountriesList = supportedCountries.split(",");
    when(distributionServiceConfig.getSupportedCountries()).thenReturn(supportedCountriesList);

    Errors errors = new BindException(distributionServiceConfig, "distributionServiceConfig");
    distributionServiceConfigValidator.validate(distributionServiceConfig, errors);

    assertThat(errors.getAllErrors()).isEmpty();

  }

  public static ValidationResult buildExpectedResult(ValidationError... errors) {
    var validationResult = new ValidationResult();
    Arrays.stream(errors).forEach(validationResult::add);
    return validationResult;
  }
}
