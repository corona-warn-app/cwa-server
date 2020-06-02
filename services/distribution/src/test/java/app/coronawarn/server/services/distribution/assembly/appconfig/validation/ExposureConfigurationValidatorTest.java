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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import app.coronawarn.server.services.distribution.assembly.appconfig.ExposureConfigurationProvider;
import app.coronawarn.server.services.distribution.assembly.appconfig.UnableToLoadFileException;
import app.coronawarn.server.services.distribution.assembly.appconfig.validation.WeightValidationError.ErrorType;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ExposureConfigurationValidatorTest {

  private static final ValidationResult SUCCESS = new ValidationResult();

  @ParameterizedTest
  @MethodSource("createOkTests")
  void ok(TestWithExpectedResult test) throws UnableToLoadFileException {
    assertThat(getResultForTest(test)).isEqualTo(SUCCESS);
  }

  @ParameterizedTest
  @MethodSource("createFailedTests")
  void fails(TestWithExpectedResult test) throws UnableToLoadFileException {
    assertThat(getResultForTest(test)).isEqualTo(test.result);
  }

  @Test
  void emptyFileThrowsLoadFailure() {
    assertThat(
        catchThrowable(() -> ExposureConfigurationProvider.readFile("parameters/empty.yaml")))
        .isInstanceOf(UnableToLoadFileException.class);
  }

  private ValidationResult getResultForTest(TestWithExpectedResult test)
      throws UnableToLoadFileException {
    var config = ExposureConfigurationProvider.readFile(test.path());
    var validator = new ExposureConfigurationValidator(config);
    return validator.validate();
  }

  private static Stream<Arguments> createOkTests() {
    return Stream.of(
        AllOk(),
        PartlyFilled(),
        WeightOk()
    ).map(Arguments::of);
  }

  private static Stream<Arguments> createFailedTests() {
    return Stream.of(
        ScoreTooHigh(),
        // TODO cwa-server/#320 Validate that no attributes are missing in .yaml
        // ScoreNegative(),
        WeightNegative(),
        WeightTooHigh()
    ).map(Arguments::of);
  }

  public static TestWithExpectedResult AllOk() {
    return new TestWithExpectedResult("all_ok.yaml");
  }

  public static TestWithExpectedResult PartlyFilled() {
    return new TestWithExpectedResult("partly_filled.yaml");
  }

  public static TestWithExpectedResult WeightOk() {
    return new TestWithExpectedResult("weight_ok.yaml");
  }

  public static TestWithExpectedResult WeightNegative() {
    return new TestWithExpectedResult("weight_negative.yaml")
        .with(new WeightValidationError("transmission", -10d, ErrorType.OUT_OF_RANGE))
        .with(new WeightValidationError("attenuation", 0.0001, ErrorType.TOO_MANY_DECIMAL_PLACES))
        .with(new WeightValidationError("attenuation", 0.0001, ErrorType.OUT_OF_RANGE))
        .with(new WeightValidationError("duration", 0, ErrorType.OUT_OF_RANGE));
  }

  public static TestWithExpectedResult WeightTooHigh() {
    return new TestWithExpectedResult("weight_too_high.yaml")
        .with(new WeightValidationError("duration", 99999999d, ErrorType.OUT_OF_RANGE))
        .with(new WeightValidationError("attenuation", 100.001d, ErrorType.OUT_OF_RANGE))
        .with(new WeightValidationError("transmission", 101d, ErrorType.OUT_OF_RANGE));
  }

  public static TestWithExpectedResult ScoreNegative() {
    return new TestWithExpectedResult("score_negative.yaml")
         .with(new RiskLevelValidationError("transmission", "appDefined1"))
         .with(new RiskLevelValidationError("transmission", "appDefined3"));
  }

  public static TestWithExpectedResult ScoreTooHigh() {
    return new TestWithExpectedResult("score_too_high.yaml")
        .with(new RiskLevelValidationError("transmission", "appDefined1"))
        .with(new RiskLevelValidationError("transmission", "appDefined2"));
  }
}
