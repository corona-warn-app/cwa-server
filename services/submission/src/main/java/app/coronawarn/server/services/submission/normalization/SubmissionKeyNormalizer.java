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
import app.coronawarn.server.services.submission.config.SubmissionServiceConfig.TekFieldDerivations;
import java.util.Objects;

public final class SubmissionKeyNormalizer implements DiagnosisKeyNormalizer {

  private TekFieldDerivations tekFieldMappings;

  public SubmissionKeyNormalizer(SubmissionServiceConfig config) {
    tekFieldMappings = config.getTekFieldDerivations();
  }

  @Override
  public NormalizableFields normalize(NormalizableFields fieldsAndValues) {
    Integer trlValue = fieldsAndValues.getTransmissionRiskLevel();
    Integer dsosValue = fieldsAndValues.getDaysSinceOnsetOfSymptoms();

    throwIfAllRequiredFieldsMissing(trlValue, dsosValue);

    if (isMissing(dsosValue)) {
      dsosValue = tekFieldMappings.deriveDsosFromTrl(trlValue);
    } else if (isMissing(trlValue)) {
      trlValue = tekFieldMappings.deriveTrlFromDsos(dsosValue);
    }

    return NormalizableFields.of(trlValue, dsosValue);
  }

  private void throwIfAllRequiredFieldsMissing(Integer trlValue, Integer dsos) {
    if (isMissing(trlValue) && isMissing(dsos)) {
      throw new IllegalArgumentException("Normalization of key values failed. A key was provided with"
          + " both 'transmissionRiskLevel' and 'daysSinceOnsetOfSymptoms' fields missing");
    }
  }

  private boolean isMissing(Integer fieldValue) {
    return Objects.isNull(fieldValue);
  }
}
