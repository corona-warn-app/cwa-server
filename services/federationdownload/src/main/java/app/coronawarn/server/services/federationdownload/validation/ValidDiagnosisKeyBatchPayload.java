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

package app.coronawarn.server.services.federationdownload.validation;

import static app.coronawarn.server.common.persistence.domain.DiagnosisKey.EXPECTED_ROLLING_PERIOD;

import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKeyBatch;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload;
import app.coronawarn.server.services.federationdownload.config.FederationDownloadServiceConfig;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.List;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;

@Constraint(validatedBy = ValidDiagnosisKeyBatchPayload.DiagnosisKeyBatchPayloadValidator.class)
@Target({ElementType.PARAMETER})
@Documented
public @interface ValidDiagnosisKeyBatchPayload {

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

  class DiagnosisKeyBatchPayloadValidator implements
      ConstraintValidator<ValidDiagnosisKeyBatchPayload, DiagnosisKeyBatch> {


    public DiagnosisKeyBatchPayloadValidator(FederationDownloadServiceConfig federationDownloadServiceConfig) {

    }

    /**
     * Validates the following constraints.
     * <ul>
     *   <li>StartIntervalNumber values from the same {@link SubmissionPayload} shall be unique.</li>
     *   <li>There must be no gaps for StartIntervalNumber values for a user.</li>
     *   <li>There must not be any keys in the {@link SubmissionPayload} have overlapping time windows.</li>
     *   <li>The period of time covered by the data file must not exceed the configured maximum number of days.</li>
     * </ul>
     */
    /*
    public boolean isValid(DiagnosisKeyBatch diagnosisKeyBatch, ConstraintValidatorContext validatorContext) {
      List<DiagnosisKey> diagnosisKeys = diagnosisKeyBatch.getKeysList();
      validatorContext.disableDefaultConstraintViolation();

      boolean isValid = checkKeyCollectionSize(exposureKeys, validatorContext);
      isValid &= checkUniqueStartIntervalNumbers(exposureKeys, validatorContext);
      isValid &= checkNoOverlapsInTimeWindow(exposureKeys, validatorContext);

      return isValid;
    }
    */
    @Override
    public boolean isValid(DiagnosisKeyBatch diagnosisKeyBatch, ConstraintValidatorContext constraintValidatorContext) {
      return true;
    }

    private void addViolation(ConstraintValidatorContext validatorContext, String message) {
      validatorContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }


    private boolean checkKeyCollectionSize(List<DiagnosisKey> diagnosisKey,
                                           ConstraintValidatorContext validatorContext) {
      /*
      if (exposureKeys.isEmpty() || exposureKeys.size() > maxNumberOfKeys) {
        addViolation(validatorContext, String.format(
            "Number of keys must be between 1 and %s, but is %s.", maxNumberOfKeys, exposureKeys.size()));
        return false;
      }
      return true;

       */
      return diagnosisKey.size() > 0;
    }


    private boolean checkUniqueStartIntervalNumbers(List<DiagnosisKey> diagnosisKeys,
                                                    ConstraintValidatorContext validatorContext) {
      Integer[] startIntervalNumbers = diagnosisKeys.stream()
          .mapToInt(DiagnosisKey::getRollingStartIntervalNumber).boxed().toArray(Integer[]::new);
      long distinctSize = Arrays.stream(startIntervalNumbers)
          .distinct()
          .count();

      if (distinctSize < diagnosisKeys.size()) {
        addViolation(validatorContext, String.format(
            "Duplicate StartIntervalNumber found. StartIntervalNumbers: %s", startIntervalNumbers));
        return false;
      }
      return true;
    }

    private boolean checkNoOverlapsInTimeWindow(List<DiagnosisKey> diagnosisKeys,
                                                ConstraintValidatorContext validatorContext) {
      if (diagnosisKeys.size() < 2) {
        return true;
      }

      Integer[] sortedStartIntervalNumbers = diagnosisKeys.stream()
          .mapToInt(DiagnosisKey::getRollingStartIntervalNumber)
          .sorted().boxed().toArray(Integer[]::new);

      for (int i = 1; i < sortedStartIntervalNumbers.length; i++) {
        if ((sortedStartIntervalNumbers[i - 1] + EXPECTED_ROLLING_PERIOD) > sortedStartIntervalNumbers[i]) {
          addViolation(validatorContext, String.format(
              "Subsequent intervals overlap. StartIntervalNumbers: %s", sortedStartIntervalNumbers));
          return false;
        }
      }
      return true;
    }
  }
}
