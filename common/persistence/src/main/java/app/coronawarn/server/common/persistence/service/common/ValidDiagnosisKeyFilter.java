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

package app.coronawarn.server.common.persistence.service.common;


import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ValidDiagnosisKeyFilter {

  private static final Logger logger = LoggerFactory.getLogger(ValidDiagnosisKeyFilter.class);

  /**
   * Rerturns a subset of diagnosis keys from the given list which have
   * passed the default entity validation.
   */
  public List<DiagnosisKey> filter(List<DiagnosisKey> diagnosisKeys) {
    List<DiagnosisKey> validDiagnosisKeys =
        diagnosisKeys.stream().filter(ValidDiagnosisKeyFilter::isDiagnosisKeyValid).collect(Collectors.toList());

    int numberOfDiscardedKeys = diagnosisKeys.size() - validDiagnosisKeys.size();
    logger.info("Retrieved {} diagnosis key(s). Discarded {} diagnosis key(s) from the result as invalid.",
        diagnosisKeys.size(), numberOfDiscardedKeys);

    return validDiagnosisKeys;
  }

  private static boolean isDiagnosisKeyValid(DiagnosisKey diagnosisKey) {
    Collection<ConstraintViolation<DiagnosisKey>> violations = diagnosisKey.validate();
    boolean isValid = violations.isEmpty();

    if (!isValid) {
      List<String> violationMessages =
          violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.toList());
      logger.warn("Validation failed for diagnosis key from database. Violations: {}", violationMessages);
    }

    return isValid;
  }

}
