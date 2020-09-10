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

package app.coronawarn.server.services.distribution.config;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validate the values of the DistributionServiceConfig.
 */
public class DistributionServiceConfigValidator implements Validator {

  private static final List<String> ISO_COUNTRIES = Arrays.asList(Locale.getISOCountries());

  @Override
  public boolean supports(Class<?> type) {
    return DistributionServiceConfig.class.isAssignableFrom(type);
  }

  /**
   * Validate the {@link DistributionServiceConfig} if the supported country parameter is conform to the ISO 3116.
   */
  @Override
  public void validate(Object o, Errors errors) {
    DistributionServiceConfig properties = (DistributionServiceConfig) o;
    Arrays.stream(properties.getSupportedCountries()).forEach(country -> {
      if (!ISO_COUNTRIES.contains(country)) {
        errors.rejectValue("supportedCountries",
            "[" + country + "]: Country code isn't compliant to ISO 3166.");
      }
    });
  }
}
