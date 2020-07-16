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

package app.coronawarn.server.services.submission.config;

import org.springframework.util.unit.DataSize;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class SubmissionServiceConfigValidator implements Validator {

  @Override
  public boolean supports(Class<?> type) {
    return type == SubmissionServiceConfig.class;
  }

  @Override
  public void validate(Object o, Errors errors) {
    SubmissionServiceConfig properties = (SubmissionServiceConfig) o;
    DataSize minMaximumRequestSize = DataSize.ofBytes(280);
    DataSize maxMaximumRequestSize = DataSize.ofKilobytes(200);
    if (properties.getMaximumRequestSize().compareTo(minMaximumRequestSize) < 0
        || properties.getMaximumRequestSize().compareTo(maxMaximumRequestSize) > 0) {
      errors.rejectValue("maximumRequestSize",
          "Must be at least " + minMaximumRequestSize + " and not more than " + maxMaximumRequestSize + ".");
    }
  }

}