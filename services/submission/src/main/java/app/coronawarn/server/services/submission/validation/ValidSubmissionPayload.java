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

import static app.coronawarn.server.common.persistence.domain.DiagnosisKey.EXPECTED_ROLLING_PERIOD;

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
import java.util.stream.Collectors;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;

import org.apache.tomcat.util.buf.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



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

    private static final Logger logger = LoggerFactory.getLogger(SubmissionPayloadValidator.class);

    private SubmissionServiceConfig config;

    public SubmissionPayloadValidator(SubmissionServiceConfig submissionServiceConfig) {
       this.config = submissionServiceConfig;
    }

    /**
     * Validates the following constraints.
     * <ul>
     *   <li>StartIntervalNumber values from the same {@link SubmissionPayload} shall be unique.</li>
     *   <li>There must not be any keys in the {@link SubmissionPayload} have overlapping time windows.</li>
     *   <li>Number of keys submitted must not exceed the configured maximum (see <code>application.yml
     *       services.submission.payload.max-number-of-keys</code></li>
     *   <li>Visited countries field must be a value from within the accepted country codes.</li>
     * </ul>
     */
    @Override
    public boolean isValid(SubmissionPayload submissionPayload, ConstraintValidatorContext validatorContext) {
      List<TemporaryExposureKey> exposureKeys = submissionPayload.getKeysList();
      validatorContext.disableDefaultConstraintViolation();

      boolean isValid = checkKeyCollectionSize(exposureKeys, validatorContext);
      isValid &= checkUniqueStartIntervalNumbers(exposureKeys, validatorContext);
      isValid &= checkNoOverlapsInTimeWindow(exposureKeys, validatorContext);
      isValid &= checkOriginCountryIsAccepted(submissionPayload, validatorContext);
      logIfVisitedCountriesNotAllowed(submissionPayload, validatorContext);

      return isValid;
    }

    /**
     * @return false if the originCountry field of the given payload does not contain
     * a country code from the configured <code>application.yml/allowed-countries</code>
     */
    private boolean checkOriginCountryIsAccepted(SubmissionPayload submissionPayload,
        ConstraintValidatorContext validatorContext) {

      // TODO: Uncomment below when the proto definition is changed
      // String originCountry = submissionPayload.getOriginCountry();
      String originCountry = "DE";
      if (!config.isCountryAllowed(originCountry)) {
        addViolation(validatorContext, String.format(
            "Origin country %s is not part of the allowed countries list", originCountry));
        return false;
      }
      return true;
    }

    /**
     * Log a warning if the payload contains a visited country which is not
     * part of the <code>allowed-countries</code> list
     */
    private void logIfVisitedCountriesNotAllowed(SubmissionPayload submissionPayload,
        ConstraintValidatorContext validatorContext) {
      // TODO: Uncomment below when the proto definition is changed
      // List<String> visitedCountries = submissionPayload.getVisitedCountries();
      List<String> visitedCountries = List.of("DE", "FR");
      if (!config.areCountriesAllowed(visitedCountries)) {
           logger.warn("Submission Payload contains some"
               + " visited countries which are not allowed: {}", StringUtils.join(visitedCountries, ','));
      }
    }

    private void addViolation(ConstraintValidatorContext validatorContext, String message) {
      validatorContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }

    private boolean checkKeyCollectionSize(List<TemporaryExposureKey> exposureKeys,
        ConstraintValidatorContext validatorContext) {
      if (exposureKeys.isEmpty() || exceedsMaxNumberOfKeysPerSubmission(exposureKeys)) {
        addViolation(validatorContext, String.format(
            "Number of keys must be between 1 and %s, but is %s.", config.getMaxNumberOfKeys(), exposureKeys.size()));
        return false;
      }
      return true;
    }

    private boolean exceedsMaxNumberOfKeysPerSubmission(List<TemporaryExposureKey> exposureKeys) {
      return exposureKeys.size() > config.getMaxNumberOfKeys();
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
            "Duplicate StartIntervalNumber found. StartIntervalNumbers: %s",
            Arrays.stream(startIntervalNumbers).map(String::valueOf).collect(Collectors.joining(","))));
        return false;
      }
      return true;
    }

    private boolean checkNoOverlapsInTimeWindow(List<TemporaryExposureKey> exposureKeys,
        ConstraintValidatorContext validatorContext) {
      if (exposureKeys.size() < 2) {
        return true;
      }

      Integer[] sortedStartIntervalNumbers = exposureKeys.stream()
          .mapToInt(TemporaryExposureKey::getRollingStartIntervalNumber)
          .sorted().boxed().toArray(Integer[]::new);

      for (int i = 1; i < sortedStartIntervalNumbers.length; i++) {
        if ((sortedStartIntervalNumbers[i - 1] + EXPECTED_ROLLING_PERIOD) > sortedStartIntervalNumbers[i]) {
          addViolation(validatorContext, String.format(
              "Subsequent intervals overlap. StartIntervalNumbers: %s",
              Arrays.stream(sortedStartIntervalNumbers).map(String::valueOf).collect(Collectors.joining(","))));
          return false;
        }
      }
      return true;
    }
  }
}
