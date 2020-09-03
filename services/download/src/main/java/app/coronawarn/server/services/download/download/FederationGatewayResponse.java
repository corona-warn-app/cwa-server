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

package app.coronawarn.server.services.download.download;

import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKeyBatch;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

/**
 * Contains the {@link DiagnosisKeyBatch} and batch tag metadata as served by the federation gateway.
 */
public class FederationGatewayResponse {

  private final DiagnosisKeyBatch diagnosisKeyBatch;
  private final String batchTag;
  private final Optional<String> nextBatchTag;
  private final LocalDate date;

  /**
   * Creates a FederationGatewayResponse.
   */
  public FederationGatewayResponse(
      DiagnosisKeyBatch diagnosisKeyBatch, String batchTag, Optional<String> nextBatchTag, LocalDate date) {
    this.diagnosisKeyBatch = diagnosisKeyBatch;
    this.batchTag = batchTag;
    this.nextBatchTag = nextBatchTag;
    this.date = date;
  }

  public DiagnosisKeyBatch getDiagnosisKeyBatch() {
    return diagnosisKeyBatch;
  }

  public String getBatchTag() {
    return batchTag;
  }

  public Optional<String> getNextBatchTag() {
    return nextBatchTag;
  }

  public LocalDate getDate() {
    return date;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FederationGatewayResponse that = (FederationGatewayResponse) o;
    return Objects.equals(diagnosisKeyBatch, that.diagnosisKeyBatch)
        && Objects.equals(batchTag, that.batchTag)
        && Objects.equals(nextBatchTag, that.nextBatchTag)
        && Objects.equals(date, that.date);
  }

  @Override
  public int hashCode() {
    return Objects.hash(diagnosisKeyBatch, batchTag, nextBatchTag, date);
  }
}
