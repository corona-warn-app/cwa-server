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

package app.coronawarn.server.services.federation.upload.payload;

import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKeyBatch;

/**
 * This class represents an Upload call to the Federation Gateway. The payload for EFGS must contain the following
 * information:
 *
 * <p><ul>
 * <li> The bytes of the protobuf ({@link DiagnosisKeyBatch} batch). </li>
 * <li> The signature bytes (String batchSignature). </li>
 * <li> The unique batch tag (String batchTag). </li>
 * </p></ul>
 */
public class UploadPayload {

  private DiagnosisKeyBatch batch;
  private String batchSignature;
  private String batchTag;

  public DiagnosisKeyBatch getBatch() {
    return batch;
  }

  public UploadPayload setBatch(DiagnosisKeyBatch batch) {
    this.batch = batch;
    return this;
  }

  public String getBatchSignature() {
    return batchSignature;
  }

  public UploadPayload setBatchSignature(String batchSignature) {
    this.batchSignature = batchSignature;
    return this;
  }

  public String getBatchTag() {
    return batchTag;
  }

  public UploadPayload setBatchTag(String batchTag) {
    this.batchTag = batchTag;
    return this;
  }
}
