/*-
 * ---license-start
 * Corona-Warn-App
 * ---
 * Copyright (C) 2020 SAP SE and all other contributors
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ---license-end
 */

package app.coronawarn.server.services.distribution.assembly.appconfig.validation;

import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.ParameterSpec.RISK_SCORE_MAX;
import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.ParameterSpec.RISK_SCORE_MIN;
import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.RiskScoreClassificationValidatorTest.buildError;
import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.ValidationError.ErrorType.INVALID_VALUES;
import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.ValidationError.ErrorType.VALUE_OUT_OF_BOUNDS;
import static app.coronawarn.server.services.distribution.common.Helpers.loadApplicationConfiguration;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import app.coronawarn.server.common.protocols.internal.ApplicationConfiguration;
import app.coronawarn.server.services.distribution.assembly.appconfig.ApplicationConfigurationPublicationConfig;
import app.coronawarn.server.services.distribution.assembly.appconfig.UnableToLoadFileException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
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

@EnableConfigurationProperties(value = DistributionServiceConfig.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DistributionServiceConfig.class, ApplicationConfigurationPublicationConfig.class},
    initializers = ConfigFileApplicationContextInitializer.class)
class ApplicationConfigurationValidatorTest {

  private static final ValidationResult SUCCESS = new ValidationResult();
  private static final TestWithExpectedResult.Builder TEST_BUILDER = new TestWithExpectedResult.Builder("configtests/");

  @Autowired
  DistributionServiceConfig distributionServiceConfig;

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
    return Stream.of(AllOk()).map(Arguments::of);
  }

  private static Stream<Arguments> createNegativeTests() {
    return Stream.of(
        MinRiskThresholdOutOfBoundsNegative(),
        MinRiskThresholdOutOfBoundsPositive()
    ).map(Arguments::of);
  }

  public static TestWithExpectedResult AllOk() {
    return TEST_BUILDER.build("app-config_ok.yaml");
  }

  public static TestWithExpectedResult MinRiskThresholdOutOfBoundsNegative() {
    return TEST_BUILDER.build("app-config_mrs_negative.yaml")
        .with(buildError("min-risk-score", RISK_SCORE_MIN - 1, VALUE_OUT_OF_BOUNDS));
  }

  public static TestWithExpectedResult MinRiskThresholdOutOfBoundsPositive() {
    return TEST_BUILDER.build("app-config_mrs_oob.yaml")
        .with(buildError("min-risk-score", RISK_SCORE_MAX + 1, VALUE_OUT_OF_BOUNDS));
  }

  private ConfigurationValidator buildApplicationConfigurationValidator(
      DistributionServiceConfig distributionServiceConfig)
      throws UnableToLoadFileException {
    ApplicationConfigurationPublicationConfig applicationConfigurationPublicationConfig = new ApplicationConfigurationPublicationConfig();
    ApplicationConfiguration appConfig = applicationConfigurationPublicationConfig
        .createMasterConfiguration(distributionServiceConfig);

    return new ApplicationConfigurationValidator(appConfig);
  }

  @ParameterizedTest
  @MethodSource("setInvalidSupportedCountries")
  void failsOnInvalidSupportedCountries(String supportedCountries) throws UnableToLoadFileException {
    distributionServiceConfig.setSupportedCountries(supportedCountries);

    var validator = buildApplicationConfigurationValidator(distributionServiceConfig);
    List<String> supportedCountriesList = Arrays.asList(supportedCountries.split(","));
    assertThat(validator.validate()).isEqualToComparingOnlyGivenFields(
        buildExpectedResult(new ValidationError("supported-countries", supportedCountriesList, INVALID_VALUES)));

  }
  private static Stream<Arguments> setInvalidSupportedCountries() {
    return Stream.of(
        Arguments.of("DE,FRE"),
        Arguments.of("DE, "),
        Arguments.of(" ")

    );
  }

  @ParameterizedTest
  @MethodSource("setValidSupportedCountries")
  void successOnValidSupportedCountries(String supportedCountries) throws UnableToLoadFileException {
    distributionServiceConfig.setSupportedCountries(supportedCountries);

    var validator = buildApplicationConfigurationValidator(distributionServiceConfig);

    assertThat(validator.validate()).isEqualTo(SUCCESS);

  }
  private static Stream<Arguments> setValidSupportedCountries() {
    return Stream.of(
        Arguments.of("DE,FR"),
        Arguments.of("DE")
    );
  }
  public static ValidationResult buildExpectedResult(ValidationError... errors) {
    var validationResult = new ValidationResult();
    Arrays.stream(errors).forEach(validationResult::add);
    return validationResult;
  }
}
