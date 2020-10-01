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

public enum FederationBatchStatus {
  /**
   * The corresponding batch has not been processed yet.
   */
  UNPROCESSED,
  /**
   * The corresponding batch has been processed.
   */
  PROCESSED,
  /**
   * The corresponding batch has been processed.
   * Some keys did not pass validation.
   */
  PROCESSED_WITH_FAILURES,
  /**
   * An error occurred while processing the batch.
   */
  ERROR,
  /**
   * Processing a batch failed for the second time and will not be attempted again.
   */
  ERROR_WONT_RETRY
}
