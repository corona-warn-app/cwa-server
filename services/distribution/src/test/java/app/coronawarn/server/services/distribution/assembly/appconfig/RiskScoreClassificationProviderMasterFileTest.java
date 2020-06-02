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

package app.coronawarn.server.services.distribution.assembly.appconfig;

import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.services.distribution.assembly.appconfig.validation.RiskScoreClassificationValidator;
import app.coronawarn.server.services.distribution.assembly.appconfig.validation.ValidationResult;
import org.junit.jupiter.api.Test;

/**
 * This test will verify that the provided risk score classification master file is syntactically correct and according
 * to spec. There should never be any deployment when this test is failing.
 */
class RiskScoreClassificationProviderMasterFileTest {

  private static final ValidationResult SUCCESS = new ValidationResult();

  @Test
  void testMasterFile() throws UnableToLoadFileException {
    var config = RiskScoreClassificationProvider.readMasterFile();

    var validator = new RiskScoreClassificationValidator(config);

    assertThat(validator.validate()).isEqualTo(SUCCESS);
  }
}
