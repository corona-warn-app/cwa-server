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

package app.coronawarn.server.services.download.normalization;


import app.coronawarn.server.common.persistence.domain.normalization.DiagnosisKeyNormalizer;
import app.coronawarn.server.common.persistence.domain.normalization.NormalizableFields;
import app.coronawarn.server.services.download.DownloadServiceConfig;
import app.coronawarn.server.services.download.DownloadServiceConfig.TekFieldDerivations;

/**
 * This class is used to derive transmission risk level using the days since onset of symptoms.
 */
public class FederationKeyNormalizer implements DiagnosisKeyNormalizer {

  private final TekFieldDerivations tekFieldDerivations;

  /**
   * Constructor for this class.
   *
   * @param config A {@link DownloadServiceConfig} object.
   */
  public FederationKeyNormalizer(DownloadServiceConfig config) {
    this.tekFieldDerivations = config.getTekFieldDerivations();
  }

  @Override
  public NormalizableFields normalize(NormalizableFields fieldsAndValues) {
    validateNormalizableFields(fieldsAndValues);
    int trl = tekFieldDerivations.deriveTrlFromDsos(fieldsAndValues.getDaysSinceOnsetOfSymptoms());
    return NormalizableFields.of(trl, fieldsAndValues.getDaysSinceOnsetOfSymptoms());
  }

  private void validateNormalizableFields(NormalizableFields fieldsAndValues) {
    if (fieldsAndValues.getDaysSinceOnsetOfSymptoms() == null) {
      throw new IllegalArgumentException("Days since onset of symptoms is missing!");
    }
  }

}
