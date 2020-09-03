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

package app.coronawarn.server.common.persistence.domain;

import java.time.LocalDate;
import java.util.Objects;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;

/**
 * Information about federation batches with their status.
 */
public class FederationBatchInfo {

  @Id
  private final String batchTag;
  private final LocalDate date;
  private FederationBatchStatus status;

  /**
   * Creates a FederationBatchInfo and sets its status to {@link FederationBatchStatus#UNPROCESSED}.
   */
  public FederationBatchInfo(String batchTag, LocalDate date) {
    this(batchTag, date, FederationBatchStatus.UNPROCESSED);
  }

  /**
   * Creates a FederationBatchInfo.
   */
  @PersistenceConstructor
  public FederationBatchInfo(String batchTag, LocalDate date, FederationBatchStatus status) {
    this.batchTag = batchTag;
    this.date = date;
    this.status = status;
  }

  public String getBatchTag() {
    return batchTag;
  }

  public LocalDate getDate() {
    return date;
  }

  public FederationBatchStatus getStatus() {
    return status;
  }

  public void setStatus(FederationBatchStatus status) {
    this.status = status;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FederationBatchInfo that = (FederationBatchInfo) o;
    return Objects.equals(batchTag, that.batchTag)
        && Objects.equals(date, that.date)
        && status == that.status;
  }

  @Override
  public int hashCode() {
    return Objects.hash(batchTag, date, status);
  }
}
