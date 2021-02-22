

package app.coronawarn.server.services.distribution.assembly.appconfig.validation;

import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.ApplicationVersionConfigurationValidator.CONFIG_PREFIX;
import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.RiskScoreClassificationValidatorTest.buildError;
import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.RiskScoreClassificationValidatorTest.buildExpectedResult;
import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.common.protocols.internal.ApplicationVersionConfiguration;
import app.coronawarn.server.common.protocols.internal.ExposureDetectionParametersAndroid;
import app.coronawarn.server.common.protocols.internal.ExposureDetectionParametersIOS;
import app.coronawarn.server.common.protocols.internal.KeyDownloadParametersAndroid;
import app.coronawarn.server.services.distribution.assembly.appconfig.ApplicationConfigurationPublicationConfig;
import app.coronawarn.server.services.distribution.assembly.appconfig.validation.ValidationError.ErrorType;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.util.Arrays;
import org.junit.jupiter.api.AfterAll;


@EnableConfigurationProperties(value = DistributionServiceConfig.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DistributionServiceConfig.class, ApplicationConfigurationPublicationConfig.class},
    initializers = ConfigFileApplicationContextInitializer.class)
class ApplicationVersionConfigurationValidatorTest {

  private static final ValidationResult SUCCESS = new ValidationResult();

  @Autowired
  DistributionServiceConfig distributionServiceConfig;

  @Autowired
  ApplicationConfigurationPublicationConfig applicationConfigurationPublicationConfig;

  @ParameterizedTest
  @MethodSource("setSemanticVersionsLatestHigherThanMin")
  void succeedsIfLatestHigherThanMin(String latestAndroid, String minAndroid, String latestIos, String minIos) {
    distributionServiceConfig.getAppVersions().setLatestAndroid(latestAndroid);
    distributionServiceConfig.getAppVersions().setMinAndroid(minAndroid);
    distributionServiceConfig.getAppVersions().setLatestIos(latestIos);
    distributionServiceConfig.getAppVersions().setMinIos(minIos);

    var validator = buildVersionValidator(distributionServiceConfig);
    assertThat(validator.validate()).isEqualTo(SUCCESS);
  }

  private static Stream<Arguments> setSemanticVersionsLatestHigherThanMin() {
    return Stream.of(
        Arguments.of("2.0.0", "1.0.0", "1.0.0", "1.0.0"),
        Arguments.of("0.2.0", "0.1.0", "1.0.0", "1.0.0"),
        Arguments.of("0.0.2", "0.0.1", "1.0.0", "1.0.0"),
        Arguments.of("1.0.0", "1.0.0", "2.0.0", "1.0.0"),
        Arguments.of("1.0.0", "1.0.0", "0.2.0", "0.1.0"),
        Arguments.of("1.0.0", "1.0.0", "0.0.2", "0.0.1")
    );
  }

  @ParameterizedTest
  @MethodSource("setSemanticVersionsLatestEqualsMin")
  void succeedsWithEqualSemanticVersion(String latestAndroid, String minAndroid, String latestIos, String minIos) {

    distributionServiceConfig.getAppVersions().setLatestAndroid(latestAndroid);
    distributionServiceConfig.getAppVersions().setMinAndroid(minAndroid);
    distributionServiceConfig.getAppVersions().setLatestIos(latestIos);
    distributionServiceConfig.getAppVersions().setMinIos(minIos);

    var validator = buildVersionValidator(distributionServiceConfig);
    assertThat(validator.validate()).isEqualTo(SUCCESS);
  }

  private static Stream<Arguments> setSemanticVersionsLatestEqualsMin() {
    return Stream.of(
        Arguments.of("1.0.0", "1.0.0", "1.0.0", "1.0.0"),
        Arguments.of("0.1.0", "0.1.0", "1.0.0", "1.0.0"),
        Arguments.of("0.0.1", "0.0.1", "1.0.0", "1.0.0"),
        Arguments.of("1.0.0", "1.0.0", "1.0.0", "1.0.0"),
        Arguments.of("1.0.0", "1.0.0", "0.1.0", "0.1.0"),
        Arguments.of("1.0.0", "1.0.0", "0.0.1", "0.0.1")
    );
  }

