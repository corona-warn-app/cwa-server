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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;


/**
 * Validates the supported countries according to ISO 3166.
 */
public class SupportedCountriesValidator extends ConfigurationValidator
    implements ConstraintValidator<ValidSupportedCountries, String> {

  public SupportedCountriesValidator() {

  }

  @Override
  public ValidationResult validate() {
    errors = new ValidationResult();

    return errors;
  }

  /**
   * Validates the supported countries in the protobuf according to ISO 3166.
   */
  public static boolean validateSupportedCountries(List<String> supportedCountries) {
    Set<String> isoCountries = new HashSet<>(Arrays.asList(Locale.getISOCountries()));
    boolean areIsoCountries = isoCountries.containsAll(supportedCountries);

    return areIsoCountries;
  }

  /**
   * Validates the supported countries coming from the application.yaml according to ISO 3166.
   */
  @Override
  public boolean isValid(String supportedCountries, ConstraintValidatorContext constraintValidatorContext) {
    Set<String> isoCountries = new HashSet<>(Arrays.asList(Locale.getISOCountries()));
    boolean areIsoCountries = isoCountries.containsAll(List.of(supportedCountries.split(",")));

    return areIsoCountries;
  }
}
