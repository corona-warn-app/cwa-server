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

package app.coronawarn.server.services.submission.validation;

import app.coronawarn.server.common.protocols.external.exposurenotification.TemporaryExposureKey;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload;
import app.coronawarn.server.services.submission.config.SubmissionServiceConfig;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;

@Constraint(validatedBy = ValidSubmissionPayload.SubmissionPayloadValidator.class)
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ValidSubmissionPayload {

  /**
   * Error message.
   *
   * @return the error message
   */
  String message() default "Invalid diagnosis key submission payload.";

  /**
   * Groups.
   *
   * @return
   */
  Class<?>[] groups() default {};

  /**
   * Payload.
   *
   * @return
   */
  Class<? extends Payload>[] payload() default {};

  class SubmissionPayloadValidator implements
      ConstraintValidator<ValidSubmissionPayload, SubmissionPayload> {

    private final int maxNumberOfKeys;
    private final int maxRollingPeriod;
    private final String[] supportedCountries;
    private final SubmissionServiceConfig submissionServiceConfig;

    public SubmissionPayloadValidator(SubmissionServiceConfig submissionServiceConfig) {
      this.submissionServiceConfig = submissionServiceConfig;
      maxNumberOfKeys = submissionServiceConfig.getMaxNumberOfKeys();
      maxRollingPeriod = submissionServiceConfig.getMaxRollingPeriod();
      supportedCountries = submissionServiceConfig.getSupportedCountries();
    }

    /**
     * Validates the following constraints.
     * <ul>
     *   <li>StartIntervalNumber values from the same {@link SubmissionPayload} shall be unique.</li>
     *   <li>There must be no gaps for StartIntervalNumber values for a user.</li>
     *   <li>There must not be any keys in the {@link SubmissionPayload} have overlapping time windows.</li>
     *   <li>The period of time covered by the data file must not exceed the configured maximum number of days.</li>
     *   <li>The origin country must be part of the supported countries.</li>
     *   <li>The visited countries must be part of the supported countries.</li>
     * </ul>
     */
    @Override
    public boolean isValid(SubmissionPayload submissionPayload, ConstraintValidatorContext validatorContext) {
      List<TemporaryExposureKey> exposureKeys = submissionPayload.getKeysList();
      validatorContext.disableDefaultConstraintViolation();

      if (keysHaveFlexibleRollingPeriod(exposureKeys)) {
        return checkStartIntervalNumberIsAtMidNight(exposureKeys, validatorContext)
            && checkKeysCumulateEqualOrLessThanMaxRollingPeriodPerDay(exposureKeys, validatorContext)
            && checkOriginCountryIsValid(submissionPayload, validatorContext)
            && checkVisitedCountriesAreValid(submissionPayload, validatorContext);
      } else {
        return checkStartIntervalNumberIsAtMidNight(exposureKeys, validatorContext)
            && checkKeyCollectionSize(exposureKeys, validatorContext)
            && checkUniqueStartIntervalNumbers(exposureKeys, validatorContext)
            && checkVisitedCountriesAreValid(submissionPayload, validatorContext);
      }
    }

    private void addViolation(ConstraintValidatorContext validatorContext, String message) {
      validatorContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }

    private boolean checkKeyCollectionSize(List<TemporaryExposureKey> exposureKeys,
        ConstraintValidatorContext validatorContext) {
      if (exposureKeys.isEmpty() || exposureKeys.size() > maxNumberOfKeys) {
        addViolation(validatorContext, String.format(
            "Number of keys must be between 1 and %s, but is %s.", maxNumberOfKeys, exposureKeys.size()));
        return false;
      }
      return true;
    }

    private boolean checkUniqueStartIntervalNumbers(List<TemporaryExposureKey> exposureKeys,
        ConstraintValidatorContext validatorContext) {
      Integer[] startIntervalNumbers = exposureKeys.stream()
          .mapToInt(TemporaryExposureKey::getRollingStartIntervalNumber).boxed().toArray(Integer[]::new);
      long distinctSize = Arrays.stream(startIntervalNumbers)
          .distinct()
          .count();

      if (distinctSize < exposureKeys.size()) {
        addViolation(validatorContext, String.format(
            "Duplicate StartIntervalNumber found. StartIntervalNumbers: %s", startIntervalNumbers));
        return false;
      }
      return true;
    }

    private boolean checkKeysCumulateEqualOrLessThanMaxRollingPeriodPerDay(List<TemporaryExposureKey> exposureKeys,
        ConstraintValidatorContext validatorContext) {

      boolean isValidRollingPeriod = exposureKeys.stream().collect(Collectors
          .groupingBy(TemporaryExposureKey::getRollingStartIntervalNumber,
              Collectors.summingInt(TemporaryExposureKey::getRollingPeriod)))
          .values().stream()
          .anyMatch(sum -> sum <= maxRollingPeriod);

      if (!isValidRollingPeriod) {
        addViolation(validatorContext, "The sum of the rolling periods exceeds 144 per day");
        return false;
      }
      return true;
    }

    private boolean keysHaveFlexibleRollingPeriod(List<TemporaryExposureKey> exposureKeys) {
      return exposureKeys.stream()
          .anyMatch(temporaryExposureKey -> temporaryExposureKey.getRollingPeriod() < maxRollingPeriod);
    }

    private boolean checkStartIntervalNumberIsAtMidNight(List<TemporaryExposureKey> exposureKeys,
        ConstraintValidatorContext validatorContext) {
      boolean isNotMidNight00Utc = exposureKeys.stream()
          .anyMatch(exposureKey -> exposureKey.getRollingStartIntervalNumber() % maxRollingPeriod > 0);

      if (isNotMidNight00Utc) {
        addViolation(validatorContext, "Start Interval Number must be at midnight ( 00:00 UTC )");
        return false;
      }

      return true;
    }

    /**
     * Verify if payload contains invalid or unaccepted origin country.
     *
     * @return false if the originCountry field of the given payload does not contain a country code from the configured
     * <code>application.yml/supported-countries</code>
     */
    private boolean checkOriginCountryIsValid(SubmissionPayload submissionPayload,
        ConstraintValidatorContext validatorContext) {
      String originCountry = submissionPayload.getOrigin();
      List<String> supportedCountriesList = List.of(supportedCountries);

      if (!supportedCountriesList.contains(originCountry)) {
        addViolation(validatorContext, String.format(
            "Origin country %s is not part of the supported countries list", originCountry));
        return false;
      }
      return true;
    }

    private boolean checkVisitedCountriesAreValid(SubmissionPayload submissionPayload,
        ConstraintValidatorContext validatorContext) {
      List<String> supportedCountriesList = List.of(supportedCountries);
      AtomicBoolean validCountries = new AtomicBoolean(true);

      submissionPayload.getVisitedCountriesList().forEach(country -> {
        if (!supportedCountriesList.contains(country)) {
          addViolation(validatorContext,
              "[" + country + "]: Visited country is not part of the supported countries list");

          validCountries.set(false);
        }
      });

      return validCountries.get();
    }
  }
}