  @ParameterizedTest
  @MethodSource("setSemanticVersionsLatestLowerThanMinAndroid")
  void failsWithBadSemanticVersionAndroid(String latestAndroid, String minAndroid, String latestIos, String minIos) {

    distributionServiceConfig.getAppVersions().setLatestAndroid(latestAndroid);
    distributionServiceConfig.getAppVersions().setMinAndroid(minAndroid);
    distributionServiceConfig.getAppVersions().setLatestIos(latestIos);
    distributionServiceConfig.getAppVersions().setMinIos(minIos);

    var validator = buildVersionValidator(distributionServiceConfig);

    assertThat(validator.validate()).isEqualTo(buildExpectedResult(
        buildError(CONFIG_PREFIX + "android.[latest|min]", minAndroid, ErrorType.MIN_GREATER_THAN_MAX)));
  }

  private static Stream<Arguments> setSemanticVersionsLatestLowerThanMinAndroid() {
    return Stream.of(
        Arguments.of("1.0.0", "2.0.0", "1.0.0", "1.0.0"),
        Arguments.of("1.0.0", "1.1.0", "1.0.0", "1.0.0"),
        Arguments.of("1.0.0", "1.0.1", "1.0.0", "1.0.0")
    );
  }

  @ParameterizedTest
  @MethodSource("setSemanticVersionsLatestLowerThanMinIos")
  void failsWithBadSemanticVersionIos(String latestAndroid, String minAndroid, String latestIos, String minIos) {

    distributionServiceConfig.getAppVersions().setLatestAndroid(latestAndroid);
    distributionServiceConfig.getAppVersions().setMinAndroid(minAndroid);
    distributionServiceConfig.getAppVersions().setLatestIos(latestIos);
    distributionServiceConfig.getAppVersions().setMinIos(minIos);

    var validator = buildVersionValidator(distributionServiceConfig);

    assertThat(validator.validate()).isEqualTo(
        buildExpectedResult(buildError(CONFIG_PREFIX + "ios.[latest|min]", minIos, ErrorType.MIN_GREATER_THAN_MAX)));
  }

  @Nested
  @DisplayName("AppConfigParameters Tests")
  class AppConfigParameterTest {

    @Test
    void testBuildKeyDownloadParametersAndroid() {
      KeyDownloadParametersAndroid parametersAndroid =
          applicationConfigurationPublicationConfig.buildKeyDownloadParametersAndroid(distributionServiceConfig);

      assertThat(parametersAndroid.getDownloadTimeoutInSeconds()).isEqualTo(30);
      assertThat(parametersAndroid.getOverallTimeoutInSeconds()).isEqualTo(480);
    }

    @Test
    void testExposureDetectionParametersAndroid() {
      ExposureDetectionParametersAndroid exposureDetectionParametersAndroid =
          applicationConfigurationPublicationConfig.buildExposureDetectionParametersAndroid(distributionServiceConfig);

      assertThat(exposureDetectionParametersAndroid.getMaxExposureDetectionsPerInterval()).isEqualTo(6);
      assertThat(exposureDetectionParametersAndroid.getOverallTimeoutInSeconds()).isEqualTo(900);
    }

    @Test
    void testExposureDetectionParametersIos() {
      ExposureDetectionParametersIOS exposureDetectionParametersIOS =
          applicationConfigurationPublicationConfig.buildExposureDetectionParametersIos(distributionServiceConfig);

      assertThat(exposureDetectionParametersIOS.getMaxExposureDetectionsPerInterval()).isEqualTo(6);
    }

