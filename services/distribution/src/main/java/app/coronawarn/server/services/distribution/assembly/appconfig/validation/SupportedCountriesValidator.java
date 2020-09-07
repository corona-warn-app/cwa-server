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

import static app.coronawarn.server.services.distribution.assembly.appconfig.validation.ValidationError.ErrorType.INVALID_VALUES;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;



/**
 * Validates the supported countries according to ISO 3166.
 */
public class SupportedCountriesValidator extends ConfigurationValidator {

  public static final List<String> ISO_COUNTRIES = Arrays.asList(Locale.getISOCountries());
  private final List<String> supportedCountries;

  public SupportedCountriesValidator(List<String> supportedCountries) {
    this.supportedCountries = supportedCountries;
  }

  @Override
  public ValidationResult validate() {
    errors = new ValidationResult();
    validateSupportedCountries();
    return errors;
  }

  private void validateSupportedCountries() {
    supportedCountries.forEach(country -> {
          if (!ISO_COUNTRIES.contains(country)) {
            errors.add(new ValidationError("supported-countries", country, INVALID_VALUES));
          }
        }
    );
  }
}
