

package app.coronawarn.server.services.distribution.config;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import app.coronawarn.server.services.distribution.assembly.appconfig.ApplicationConfigurationPublicationConfig;
import app.coronawarn.server.services.distribution.assembly.appconfig.UnableToLoadFileException;
import app.coronawarn.server.services.distribution.assembly.appconfig.validation.TestWithExpectedResult;
import app.coronawarn.server.services.distribution.assembly.appconfig.validation.ValidationError;
import app.coronawarn.server.services.distribution.assembly.appconfig.validation.ValidationResult;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
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

  @Test
  void testIosKeyDownloadParameters() {
    assertEquals(emptyList(),
        distributionServiceConfig.getAppConfigParameters().getIosKeyDownloadParameters()
            .getCachedDayPackagesToUpdateOnETagMismatch());
    assertEquals(emptyList(),
        distributionServiceConfig.getAppConfigParameters().getIosKeyDownloadParameters()
            .getCachedHourPackagesToUpdateOnETagMismatch());
  }

  @Test
  void testAndroidKeyDownloadParameters() {
    assertEquals(emptyList(),
        distributionServiceConfig.getAppConfigParameters().getAndroidKeyDownloadParameters()
            .getCachedDayPackagesToUpdateOnETagMismatch());
    assertEquals(emptyList(),
        distributionServiceConfig.getAppConfigParameters().getAndroidKeyDownloadParameters()
            .getCachedHourPackagesToUpdateOnETagMismatch());
    assertEquals(30,
        distributionServiceConfig.getAppConfigParameters().getAndroidKeyDownloadParameters()
            .getDownloadTimeoutInSeconds());
    assertEquals(480,
        distributionServiceConfig.getAppConfigParameters().getAndroidKeyDownloadParameters()
            .getOverallTimeoutInSeconds());
  }

  @Test
  void testIosExposureDetectionParameters() {
    assertEquals(6,
        distributionServiceConfig.getAppConfigParameters().getIosExposureDetectionParameters()
            .getMaxExposureDetectionsPerInterval());
  }

  @Test
  void testAndroidExposureDetectionParameters() {
    assertEquals(6,
        distributionServiceConfig.getAppConfigParameters().getAndroidExposureDetectionParameters()
            .getMaxExposureDetectionsPerInterval());
    assertEquals(900,
        distributionServiceConfig.getAppConfigParameters().getAndroidExposureDetectionParameters()
            .getOverallTimeoutInSeconds());
  }

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
