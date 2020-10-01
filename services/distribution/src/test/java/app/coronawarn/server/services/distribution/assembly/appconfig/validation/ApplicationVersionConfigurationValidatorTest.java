

package app.coronawarn.server.services.distribution.assembly.appconfig.validation;

import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.ApplicationVersionConfigurationValidator.CONFIG_PREFIX;
import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.RiskScoreClassificationValidatorTest.buildError;
import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.RiskScoreClassificationValidatorTest.buildExpectedResult;
import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.common.protocols.internal.ApplicationVersionConfiguration;
import app.coronawarn.server.services.distribution.assembly.appconfig.ApplicationConfigurationPublicationConfig;
import app.coronawarn.server.services.distribution.assembly.appconfig.validation.ValidationError.ErrorType;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

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
}
