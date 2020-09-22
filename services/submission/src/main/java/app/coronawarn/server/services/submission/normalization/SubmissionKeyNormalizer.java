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

package app.coronawarn.server.services.submission.normalization;

import app.coronawarn.server.common.persistence.domain.normalization.DiagnosisKeyNormalizer;
import app.coronawarn.server.common.persistence.domain.normalization.NormalizableFields;
import app.coronawarn.server.services.submission.config.SubmissionServiceConfig;
import java.util.Map;

public final class SubmissionKeyNormalizer implements DiagnosisKeyNormalizer{

  private final Map<Integer, Integer> dsosFromTrlMap;

  public SubmissionKeyNormalizer(SubmissionServiceConfig config) {
     dsosFromTrlMap = config.getTekPropertyDerivations().getDsosFromTrl();
  }

  @Override
  public NormalizableFields normalize(NormalizableFields fieldsAndValues) {
    int trlValue = fieldsAndValues.getTransmissionRiskLevel();
    int dsos = fieldsAndValues.getDaysSinceOnsetOfSymptoms();

    if(isMissing(trlValue) && isMissing(dsos)) {
      throw new IllegalArgumentException("Normalization of key values failed. A key was provided with"
          + " both 'transmission risk level' and 'days since onset of symptoms' fields missing");
    }

    if (isMissing(trlValue)) {
      //TODO ...implement for trl
    } else if(isMissing(dsos)) {
      dsos = dsosFromTrlMap.getOrDefault(trlValue, 0); //TODO : What is the default? Should this pass validation?
    }

    return NormalizableFields.of(trlValue, dsos);
  }

  private boolean isMissing(int fieldValue) {
    return fieldValue <= 0;
  }
}
