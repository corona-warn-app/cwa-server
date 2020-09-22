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

package app.coronawarn.server.common.persistence.domain.normalization;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;

/**
 * This data structure is just a container for all fields of a
 * {@link DiagnosisKey} to which normalization can be applied.
 */
public final class NormalizableFields {

  private final int transmissionRiskLevel;
  private final int daysSinceOnsetOfSymptoms;

  private NormalizableFields(int transmissionRiskLevel, int daysSinceOnsetOfSymptoms) {
    this.transmissionRiskLevel = transmissionRiskLevel;
    this.daysSinceOnsetOfSymptoms = daysSinceOnsetOfSymptoms;
  }

  public int getTransmissionRiskLevel() {
    return transmissionRiskLevel;
  }

  public int getDaysSinceOnsetOfSymptoms() {
    return daysSinceOnsetOfSymptoms;
  }

  public static NormalizableFields of(int transmissionRiskLevel, int daysSinceOnsetOfSymptoms) {
    return new NormalizableFields(transmissionRiskLevel, daysSinceOnsetOfSymptoms);
  }
}
