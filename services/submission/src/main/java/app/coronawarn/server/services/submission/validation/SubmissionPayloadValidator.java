/*
 * Corona-Warn-App
 *
 * SAP SE and all other contributors /
 * copyright owners license this file to you under the Apache
 * License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package app.coronawarn.server.services.submission.validation;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.TemporaryExposureKey;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload;
import app.coronawarn.server.services.submission.config.SubmissionServiceConfig;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.persistence.criteria.CriteriaBuilder.In;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validates {@link SubmissionPayload} instances according to https://developer.apple.com/documentation/exposurenotification/setting_up_an_exposure_notification_server
 */
@Component
public class SubmissionPayloadValidator implements Validator {

  private final int maxNumberOfKeys;

  public SubmissionPayloadValidator(SubmissionServiceConfig submissionServiceConfig) {
    maxNumberOfKeys = submissionServiceConfig.getMaxNumberOfKeys();
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return SubmissionPayload.class.equals(clazz);
  }

  /**
   * Validates the following constraints.
   * <ul>
   *   <li>StartIntervalNumber values from the same {@link SubmissionPayload} shall be unique.</li>
   *   <li>There must be no gaps for StartIntervalNumber values for a user.</li>
   *   <li>There must not be any keys in the {@link SubmissionPayload} have overlapping time windows.</li>
   *   <li>The period of time covered by the data file must not exceed the configured maximum number of days.</li>
   * </ul>
   *
   * @param target The {@link SubmissionPayload} that shall be validated.
   * @param errors The {@link Errors} object that shall be used for the creation of errors.
   */
  @Override
  public void validate(Object target, Errors errors) {
    List<TemporaryExposureKey> temporaryExposureKeys = ((SubmissionPayload) target).getKeysList();

    if (Objects.isNull(temporaryExposureKeys)) {
      errors.rejectValue(null, "Field 'keys' points to Null.");
      return;
    }

    checkKeyCollectionSize(temporaryExposureKeys, errors);
    checkUniqueStartIntervalNumbers(temporaryExposureKeys, errors);
    checkNoGapsInTimeWindow(temporaryExposureKeys, errors);
  }

  private void checkKeyCollectionSize(List<TemporaryExposureKey> temporaryExposureKeys, Errors errors) {
    if (temporaryExposureKeys.isEmpty() || temporaryExposureKeys.size() > maxNumberOfKeys) {
      String error = String.format("Number of keys must be between 1 and %s, but is %s.",
          maxNumberOfKeys, temporaryExposureKeys.size());
      errors.rejectValue(null, error);
    }
  }

  private void checkUniqueStartIntervalNumbers(List<TemporaryExposureKey> temporaryExposureKeys, Errors errors) {
    Integer[] startIntervalNumbers = temporaryExposureKeys.stream()
        .mapToInt(TemporaryExposureKey::getRollingStartIntervalNumber).boxed().toArray(Integer[]::new);
    long distinctSize = Arrays.stream(startIntervalNumbers)
        .distinct()
        .count();

    if (distinctSize < temporaryExposureKeys.size()) {
      String error = String.format(
          "Duplicate StartIntervalNumber found. StartIntervalNumbers: %s", startIntervalNumbers);
      errors.rejectValue(null, error);
    }
  }

  private void checkNoGapsInTimeWindow(List<TemporaryExposureKey> temporaryExposureKeys, Errors errors) {
    if (temporaryExposureKeys.size() < 2) {
      return;
    }

    Integer[] sortedStartInveralNumbers = temporaryExposureKeys.stream()
        .mapToInt(TemporaryExposureKey::getRollingStartIntervalNumber)
        .sorted().boxed().toArray(Integer[]::new);

    for (int i = 1; i < sortedStartInveralNumbers.length; i++) {
      if (sortedStartInveralNumbers[i] != sortedStartInveralNumbers[i - 1] + DiagnosisKey.EXPECTED_ROLLING_PERIOD) {
        String error = String.format(
            "Subsequent intervals do not align. StartIntervalNumbers: %s", sortedStartInveralNumbers);
        errors.rejectValue(null, error);
        return;
      }
    }
  }
}
