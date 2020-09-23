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

package app.coronawarn.server.services.federation.upload.client;

import app.coronawarn.server.common.federation.client.upload.BatchUploadResponse;
import app.coronawarn.server.services.federation.upload.payload.UploadPayload;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("fake-client")
public class TestFederationUploadClient implements FederationUploadClient {

  private static final Logger logger = LoggerFactory
      .getLogger(TestFederationUploadClient.class);

  @Override
  public BatchUploadResponse postBatchUpload(UploadPayload uploadPayload) {
    logger.info("Calling fake batch upload with: \n\tkeys:{}\n\tbatchTag:{}\n\tbatchSignature:{}",
        uploadPayload.getBatch().getKeysCount(),
        uploadPayload.getBatchTag(),
        uploadPayload.getBatchSignature());
    return new BatchUploadResponse(
        Collections.emptyList(),
        Collections.emptyList(),
        Collections.emptyList()
    );
  }
}
