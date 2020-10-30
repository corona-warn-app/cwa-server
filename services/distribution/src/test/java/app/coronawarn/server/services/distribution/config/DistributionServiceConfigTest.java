package app.coronawarn.server.services.distribution.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import app.coronawarn.server.services.distribution.assembly.appconfig.ApplicationConfigurationPublicationConfig;
import app.coronawarn.server.services.distribution.assembly.appconfig.UnableToLoadFileException;
import app.coronawarn.server.services.distribution.assembly.appconfig.validation.TestWithExpectedResult;
import app.coronawarn.server.services.distribution.assembly.appconfig.validation.ValidationError;
import app.coronawarn.server.services.distribution.assembly.appconfig.validation.ValidationResult;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.AppConfigParameters.AndroidExposureDetectionParameters;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.AppConfigParameters.IosExposureDetectionParameters;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.AppConfigParameters.KeyDownloadParameters;
import java.util.Arrays;
import javax.validation.Validation;
import javax.validation.Validator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
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
  private KeyDownloadParameters keyDownloadParameters;
  private IosExposureDetectionParameters iosExposureDetectionParameters;
  private AndroidExposureDetectionParameters androidExposureDetectionParameters;
  private Validator validator;

  public static ValidationResult buildExpectedResult(ValidationError... errors) {
    var validationResult = new ValidationResult();
    Arrays.stream(errors).forEach(validationResult::add);
    return validationResult;
  }

  private

  @BeforeEach
  void setup() {
    keyDownloadParameters = new KeyDownloadParameters();
    iosExposureDetectionParameters = new IosExposureDetectionParameters();
    androidExposureDetectionParameters = new AndroidExposureDetectionParameters();
    validator = Validation.buildDefaultValidatorFactory().getValidator();
  }

  @Test
  void testDefaultValuesKeyDownloadParameters() {
    assertEquals(3,
        distributionServiceConfig.getAppConfigParameters().getKeyDownloadParameters().getNumberOfRetriesPerFile());
    assertEquals(30,
        distributionServiceConfig.getAppConfigParameters().getKeyDownloadParameters().getHttpTimeoutInSeconds());
    assertEquals(480,
        distributionServiceConfig.getAppConfigParameters().getKeyDownloadParameters().getOverallTimeoutInSeconds());
  }

  @Test
  void testDefaultIosExposureDetectionParameters() {
    assertEquals(6, distributionServiceConfig.getAppConfigParameters().getIosExposureDetectionParameters()
        .getMaxExposureDetectionsPerInterval());
    assertEquals(900, distributionServiceConfig.getAppConfigParameters().getIosExposureDetectionParameters()
        .getOverallTimeoutInSeconds());
  }

  @Test
  void testDefaultAndroidExposureDetectionParameters() {
    assertEquals(6, distributionServiceConfig.getAppConfigParameters().getAndroidExposureDetectionParameters()
        .getMaxExposureDetectionsPerInterval());
    assertEquals(900, distributionServiceConfig.getAppConfigParameters().getAndroidExposureDetectionParameters()
        .getOverallTimeoutInSeconds());
  }

  @Test
  void testLowerBoundaryMaxNumberOfRetriesPerFile() {
    keyDownloadParameters.setNumberOfRetriesPerFile(-3);
    validator.validate(keyDownloadParameters).stream().findFirst()
        .ifPresentOrElse(violation -> assertEquals(KeyDownloadParameters.MIN_VALUE_ERROR_MESSAGE_NUMBER_OF_RETRIES,
            violation.getMessage()),
            Assertions::fail);


  }

  @Test
  void testUpperBoundaryMaxNumberOfRetriesPerFile() {
    keyDownloadParameters.setNumberOfRetriesPerFile(500);
    validator.validate(keyDownloadParameters).stream().findFirst()
        .ifPresentOrElse(violation -> assertEquals(KeyDownloadParameters.MAX_VALUE_ERROR_MESSAGE_NUMBER_OF_RETRIES,
            violation.getMessage()),
            Assertions::fail);


  }

  @Test
  void testLowerBoundaryHttpTimeout() {
    keyDownloadParameters.setHttpTimeoutInSeconds(-200);
    validator.validate(keyDownloadParameters).stream().findFirst()
        .ifPresentOrElse(violation -> assertEquals(KeyDownloadParameters.MIN_VALUE_ERROR_MESSAGE_HTTP_TIMEOUT,
            violation.getMessage()),
            Assertions::fail);


  }

  @Test
  void testUpperBoundaryHttpTimeout() {
    keyDownloadParameters.setHttpTimeoutInSeconds(121);
    validator.validate(keyDownloadParameters).stream().findFirst()
        .ifPresentOrElse(
            violation -> assertEquals(KeyDownloadParameters.MAX_VALUE_ERROR_MESSAGE_HTTP_TIMEOUT,
                violation.getMessage()),
            Assertions::fail);


  }

  @Test
  void testLowerBoundaryKeyDownloadOverallTimeout() {
    keyDownloadParameters.setOverallTimeoutInSeconds(-1);
    validator.validate(keyDownloadParameters).stream().findFirst()
        .ifPresentOrElse(
            violation -> assertEquals(KeyDownloadParameters.MIN_VALUE_ERROR_MESSAGE_OVERALL_TIMEOUT,
                violation.getMessage()),
            Assertions::fail);


  }

  @Test
  void testUpperBoundaryKeyDownloadOverallTimeout() {
    keyDownloadParameters.setOverallTimeoutInSeconds(2000);
    validator.validate(keyDownloadParameters).stream().findFirst()
        .ifPresentOrElse(
            violation -> assertEquals(KeyDownloadParameters.MAX_VALUE_ERROR_MESSAGE_OVERALL_TIMEOUT,
                violation.getMessage()),
            Assertions::fail);


  }

  @Test
  void testLowerBoundaryIosMaxExposureDetectionsPerInterval() {
    iosExposureDetectionParameters.setMaxExposureDetectionsPerInterval(-1);
    validator.validate(iosExposureDetectionParameters).stream().findFirst()
        .ifPresentOrElse(violation -> assertEquals(
            IosExposureDetectionParameters.MIN_VALUE_ERROR_MESSAGE_MAX_EXPOSURE_DETECTIONS,
            violation.getMessage()),
            Assertions::fail);


  }

  @Test
  void testUpperBoundaryIosMaxExposureDetectionsPerInterval() {
    iosExposureDetectionParameters.setMaxExposureDetectionsPerInterval(7);
    validator.validate(iosExposureDetectionParameters).stream().findFirst()
        .ifPresentOrElse(
            violation -> assertEquals(
                IosExposureDetectionParameters.MAX_VALUE_ERROR_MESSAGE_MAX_EXPOSURE_DETECTIONS,
                violation.getMessage()),
            Assertions::fail);


  }

  @Test
  void testLowerBoundaryIosOverallTimeout() {
    iosExposureDetectionParameters.setOverallTimeoutInSeconds(-1);
    validator.validate(iosExposureDetectionParameters).stream().findFirst()
        .ifPresentOrElse(violation -> assertEquals(
            IosExposureDetectionParameters.MIN_VALUE_ERROR_MESSAGE_OVERALL_TIMEOUT,
            violation.getMessage()),
            Assertions::fail);


  }

  @Test
  void testUpperBoundaryIosOverallTimeout() {
    iosExposureDetectionParameters.setOverallTimeoutInSeconds(3601);
    validator.validate(iosExposureDetectionParameters).stream().findFirst()
        .ifPresentOrElse(
            violation -> assertEquals(
                IosExposureDetectionParameters.MAX_VALUE_ERROR_MESSAGE_OVERALL_TIMEOUT,
                violation.getMessage()),
            Assertions::fail);


  }

  @Test
  void testLowerBoundaryAndroidMaxExposureDetectionsPerInterval() {
    androidExposureDetectionParameters.setMaxExposureDetectionsPerInterval(-1);
    validator.validate(androidExposureDetectionParameters).stream().findFirst()
        .ifPresentOrElse(violation -> assertEquals(
            AndroidExposureDetectionParameters.MIN_VALUE_ERROR_MESSAGE_MAX_EXPOSURE_DETECTIONS,
            violation.getMessage()),
            Assertions::fail);


  }

  @Test
  void testUpperBoundaryAndroidMaxExposureDetectionsPerInterval() {
    androidExposureDetectionParameters.setMaxExposureDetectionsPerInterval(7);
    validator.validate(androidExposureDetectionParameters).stream().findFirst()
        .ifPresentOrElse(
            violation -> assertEquals(
                AndroidExposureDetectionParameters.MAX_VALUE_ERROR_MESSAGE_MAX_EXPOSURE_DETECTIONS,
                violation.getMessage()),
            Assertions::fail);


  }

  @Test
  void testLowerBoundaryAndroidOverallTimeout() {
    androidExposureDetectionParameters.setOverallTimeoutInSeconds(-1);
    validator.validate(androidExposureDetectionParameters).stream().findFirst()
        .ifPresentOrElse(violation -> assertEquals(
            AndroidExposureDetectionParameters.MIN_VALUE_ERROR_MESSAGE_OVERALL_TIMEOUT,
            violation.getMessage()),
            Assertions::fail);


  }

  @Test
  void testUpperBoundaryAndroidOverallTimeout() {
    androidExposureDetectionParameters.setOverallTimeoutInSeconds(3601);
    validator.validate(androidExposureDetectionParameters).stream().findFirst()
        .ifPresentOrElse(
            violation -> assertEquals(
                AndroidExposureDetectionParameters.MAX_VALUE_ERROR_MESSAGE_OVERALL_TIMEOUT,
                violation.getMessage()),
            Assertions::fail);


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
}