    @Test
    void testCachedDayAndHourPackagesForKeyDownloadParameters() {
      distributionServiceConfig.getAppConfigParameters().getAndroidKeyDownloadParameters().
          setRevokedDayPackages("[\n{ \"region\":\"EUR\", \"date\":\"2020-10-28\", \"etag\":"
              + "\"\\\"7d595060d664c4040ff3f65b532f6a57\\\"\" },\n"
              + "{ \"region\":\"DE\", \"date\":\"2020-10-29\", \"etag\":"
              + "\"\\\"7d595060d664c4040ff3f65b532f6a58\\\"\" }]");
      distributionServiceConfig.getAppConfigParameters().getAndroidKeyDownloadParameters()
          .setRevokedHourPackages("[\n{ \"region\":\"EUR\", \"date\":\"2020-10-28\", \"hour\":3, "
              + "\"etag\":\"\\\"7d595060d664c4040ff3f65b532f6a57\\\"\" },\n"
              + "{ \"region\":\"EUR\", \"date\":\"2020-10-29\", \"hour\":5, "
              + "\"etag\":\"\\\"7d595060d664c4040ff3f65b532f6a57\\\"\" }]");
      KeyDownloadParametersAndroid keyDownloadParametersAndroid =
          applicationConfigurationPublicationConfig.buildKeyDownloadParametersAndroid(distributionServiceConfig);

      assertThat(keyDownloadParametersAndroid.getRevokedDayPackages(0).toString())
          .hasToString("region: \"EUR\"\ndate: \"2020-10-28\"\netag:"
              + " \"\\\"7d595060d664c4040ff3f65b532f6a57\\\"\"\n");
      assertThat(keyDownloadParametersAndroid.getRevokedDayPackages(1).toString())
          .hasToString("region: \"DE\"\ndate: \"2020-10-29\"\netag:"
              + " \"\\\"7d595060d664c4040ff3f65b532f6a58\\\"\"\n");
      assertThat(keyDownloadParametersAndroid.getRevokedHourPackages(0).toString())
          .hasToString("region: \"EUR\"\ndate: \"2020-10-28\"\nhour: 3\netag:"
              + " \"\\\"7d595060d664c4040ff3f65b532f6a57\\\"\"\n");
      assertThat(keyDownloadParametersAndroid.getRevokedHourPackages(1).toString())
          .hasToString("region: \"EUR\"\ndate: \"2020-10-29\"\nhour: 5\netag:"
              + " \"\\\"7d595060d664c4040ff3f65b532f6a57\\\"\"\n");
    }
  }

  private static Stream<Arguments> setSemanticVersionsLatestLowerThanMinIos() {
    return Stream.of(

        Arguments.of("1.0.0", "1.0.0", "1.0.0", "2.0.0"),
        Arguments.of("1.0.0", "1.0.0", "1.0.0", "1.1.0"),
        Arguments.of("1.0.0", "1.0.0", "1.0.0", "1.0.1")
    );
  }

  private ConfigurationValidator buildVersionValidator(DistributionServiceConfig distributionServiceConfig) {
    ApplicationVersionConfiguration appConfig = applicationConfigurationPublicationConfig
        .buildApplicationVersionConfiguration(distributionServiceConfig);
    return new ApplicationVersionConfigurationValidator(appConfig);
  }


@AfterAll
static void checkCoverage() {
  System.out.println(
    System.lineSeparator() + System.lineSeparator() +
    "===========================================" + System.lineSeparator() +
    "| DD2480 COVERAGE TOOL                    |" + System.lineSeparator() +
    "| ApplicationVersionConfigurationValidator|" + System.lineSeparator() +
    "| compare()                               |" + System.lineSeparator() +
    "===========================================" + System.lineSeparator());

  System.out.println(Arrays.toString(ApplicationVersionConfigurationValidator.compareCov));

  System.out.println(System.lineSeparator());
}
}