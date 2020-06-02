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

import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.GeneralValidationError.ErrorType.MIN_GREATER_THAN_MAX;

import app.coronawarn.server.common.protocols.internal.ApplicationVersionConfiguration;
import app.coronawarn.server.common.protocols.internal.ApplicationVersionInfo;
import app.coronawarn.server.common.protocols.internal.SemanticVersion;

public class ApplicationVersionConfigurationValidator extends ConfigurationValidator {

  private final ApplicationVersionConfiguration config;

  public ApplicationVersionConfigurationValidator(ApplicationVersionConfiguration config) {
    this.config = config;
  }

  @Override
  public ValidationResult validate() {
    this.errors = new ValidationResult();
    validateApplicationVersionInfo("ios", config.getIos());
    validateApplicationVersionInfo("android", config.getAndroid());
    return this.errors;
  }

  private void validateApplicationVersionInfo(String name, ApplicationVersionInfo appVersionInfo) {
    ComparisonResult comparisonResult = compare(appVersionInfo.getLatest(), appVersionInfo.getMin());
    if (ComparisonResult.LESS.equals(comparisonResult)) {
      this.errors.add(new GeneralValidationError(name + ": latest/min", appVersionInfo, MIN_GREATER_THAN_MAX));
    }
  }

  private ComparisonResult compare(SemanticVersion left, SemanticVersion right) {
    if (left.getMajor() < right.getMajor()) {
      return ComparisonResult.LESS;
    }
    if (left.getMajor() > right.getMajor()) {
      return ComparisonResult.GREATER;
    }
    if (left.getMinor() < right.getMinor()) {
      return ComparisonResult.LESS;
    }
    if (left.getMinor() > right.getMinor()) {
      return ComparisonResult.GREATER;
    }
    if (left.getPatch() < right.getPatch()) {
      return ComparisonResult.LESS;
    }
    if (left.getPatch() > right.getPatch()) {
      return ComparisonResult.GREATER;
    }
    return ComparisonResult.EQUAL;
  }

  private enum ComparisonResult {
    LESS,
    EQUAL,
    GREATER
  }
}
