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

package app.coronawarn.server.services.download.validation;

import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Responsible for checking fields of the {@link DiagnosisKey} objects contained within batches
 * downloaded from the EFGS. This check is prior to the one executed when building the domain
 * {@link app.coronawarn.server.common.persistence.domain.DiagnosisKey} entity which ensures our
 * data model constraints are not violated for any incoming data channel.
 */
@Component
public class ValidFederationKeyFilter {

  private static final Logger logger = LoggerFactory.getLogger(ValidFederationKeyFilter.class);

  /**
   * Accepts or rejects a key based on the evaluation of the fields against permitted values.
   */
  public boolean isValid(
      app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKey federationKey) {
    return hasValidDaysSinceOnsetOfSymptoms(federationKey);
  }

  private boolean hasValidDaysSinceOnsetOfSymptoms(DiagnosisKey federationKey) {
    boolean hasValidDsos = federationKey.hasDaysSinceOnsetOfSymptoms()
        && federationKey.getDaysSinceOnsetOfSymptoms() >= -14
        && federationKey.getDaysSinceOnsetOfSymptoms() <= 4000;
    if (!hasValidDsos) {
      logger.info("Federation DiagnosisKey found with invalid 'daysSinceOnsetOfSymptoms' value {}",
          federationKey.getDaysSinceOnsetOfSymptoms());
    }
    return hasValidDsos;
  }
}
