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

import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.RiskScoreClassificationValidator.CONFIG_PREFIX;
import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.ValidationError.ErrorType.BLANK_LABEL;
import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.ValidationError.ErrorType.INVALID_PARTITIONING;
import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.ValidationError.ErrorType.INVALID_URL;
import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.ValidationError.ErrorType.MIN_GREATER_THAN_MAX;
import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.ValidationError.ErrorType.VALUE_OUT_OF_BOUNDS;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.common.protocols.internal.RiskScoreClass;
import app.coronawarn.server.common.protocols.internal.RiskScoreClassification;
import app.coronawarn.server.services.distribution.assembly.appconfig.validation.ValidationError.ErrorType;
import java.util.Arrays;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class RiskScoreClassificationValidatorTest {

  private final static int MAX_SCORE = ParameterSpec.RISK_SCORE_MAX;
  private final static String VALID_LABEL = "myLabel";
  private final static String VALID_URL = "https://www.my.url";

  public final static RiskScoreClassification MINIMAL_RISK_SCORE_CLASSIFICATION =
      buildClassification(buildRiskClass(VALID_LABEL, 0, MAX_SCORE, VALID_URL));

  @ParameterizedTest
  @ValueSource(strings = {"", " "})
  void failsForBlankLabels(String invalidLabel) {
    var validator = buildValidator(buildRiskClass(invalidLabel, 0, MAX_SCORE, VALID_URL));
    var expectedResult =
        buildExpectedResult(buildError(CONFIG_PREFIX + "risk-classes.label", invalidLabel, BLANK_LABEL));

    assertThat(validator.validate()).isEqualTo(expectedResult);
  }

  @ParameterizedTest
  @ValueSource(strings = {"invalid.Url", "invalid-url", "$$$://invalid.url", "", " "})
  void failsForInvalidUrl(String invalidUrl) {
    var validator = buildValidator(buildRiskClass(VALID_LABEL, 0, MAX_SCORE, invalidUrl));
    var expectedResult =
        buildExpectedResult(buildError(CONFIG_PREFIX + "risk-classes.url", invalidUrl, INVALID_URL));

    assertThat(validator.validate()).isEqualTo(expectedResult);
  }

  @Test
  void failsForNegativeRiskValues() {
    // must cover value range of equal size in order to avoid INVALID_PARTITIONING error
    int negativeMin = -MAX_SCORE - 1;
    int negativeMax = -1;
    var validator = buildValidator(buildRiskClass(VALID_LABEL, negativeMin, negativeMax, VALID_URL));
    var expectedResult = buildExpectedResult(
        buildError(CONFIG_PREFIX + "risk-classes.[minRiskLevel|maxRiskLevel]", negativeMin, VALUE_OUT_OF_BOUNDS),
        buildError(CONFIG_PREFIX + "risk-classes.[minRiskLevel|maxRiskLevel]", negativeMax, VALUE_OUT_OF_BOUNDS));

    assertThat(validator.validate()).isEqualTo(expectedResult);
  }

  @Test
  void failsForTooLargeRiskValues() {
    // must cover value range of equal size in order to avoid INVALID_PARTITIONING error
    int tooLargeMin = MAX_SCORE + 1;
    int tooLargeMax = 2 * MAX_SCORE + 1;
    var validator = buildValidator(buildRiskClass(VALID_LABEL, tooLargeMin, tooLargeMax, VALID_URL));
    var expectedResult = buildExpectedResult(
        buildError(CONFIG_PREFIX + "risk-classes.[minRiskLevel|maxRiskLevel]", tooLargeMin, VALUE_OUT_OF_BOUNDS),
        buildError(CONFIG_PREFIX + "risk-classes.[minRiskLevel|maxRiskLevel]", tooLargeMax, VALUE_OUT_OF_BOUNDS));

    assertThat(validator.validate()).isEqualTo(expectedResult);
  }

  @Test
  void failsIfMinGreaterThanMax() {
    int min = 1;
    int max = 0;
    // Note: additional classes have to be added in order to reach the expected value range size
    var validator = buildValidator(buildRiskClass(VALID_LABEL, min, max, VALID_URL),
        buildRiskClass(VALID_LABEL, 0, MAX_SCORE, VALID_URL));
    var expectedResult =
        buildError(CONFIG_PREFIX + "risk-classes.[minRiskLevel+maxRiskLevel]",
            (min + ", " + max), MIN_GREATER_THAN_MAX);

    assertThat(validator.validate().hasError(expectedResult)).isTrue();
  }

  @ParameterizedTest
  @MethodSource("createInvalidPartitionings")
  void failsIfPartitioningInvalid(RiskScoreClassification invalidClassification) {
    var validator = new RiskScoreClassificationValidator(invalidClassification);
    int coveredRange = invalidClassification.getRiskClassesList().stream()
        .mapToInt(riskScoreClass -> (riskScoreClass.getMax() - riskScoreClass.getMin()))
        .sum();
    var expectedResult = buildExpectedResult(
        buildError(CONFIG_PREFIX + "risk-classes", coveredRange, INVALID_PARTITIONING));

    assertThat(validator.validate()).isEqualTo(expectedResult);
  }

  private static Stream<Arguments> createInvalidPartitionings() {
    return Stream.of(
        buildClassification(buildRiskClass(VALID_LABEL, 0, 0, VALID_URL)),
        buildClassification(
            buildRiskClass(VALID_LABEL, 0, MAX_SCORE / 2, VALID_URL),
            buildRiskClass(VALID_LABEL, MAX_SCORE / 2, MAX_SCORE, VALID_URL),
            buildRiskClass(VALID_LABEL, 0, MAX_SCORE, VALID_URL)),
        buildClassification(
            buildRiskClass(VALID_LABEL, 0, MAX_SCORE, VALID_URL),
            buildRiskClass(VALID_LABEL, 0, MAX_SCORE, VALID_URL))
    ).map(Arguments::of);
  }

  @ParameterizedTest
  @MethodSource("createValidClassifications")
  void doesNotFailForValidClassification(RiskScoreClassification validClassification) {
    var validator = new RiskScoreClassificationValidator(validClassification);
    assertThat(validator.validate()).isEqualTo(new ValidationResult());
  }

  private static Stream<Arguments> createValidClassifications() {
    return Stream.of(
        MINIMAL_RISK_SCORE_CLASSIFICATION,
        // [0:MAX_SCORE/2][MAX_SCORE/2:MAX_SCORE]
        buildClassification(
            buildRiskClass(VALID_LABEL, 0, MAX_SCORE / 2, VALID_URL),
            buildRiskClass(VALID_LABEL, MAX_SCORE / 2, MAX_SCORE, VALID_URL)),
        // [0:MAX_SCORE-10][MAX_SCORE-9][MAX_SCORE-8:MAX_SCORE]
        buildClassification(
            buildRiskClass(VALID_LABEL, 0, MAX_SCORE - 10, VALID_URL),
            buildRiskClass(VALID_LABEL, MAX_SCORE - 10, MAX_SCORE - 10, VALID_URL),
            buildRiskClass(VALID_LABEL, MAX_SCORE - 10, MAX_SCORE, VALID_URL))
    ).map(Arguments::of);
  }

  public static ValidationError buildError(String parameter, Object value, ErrorType reason) {
    return new ValidationError(parameter, value, reason);
  }

  private static RiskScoreClassificationValidator buildValidator(RiskScoreClass... riskScoreClasses) {
    return new RiskScoreClassificationValidator(buildClassification(riskScoreClasses));
  }

  private static RiskScoreClassification buildClassification(RiskScoreClass... riskScoreClasses) {
    return RiskScoreClassification.newBuilder().addAllRiskClasses(asList(riskScoreClasses)).build();
  }

  private static RiskScoreClass buildRiskClass(String label, int min, int max, String url) {
    return RiskScoreClass.newBuilder().setLabel(label).setMin(min).setMax(max).setUrl(url).build();
  }

  public static ValidationResult buildExpectedResult(ValidationError... errors) {
    var validationResult = new ValidationResult();
    Arrays.stream(errors).forEach(validationResult::add);
    return validationResult;
  }
}
