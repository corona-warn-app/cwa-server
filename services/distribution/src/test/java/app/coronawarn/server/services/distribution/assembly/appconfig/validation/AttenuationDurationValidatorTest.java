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
import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.ParameterSpec.ATTENUATION_DURATION_WEIGHT_MAX;
import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.ParameterSpec.ATTENUATION_DURATION_WEIGHT_MIN;
import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.ParameterSpec.DEFAULT_BUCKET_OFFSET_MAX;
import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.ParameterSpec.DEFAULT_BUCKET_OFFSET_MIN;
import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.ParameterSpec.RISK_SCORE_NORMALIZATION_DIVISOR_MAX;
import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.ParameterSpec.RISK_SCORE_NORMALIZATION_DIVISOR_MIN;
import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.RiskScoreClassificationValidatorTest.buildError;
import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.RiskScoreClassificationValidatorTest.buildExpectedResult;
import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.WeightValidationError.ErrorType.OUT_OF_RANGE;
import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.WeightValidationError.ErrorType.TOO_MANY_DECIMAL_PLACES;
import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.common.protocols.internal.AttenuationDuration;
import app.coronawarn.server.common.protocols.internal.Thresholds;
import app.coronawarn.server.common.protocols.internal.Weights;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class AttenuationDurationValidatorTest {
  private static final Thresholds VALID_THRESHOLDS =
      buildThresholds(ATTENUATION_DURATION_THRESHOLD_MIN, ATTENUATION_DURATION_THRESHOLD_MAX);
  private static final Weights VALID_WEIGHTS =
      buildWeights(ATTENUATION_DURATION_WEIGHT_MAX, ATTENUATION_DURATION_WEIGHT_MAX, ATTENUATION_DURATION_WEIGHT_MAX);
  private static final int VALID_BUCKET_OFFSET = DEFAULT_BUCKET_OFFSET_MAX;
  private static final int VALID_NORMALIZATION_DIVISOR = RISK_SCORE_NORMALIZATION_DIVISOR_MIN;

  @ParameterizedTest
  @ValueSource(ints = {ATTENUATION_DURATION_THRESHOLD_MIN - 1, ATTENUATION_DURATION_THRESHOLD_MAX + 1})
  void failsIfAttenuationDurationThresholdOutOfBounds(int invalidThresholdValue) {
    AttenuationDurationValidator validator = buildValidator(
        buildThresholds(invalidThresholdValue, invalidThresholdValue));

    ValidationResult expectedResult = buildExpectedResult(
        buildError("attenuation-duration.thresholds.lower", invalidThresholdValue, VALUE_OUT_OF_BOUNDS),
        buildError("attenuation-duration.thresholds.upper", invalidThresholdValue, VALUE_OUT_OF_BOUNDS));

    assertThat(validator.validate()).isEqualTo(expectedResult);
  }

  @ParameterizedTest
  @ValueSource(ints = {DEFAULT_BUCKET_OFFSET_MIN - 1, DEFAULT_BUCKET_OFFSET_MAX + 1})
  void failsIfBucketOffsetOutOfBounds(int invalidDefaultBucketOffsetValue) {
    AttenuationDurationValidator validator = buildValidator(VALID_THRESHOLDS, VALID_WEIGHTS,
        invalidDefaultBucketOffsetValue, VALID_NORMALIZATION_DIVISOR);

    ValidationResult expectedResult = buildExpectedResult(
        buildError("attenuation-duration.default-bucket-offset", invalidDefaultBucketOffsetValue, VALUE_OUT_OF_BOUNDS));

    assertThat(validator.validate()).isEqualTo(expectedResult);
  }

  @ParameterizedTest
  @ValueSource(ints = {RISK_SCORE_NORMALIZATION_DIVISOR_MIN - 1, RISK_SCORE_NORMALIZATION_DIVISOR_MAX + 1})
  void failsIfRiskScoreNormalizationDivisorOutOfBounds(int invalidRiskScoreNormalizationDivisorValue) {
    AttenuationDurationValidator validator = buildValidator(VALID_THRESHOLDS, VALID_WEIGHTS, VALID_BUCKET_OFFSET,
        invalidRiskScoreNormalizationDivisorValue);

    ValidationResult expectedResult = buildExpectedResult(
        buildError("attenuation-duration.risk-score-normalization-divisor", invalidRiskScoreNormalizationDivisorValue,
            VALUE_OUT_OF_BOUNDS));

    assertThat(validator.validate()).isEqualTo(expectedResult);
  }

  @Test
  void failsIfUpperAttenuationDurationThresholdLesserThanLower() {
    AttenuationDurationValidator validator = buildValidator(
        buildThresholds(ATTENUATION_DURATION_THRESHOLD_MAX, ATTENUATION_DURATION_THRESHOLD_MIN));

    ValidationResult expectedResult = buildExpectedResult(
        new GeneralValidationError("attenuation-duration.thresholds.lower, attenuation-duration.thresholds.upper",
            (ATTENUATION_DURATION_THRESHOLD_MAX + ", " + ATTENUATION_DURATION_THRESHOLD_MIN), MIN_GREATER_THAN_MAX));

    assertThat(validator.validate()).isEqualTo(expectedResult);
  }

  @ParameterizedTest
  @ValueSource(doubles = {ATTENUATION_DURATION_WEIGHT_MIN - .1, ATTENUATION_DURATION_WEIGHT_MAX + .1})
  void failsIfWeightsOutOfBounds(double invalidWeightValue) {
    AttenuationDurationValidator validator = buildValidator(
        buildWeights(invalidWeightValue, invalidWeightValue, invalidWeightValue));

    ValidationResult expectedResult = buildExpectedResult(
        new WeightValidationError("attenuation-duration.weights.low", invalidWeightValue, OUT_OF_RANGE),
        new WeightValidationError("attenuation-duration.weights.mid", invalidWeightValue, OUT_OF_RANGE),
        new WeightValidationError("attenuation-duration.weights.high", invalidWeightValue, OUT_OF_RANGE));

    assertThat(validator.validate()).isEqualTo(expectedResult);
  }

  @Test
  void failsIfWeightsHaveTooManyDecimalPlaces() {
    double invalidWeightValue = ATTENUATION_DURATION_WEIGHT_MAX - 0.0000001;
    AttenuationDurationValidator validator = buildValidator(
        buildWeights(invalidWeightValue, invalidWeightValue, invalidWeightValue));

    ValidationResult expectedResult = buildExpectedResult(
        new WeightValidationError("attenuation-duration.weights.low", invalidWeightValue, TOO_MANY_DECIMAL_PLACES),
        new WeightValidationError("attenuation-duration.weights.mid", invalidWeightValue, TOO_MANY_DECIMAL_PLACES),
        new WeightValidationError("attenuation-duration.weights.high", invalidWeightValue, TOO_MANY_DECIMAL_PLACES));

    assertThat(validator.validate()).isEqualTo(expectedResult);
  }

  private static AttenuationDurationValidator buildValidator(Thresholds thresholds) {
    return buildValidator(thresholds, VALID_WEIGHTS, VALID_BUCKET_OFFSET, VALID_NORMALIZATION_DIVISOR);
  }

  private static AttenuationDurationValidator buildValidator(Weights weights) {
    return buildValidator(VALID_THRESHOLDS, weights, VALID_BUCKET_OFFSET, VALID_NORMALIZATION_DIVISOR);
  }

  private static AttenuationDurationValidator buildValidator(
      Thresholds thresholds, Weights weights, int defaultBucketOffset, int riskScoreNormalizationDivisor) {
    return new AttenuationDurationValidator(AttenuationDuration.newBuilder()
        .setThresholds(thresholds)
        .setWeights(weights)
        .setDefaultBucketOffset(defaultBucketOffset)
        .setRiskScoreNormalizationDivisor(riskScoreNormalizationDivisor)
        .build());
  }

  private static Thresholds buildThresholds(int lower, int upper) {
    return Thresholds.newBuilder().setLower(lower).setUpper(upper).build();
  }

  private static Weights buildWeights(double low, double mid, double high) {
    return Weights.newBuilder().setLow(low).setMid(mid).setHigh(high).build();
  }
}
