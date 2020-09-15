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

package app.coronawarn.server.services.federation.upload.runner;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.services.federation.upload.Application;
import app.coronawarn.server.services.federation.upload.client.FederationUploadClient;
import app.coronawarn.server.services.federation.upload.keys.DiagnosisKeyLoader;
import app.coronawarn.server.services.federation.upload.payload.PayloadFactory;
import app.coronawarn.server.services.federation.upload.payload.UploadPayload;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class Upload implements ApplicationRunner {

  private static final Logger logger = LoggerFactory
      .getLogger(Upload.class);
  private final FederationUploadClient federationUploadClient;
  private final PayloadFactory payloadFactory;
  private final DiagnosisKeyLoader diagnosisKeyLoader;
  private final ApplicationContext applicationContext;

  /**
   * Creates an upload runner instance that reads Upload keys and send them to the Federation Gateway.
   *
   * @param federationUploadClient {@link FederationUploadClient} instance to call the EFGS API.
   * @param payloadFactory         {@link PayloadFactory} to generate the Payload Objects with proper batching and
   *                               signing.
   * @param diagnosisKeyLoader     {@link DiagnosisKeyLoader} to load DiagnosisKeys from the Upload table.
   */
  public Upload(
      FederationUploadClient federationUploadClient,
      PayloadFactory payloadFactory,
      DiagnosisKeyLoader diagnosisKeyLoader,
      ApplicationContext applicationContext) {
    this.federationUploadClient = federationUploadClient;
    this.payloadFactory = payloadFactory;
    this.diagnosisKeyLoader = diagnosisKeyLoader;
    this.applicationContext = applicationContext;
  }

  private void executeFederationUpload(UploadPayload payload) {
    logger.info("Executing batch request(s): {}", payload.getBatchTag());
    var result = this.federationUploadClient.postBatchUpload(payload);
    var retryKeys = result.getStatus500()
        .stream()
        .map(Integer::parseInt)
        .map(index -> payload.getOrderedKeys().get(index))
        .collect(Collectors.toList());
    logger.info("Error on {} keys, marking them for retry", retryKeys.size());
  }

  @Override
  public void run(ApplicationArguments args) throws Exception {
    logger.info("Running Upload Job");
    try {
      List<DiagnosisKey> diagnosisKeys = this.diagnosisKeyLoader.loadDiagnosisKeys();
      logger.info("Generating Upload Payload for {} keys", diagnosisKeys.size());
      List<UploadPayload> requests = this.payloadFactory.makePayloadList(diagnosisKeys);
      logger.info("Executing {} batch request", requests.size());
      requests.forEach(this::executeFederationUpload);
    } catch (Exception e) {
      logger.error("Upload diagnosis key data failed.", e);
      Application.killApplication(applicationContext);
    }
  }
}
