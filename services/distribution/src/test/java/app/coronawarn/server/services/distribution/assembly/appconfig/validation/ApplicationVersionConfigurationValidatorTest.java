/*
 * ---license-start
 * Corona-Warn-App
 * ---
 * Copyright (C) 2020 SAP SE and all other contributors
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ---license-end
 */

package app.coronawarn.server.services.distribution.assembly.appconfig.validation;

import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.RiskScoreClassificationValidatorTest.buildError;
import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.RiskScoreClassificationValidatorTest.buildExpectedResult;
import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.common.protocols.internal.ApplicationVersionConfiguration;
import app.coronawarn.server.services.distribution.assembly.appconfig.UnableToLoadFileException;
import app.coronawarn.server.services.distribution.assembly.appconfig.YamlLoader;
import app.coronawarn.server.services.distribution.assembly.appconfig.validation.GeneralValidationError.ErrorType;
import org.junit.jupiter.api.Test;

class ApplicationVersionConfigurationValidatorTest {

  private static final ValidationResult SUCCESS = new ValidationResult();

  @Test
  void succeedsIfLatestEqualsMin() throws UnableToLoadFileException {
    var validator = buildValidator("app-version/latest-equals-min.yaml");
    assertThat(validator.validate()).isEqualTo(SUCCESS);
  }

  @Test
  void succeedsIfLatestHigherThanMin() throws UnableToLoadFileException {
    var validator = buildValidator("app-version/latest-higher-than-min.yaml");
    assertThat(validator.validate()).isEqualTo(SUCCESS);
  }

  @Test
  void failsIfLatestLowerThanMin() throws UnableToLoadFileException {
    var validator = buildValidator("app-version/latest-lower-than-min.yaml");
    assertThat(validator.validate())
        .isEqualTo(buildExpectedResult(buildError("ios: latest/min", "1.2.2", ErrorType.MIN_GREATER_THAN_MAX)));
  }

  private ConfigurationValidator buildValidator(String filePath) throws UnableToLoadFileException {
    var configBuilder = YamlLoader.loadYamlIntoProtobufBuilder(filePath, ApplicationVersionConfiguration.Builder.class);
    return new ApplicationVersionConfigurationValidator(configBuilder.build());
  }
}
