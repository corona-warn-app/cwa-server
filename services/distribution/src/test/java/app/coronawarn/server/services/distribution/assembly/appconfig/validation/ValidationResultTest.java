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

import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.ValidationError.ErrorType.INVALID_URL;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ValidationResultTest {

  private static final ValidationError EXP_VALIDATION_ERROR = new ValidationError("expSrc", "expValue", INVALID_URL);
  private ValidationResult emptyValidationResult;

  @BeforeEach
  void setUpEmptyResult() {
    emptyValidationResult = new ValidationResult();
  }

  @Test
  void hasErrorsReturnsFalseIfNoErrors() {
    assertThat(emptyValidationResult.hasErrors()).isFalse();
  }

  @Test
  void hasErrorsReturnsTrueIfErrors() {
    emptyValidationResult.add(EXP_VALIDATION_ERROR);
    assertThat(emptyValidationResult.hasErrors()).isTrue();
  }

  @Test
  void toStringReturnsCorrectErrorInformation() {
    emptyValidationResult.add(EXP_VALIDATION_ERROR);
    assertThat(emptyValidationResult).hasToString("[" + EXP_VALIDATION_ERROR.toString() + "]");
  }

  @Test
  void toStringReturnsCorrectErrorInformationIfEmpty() {
    assertThat(emptyValidationResult).hasToString("[]");
  }
}
