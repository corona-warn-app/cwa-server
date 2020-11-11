package app.coronawarn.server.services.distribution.config;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import app.coronawarn.server.services.distribution.assembly.appconfig.ApplicationConfigurationPublicationConfig;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.AppConfigParameters.AndroidExposureDetectionParameters;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.AppConfigParameters.AndroidKeyDownloadParameters;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.AppConfigParameters.IosExposureDetectionParameters;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.AppVersions;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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

  private IosExposureDetectionParameters iosExposureDetectionParameters;
  private AndroidKeyDownloadParameters androidKeyDownloadParameters;
  private AndroidExposureDetectionParameters androidExposureDetectionParameters;
  @Autowired
  DistributionServiceConfig distributionServiceConfig;
  DistributionServiceConfigValidator distributionServiceConfigValidator = new DistributionServiceConfigValidator();
  private Validator validator;

  @BeforeEach
  void setup() {
    iosExposureDetectionParameters = new IosExposureDetectionParameters();
    androidKeyDownloadParameters = new AndroidKeyDownloadParameters();
    androidExposureDetectionParameters = new AndroidExposureDetectionParameters();
    validator = Validation.buildDefaultValidatorFactory().getValidator();
  }

  @Nested
  class SupportedCountriesTest {

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
    void successOnValidSupportedCountries(String supportedCountries) {
      String[] supportedCountriesList = supportedCountries.split(",");
      when(distributionServiceConfig.getSupportedCountries()).thenReturn(supportedCountriesList);

      Errors errors = new BindException(distributionServiceConfig, "distributionServiceConfig");
      distributionServiceConfigValidator.validate(distributionServiceConfig, errors);

      assertThat(errors.getAllErrors()).isEmpty();

    }
  }

  @Nested
  class AndroidVersioningParametersTest {

    @ParameterizedTest
    @ValueSource(ints = {-14,-1})
    void failsOnInvalidAndroidAppVersionCodes(Integer invalidVersionCode) {
      AppVersions appVersions = new AppVersions();
      appVersions.setMinAndroidVersionCode(invalidVersionCode);
      appVersions.setLatestAndroidVersionCode(invalidVersionCode);
      when(distributionServiceConfig.getAppVersions()).thenReturn(appVersions);

      Errors errors = new BindException(distributionServiceConfig, "distributionServiceConfig");
      distributionServiceConfigValidator.validate(distributionServiceConfig, errors);

      assertThat(errors.getAllErrors()).hasSize(2);
    }

    @ParameterizedTest
    @ValueSource(ints = {0,31})
    void successOnValidAndroidAppVersionCodes(Integer validVersionCode) {
      AppVersions appVersions = new AppVersions();
      appVersions.setMinAndroidVersionCode(validVersionCode);
      appVersions.setLatestAndroidVersionCode(validVersionCode);
      when(distributionServiceConfig.getAppVersions()).thenReturn(appVersions);

      Errors errors = new BindException(distributionServiceConfig, "distributionServiceConfig");
      distributionServiceConfigValidator.validate(distributionServiceConfig, errors);

      assertThat(errors.getAllErrors()).isEmpty();

    }
  }

  @Nested
  class IosExposureDetectionParametersTest {

    @ParameterizedTest
    @MethodSource("app.coronawarn.server.services.distribution.config.DistributionServiceConfigTest#iosDetectionParametersMaxExposureDetectionsPerIntervalArguments")
    void testIosDetectionParametersMaxExposureDetectionsPerIntervalBoundaries(Integer maxDetectionsPerInterval,
        String errorMessage) {
      iosExposureDetectionParameters.setMaxExposureDetectionsPerInterval(maxDetectionsPerInterval);
      validate(iosExposureDetectionParameters, errorMessage);
    }
  }

  @Nested
  class AndroidExposureDetectionParametersTest {

    @ParameterizedTest
    @MethodSource("app.coronawarn.server.services.distribution.config.DistributionServiceConfigTest#androidDetectionParametersOverAllTimeoutBoundariesArguments")
    void testOverAllTimeoutBoundaries(Integer overallTimeout, String errorMessage) {
      androidExposureDetectionParameters.setOverallTimeoutInSeconds(overallTimeout);
      validate(androidExposureDetectionParameters, errorMessage);
    }

    @ParameterizedTest
    @MethodSource("app.coronawarn.server.services.distribution.config.DistributionServiceConfigTest#androidDetectionParametersMaxExposureDetectionsPerIntervalArguments")
    void testMaxExposureDetectionsPerIntervalBoundaries(Integer overallTimeout,
        String errorMessage) {
      androidExposureDetectionParameters.setMaxExposureDetectionsPerInterval(overallTimeout);
      validate(androidExposureDetectionParameters, errorMessage);
    }
  }

  @Nested
  class AppConfigParametersValuesTest {

    @Test
    void testIosKeyDownloadParameters() {
      assertEquals(emptyList(),
          distributionServiceConfig.getAppConfigParameters().getIosKeyDownloadParameters()
              .getRevokedDayPackages());
      assertEquals(emptyList(),
          distributionServiceConfig.getAppConfigParameters().getIosKeyDownloadParameters()
              .getRevokedHourPackages());
    }

    @Test
    void testAndroidKeyDownloadParameters() {
      assertEquals(emptyList(),
          distributionServiceConfig.getAppConfigParameters().getAndroidKeyDownloadParameters()
              .getRevokedDayPackages());
      assertEquals(emptyList(),
          distributionServiceConfig.getAppConfigParameters().getAndroidKeyDownloadParameters()
              .getRevokedHourPackages());
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
  }

  @Nested
  class AndroidKeyDownloadParametersTest {

    @ParameterizedTest
    @MethodSource("app.coronawarn.server.services.distribution.config.DistributionServiceConfigTest#androidKeyDownloadParametersDownloadTimeoutArguments")
    void testDownloadTimeoutBoundaries(Integer downloadTimeout, String errorMessage) {
      androidKeyDownloadParameters.setDownloadTimeoutInSeconds(downloadTimeout);
      validate(androidKeyDownloadParameters, errorMessage);
    }

    @ParameterizedTest
    @MethodSource("app.coronawarn.server.services.distribution.config.DistributionServiceConfigTest#androidKeyDownloadParametersOverallTimeoutArguments")
    void testOverallTimeoutBoundaries(Integer overallTimeout, String errorMessage) {
      androidKeyDownloadParameters.setOverallTimeoutInSeconds(overallTimeout);
      validate(androidKeyDownloadParameters, errorMessage);
    }
  }

  private static Stream<Arguments> iosDetectionParametersMaxExposureDetectionsPerIntervalArguments() {
    return Stream.of(
        Arguments.of(-1, IosExposureDetectionParameters.MIN_VALUE_ERROR_MESSAGE_MAX_EXPOSURE_DETECTIONS),
        Arguments.of(7, IosExposureDetectionParameters.MAX_VALUE_ERROR_MESSAGE_MAX_EXPOSURE_DETECTIONS)
    );
  }

  private static Stream<Arguments> androidKeyDownloadParametersDownloadTimeoutArguments() {
    return Stream.of(
        Arguments.of(-1, AndroidKeyDownloadParameters.MIN_VALUE_ERROR_MESSAGE_DOWNLOAD_TIMEOUT),
        Arguments.of(1801, AndroidKeyDownloadParameters.MAX_VALUE_ERROR_MESSAGE_DOWNLOAD_TIMEOUT)
    );
  }

  private static Stream<Arguments> androidKeyDownloadParametersOverallTimeoutArguments() {
    return Stream.of(
        Arguments.of(-1, AndroidKeyDownloadParameters.MIN_VALUE_ERROR_MESSAGE_OVERALL_TIMEOUT),
        Arguments.of(1801, AndroidKeyDownloadParameters.MAX_VALUE_ERROR_MESSAGE_OVERALL_TIMEOUT)
    );
  }

  private static Stream<Arguments> androidDetectionParametersMaxExposureDetectionsPerIntervalArguments() {
    return Stream.of(
        Arguments.of(-1, AndroidExposureDetectionParameters.MIN_VALUE_ERROR_MESSAGE_MAX_EXPOSURE_DETECTIONS),
        Arguments.of(7, AndroidExposureDetectionParameters.MAX_VALUE_ERROR_MESSAGE_MAX_EXPOSURE_DETECTIONS)
    );
  }

  private static Stream<Arguments> androidDetectionParametersOverAllTimeoutBoundariesArguments() {
    return Stream.of(
        Arguments.of(-1, AndroidExposureDetectionParameters.MIN_VALUE_ERROR_MESSAGE_OVERALL_TIMEOUT),
        Arguments.of(3601, AndroidExposureDetectionParameters.MAX_VALUE_ERROR_MESSAGE_OVERALL_TIMEOUT)
    );
  }

  private <T> void validate(T property, String errorMessage) {
    validator.validate(property)
        .stream()
        .findAny()
        .ifPresentOrElse(assertViolationIsEqualTo(errorMessage), Assertions::fail);
  }

  private <T> Consumer<ConstraintViolation<T>> assertViolationIsEqualTo(String errorMessage) {
    return violation -> assertEquals(errorMessage, violation.getMessage());
  }

}
