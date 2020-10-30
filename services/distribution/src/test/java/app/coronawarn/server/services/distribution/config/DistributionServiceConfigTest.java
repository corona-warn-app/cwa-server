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
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.AppConfigParameters.AndroidKeyDownloadParameters;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.AppConfigParameters.IosExposureDetectionParameters;
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
  private IosExposureDetectionParameters iosExposureDetectionParameters;
  private AndroidKeyDownloadParameters androidKeyDownloadParameters;
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
    iosExposureDetectionParameters = new IosExposureDetectionParameters();
    androidKeyDownloadParameters = new AndroidKeyDownloadParameters();
    androidExposureDetectionParameters = new AndroidExposureDetectionParameters();
    validator = Validation.buildDefaultValidatorFactory().getValidator();
  }

  @Test
  void testDefaultIosKeyDownloadParameters() {
    assertEquals("[]",
        distributionServiceConfig.getAppConfigParameters().getIosKeyDownloadParameters()
            .getCachedDayPackagesToUpdateOnETagMismatch());
    assertEquals("[]",
        distributionServiceConfig.getAppConfigParameters().getIosKeyDownloadParameters()
            .getCachedHourPackagesToUpdateOnETagMismatch());
  }

  @Test
  void testDefaultAndroidKeyDownloadParameters() {
    assertEquals("[]",
        distributionServiceConfig.getAppConfigParameters().getAndroidKeyDownloadParameters()
            .getCachedDayPackagesToUpdateOnETagMismatch());
    assertEquals("[]",
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
  void testDefaultIosExposureDetectionParameters() {
    assertEquals(6,
        distributionServiceConfig.getAppConfigParameters().getIosExposureDetectionParameters()
            .getMaxExposureDetectionsPerInterval());
  }

  @Test
  void testDefaultAndroidExposureDetectionParameters() {
    assertEquals(6,
        distributionServiceConfig.getAppConfigParameters().getAndroidExposureDetectionParameters()
            .getMaxExposureDetectionsPerInterval());
    assertEquals(900,
        distributionServiceConfig.getAppConfigParameters().getAndroidExposureDetectionParameters()
            .getOverallTimeoutInSeconds());
  }

  @Test
  void testLowerBoundaryIosDetectionParametersMaxExposureDetectionsPerInterval() {
    iosExposureDetectionParameters.setMaxExposureDetectionsPerInterval(-1);
    validator.validate(iosExposureDetectionParameters).stream().findFirst()
        .ifPresentOrElse(violation -> assertEquals(
            IosExposureDetectionParameters.MIN_VALUE_ERROR_MESSAGE_MAX_EXPOSURE_DETECTIONS,
            violation.getMessage()),
            Assertions::fail);


  }

  @Test
  void testUpperBoundaryIosDetectionParametersMaxExposureDetectionsPerInterval() {
    iosExposureDetectionParameters.setMaxExposureDetectionsPerInterval(7);
    validator.validate(iosExposureDetectionParameters).stream().findFirst()
        .ifPresentOrElse(
            violation -> assertEquals(
                IosExposureDetectionParameters.MAX_VALUE_ERROR_MESSAGE_MAX_EXPOSURE_DETECTIONS,
                violation.getMessage()),
            Assertions::fail);


  }

  @Test
  void testLowerBoundaryAndroidKeyDownloadParametersDownloadTimeout() {
    androidKeyDownloadParameters.setDownloadTimeoutInSeconds(-1);
    validator.validate(androidKeyDownloadParameters).stream().findFirst()
        .ifPresentOrElse(violation -> assertEquals(
            AndroidKeyDownloadParameters.MIN_VALUE_ERROR_MESSAGE_DOWNLOAD_TIMEOUT,
            violation.getMessage()),
            Assertions::fail);


  }

  @Test
  void testUpperBoundaryAndroidKeyDownloadParametersDownloadTimeout() {
    androidKeyDownloadParameters.setDownloadTimeoutInSeconds(121);
    validator.validate(androidKeyDownloadParameters).stream().findFirst()
        .ifPresentOrElse(
            violation -> assertEquals(
                AndroidKeyDownloadParameters.MAX_VALUE_ERROR_MESSAGE_DOWNLOAD_TIMEOUT,
                violation.getMessage()),
            Assertions::fail);


  }


  @Test
  void testLowerBoundaryAndroidKeyDownloadParametersOverallTimeout() {
    androidKeyDownloadParameters.setOverallTimeoutInSeconds(-1);
    validator.validate(androidKeyDownloadParameters).stream().findFirst()
        .ifPresentOrElse(violation -> assertEquals(
            AndroidKeyDownloadParameters.MIN_VALUE_ERROR_MESSAGE_OVERALL_TIMEOUT,
            violation.getMessage()),
            Assertions::fail);


  }

  @Test
  void testUpperBoundaryAndroidKeyDownloadParametersOverallTimeout() {
    androidKeyDownloadParameters.setOverallTimeoutInSeconds(1801);
    validator.validate(androidKeyDownloadParameters).stream().findFirst()
        .ifPresentOrElse(
            violation -> assertEquals(
                AndroidKeyDownloadParameters.MAX_VALUE_ERROR_MESSAGE_OVERALL_TIMEOUT,
                violation.getMessage()),
            Assertions::fail);


  }

  @Test
  void testLowerBoundaryAndroidDetectionParametersMaxExposureDetectionsPerInterval() {
    androidExposureDetectionParameters.setMaxExposureDetectionsPerInterval(-1);
    validator.validate(androidExposureDetectionParameters).stream().findFirst()
        .ifPresentOrElse(violation -> assertEquals(
            AndroidExposureDetectionParameters.MIN_VALUE_ERROR_MESSAGE_MAX_EXPOSURE_DETECTIONS,
            violation.getMessage()),
            Assertions::fail);


  }

  @Test
  void testUpperBoundaryAndroidDetectionParametersMaxExposureDetectionsPerInterval() {
    androidExposureDetectionParameters.setMaxExposureDetectionsPerInterval(7);
    validator.validate(androidExposureDetectionParameters).stream().findFirst()
        .ifPresentOrElse(
            violation -> assertEquals(
                AndroidExposureDetectionParameters.MAX_VALUE_ERROR_MESSAGE_MAX_EXPOSURE_DETECTIONS,
                violation.getMessage()),
            Assertions::fail);


  }

  @Test
  void testLowerBoundaryAndroiDetectionParametersOverallTimeout() {
    androidExposureDetectionParameters.setOverallTimeoutInSeconds(-1);
    validator.validate(androidExposureDetectionParameters).stream().findFirst()
        .ifPresentOrElse(violation -> assertEquals(
            AndroidExposureDetectionParameters.MIN_VALUE_ERROR_MESSAGE_OVERALL_TIMEOUT,
            violation.getMessage()),
            Assertions::fail);


  }

  @Test
  void testUpperBoundaryAndroidDetectionParametersOverallTimeout() {
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
