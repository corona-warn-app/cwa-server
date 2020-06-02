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

import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.GeneralValidationError.ErrorType.MIN_GREATER_THAN_MAX;
import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.GeneralValidationError.ErrorType.VALUE_OUT_OF_BOUNDS;
import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.ParameterSpec.ATTENUATION_DURATION_THRESHOLD_MAX;
import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.ParameterSpec.ATTENUATION_DURATION_THRESHOLD_MIN;
import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.RiskScoreClassificationValidatorTest.MINIMAL_RISK_SCORE_CLASSIFICATION;
import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.RiskScoreClassificationValidatorTest.buildExpectedResult;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import app.coronawarn.server.common.protocols.internal.ApplicationConfiguration;
import app.coronawarn.server.common.protocols.internal.AttenuationDurationThresholds;
import app.coronawarn.server.services.distribution.assembly.appconfig.ApplicationConfigurationProvider;
import app.coronawarn.server.services.distribution.assembly.appconfig.ExposureConfigurationProvider;
import app.coronawarn.server.services.distribution.assembly.appconfig.UnableToLoadFileException;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class ApplicationConfigurationValidatorTest {

  private static final ValidationResult SUCCESS = new ValidationResult();

  private static final TestWithExpectedResult.Builder testBuilder = new TestWithExpectedResult.Builder("configtests/");

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

  @ParameterizedTest
  @ValueSource(ints = {ATTENUATION_DURATION_THRESHOLD_MIN - 1, ATTENUATION_DURATION_THRESHOLD_MAX + 1})
  void negativeForAttenuationDurationThresholdOutOfBounds(int invalidThresholdValue) throws Exception {
    ApplicationConfigurationValidator validator = getValidatorForAttenuationDurationThreshold(
        invalidThresholdValue, invalidThresholdValue);

    ValidationResult expectedResult = buildExpectedResult(
        new GeneralValidationError("attenuationDurationThreshold.upper", invalidThresholdValue, VALUE_OUT_OF_BOUNDS),
        new GeneralValidationError("attenuationDurationThreshold.lower", invalidThresholdValue, VALUE_OUT_OF_BOUNDS));

    assertThat(validator.validate()).isEqualTo(expectedResult);
  }

  @Test
  void negativeForUpperAttenuationDurationThresholdLesserThanLower() throws Exception {
    ApplicationConfigurationValidator validator = getValidatorForAttenuationDurationThreshold(
        ATTENUATION_DURATION_THRESHOLD_MAX, ATTENUATION_DURATION_THRESHOLD_MIN);

    ValidationResult expectedResult = buildExpectedResult(
        new GeneralValidationError("attenuationDurationThreshold.lower, attenuationDurationThreshold.upper",
            (ATTENUATION_DURATION_THRESHOLD_MAX + ", " + ATTENUATION_DURATION_THRESHOLD_MIN), MIN_GREATER_THAN_MAX));

    assertThat(validator.validate()).isEqualTo(expectedResult);
  }

  private ApplicationConfigurationValidator getValidatorForAttenuationDurationThreshold(int lower, int upper)
      throws Exception {
    ApplicationConfiguration appConfig = ApplicationConfiguration.newBuilder()
        .setMinRiskScore(100)
        .setRiskScoreClasses(MINIMAL_RISK_SCORE_CLASSIFICATION)
        .setExposureConfig(ExposureConfigurationProvider.readFile("configtests/exposure-config_ok.yaml"))
        .setAttenuationDurationThresholds(AttenuationDurationThresholds.newBuilder()
            .setLower(lower)
            .setUpper(upper)).build();
    return new ApplicationConfigurationValidator(appConfig);
  }

  @Test
  void circular() {
    assertThatThrownBy(() -> {
      ApplicationConfigurationProvider.readFile("configtests/app-config_circular.yaml");
    }).isInstanceOf(UnableToLoadFileException.class);
  }

  private ValidationResult getResultForTest(TestWithExpectedResult test)
      throws UnableToLoadFileException {
    var config = ApplicationConfigurationProvider.readFile(test.path());
    var validator = new ApplicationConfigurationValidator(config);
    return validator.validate();
  }

  private static Stream<Arguments> createOkTests() {
    return Stream.of(
        AllOk()
    ).map(Arguments::of);
  }

  private static Stream<Arguments> createNegativeTests() {
    return Stream.of(
        MinRiskThresholdOutOfBoundsNegative(),
        MinRiskThresholdOutOfBoundsPositive()
    ).map(Arguments::of);
  }

  public static TestWithExpectedResult AllOk() {
    return testBuilder.build("app-config_ok.yaml");
  }

  public static TestWithExpectedResult MinRiskThresholdOutOfBoundsNegative() {
    return testBuilder.build("app-config_mrs_negative.yaml")
        .with(new MinimumRiskLevelValidationError(-1));
  }

  public static TestWithExpectedResult MinRiskThresholdOutOfBoundsPositive() {
    return testBuilder.build("app-config_mrs_oob.yaml")
        .with(new MinimumRiskLevelValidationError(4097));
  }
}
