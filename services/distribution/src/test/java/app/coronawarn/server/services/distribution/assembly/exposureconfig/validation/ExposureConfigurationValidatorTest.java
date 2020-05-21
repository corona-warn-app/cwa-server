/*
 * Corona-Warn-App
 *
 * SAP SE and all other contributors /
 * copyright owners license this file to you under the Apache
 * License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package app.coronawarn.server.services.distribution.assembly.exposureconfig.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import app.coronawarn.server.services.distribution.assembly.exposureconfig.ExposureConfigurationProvider;
import app.coronawarn.server.services.distribution.assembly.exposureconfig.UnableToLoadFileException;
import app.coronawarn.server.services.distribution.assembly.exposureconfig.validation.WeightValidationError.ErrorType;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ExposureConfigurationValidatorTest {

  private static final ValidationResult SUCCESS = new ValidationResult();

  @ParameterizedTest
  @MethodSource("createOkTests")
  public void ok(TestWithExpectedResult test) throws UnableToLoadFileException {
    assertEquals(SUCCESS, getResultForTest(test));
  }

  @ParameterizedTest
  @MethodSource("createFailedTests")
  public void fails(TestWithExpectedResult test) throws UnableToLoadFileException {
    assertEquals(test.result, getResultForTest(test));
  }

  @Test
  public void emptyFileThrowsLoadFailure() {
    assertThrows(UnableToLoadFileException.class, () ->
        ExposureConfigurationProvider.readFile("parameters/empty.yaml"));
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
        ScoreNegative(),
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
        .with(new RiskLevelValidationError("transmission", "appDefined2"))
        .with(new RiskLevelValidationError("transmission", "appDefined3"));
  }

  public static TestWithExpectedResult ScoreTooHigh() {
    return new TestWithExpectedResult("score_too_high.yaml")
        .with(new RiskLevelValidationError("transmission", "appDefined1"))
        .with(new RiskLevelValidationError("transmission", "appDefined2"));
  }
}
