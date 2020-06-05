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
import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.ValidationError.ErrorType.VALUE_OUT_OF_BOUNDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import app.coronawarn.server.services.distribution.assembly.appconfig.ApplicationConfigurationProvider;
import app.coronawarn.server.services.distribution.assembly.appconfig.UnableToLoadFileException;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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
        .with(buildError("min-risk-score", RISK_SCORE_MIN - 1, VALUE_OUT_OF_BOUNDS));
  }

  public static TestWithExpectedResult MinRiskThresholdOutOfBoundsPositive() {
    return testBuilder.build("app-config_mrs_oob.yaml")
        .with(buildError("min-risk-score", RISK_SCORE_MAX + 1, VALUE_OUT_OF_BOUNDS));
  }
}
