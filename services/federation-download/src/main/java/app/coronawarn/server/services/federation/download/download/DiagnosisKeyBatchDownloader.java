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

package app.coronawarn.server.services.federation.download.download;

import app.coronawarn.server.common.federation.client.FederationGatewayClient;
import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKeyBatch;
import feign.Response;
import java.io.IOException;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


/**
 * The BatchDownloader downloads the batches containing the keys.
 */
@Service
public class DiagnosisKeyBatchDownloader {

  private static final Logger logger = LoggerFactory.getLogger(DiagnosisKeyBatchDownloader.class);

  private FederationGatewayClient federationGatewayClient;

  public DiagnosisKeyBatchDownloader(FederationGatewayClient federationGatewayClient) {
    this.federationGatewayClient = federationGatewayClient;
  }

  public DiagnosisKeyBatchContainer downloadBatch(LocalDate date) {
    try (Response response = federationGatewayClient.getDiagnosisKeys(
        "application/json; version=1.0",
        "abc",
        "C=DE",
        "2020-08-18")) {
      DiagnosisKeyBatch.parseFrom(response.body().asInputStream());
      // work with is
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public DiagnosisKeyBatchContainer downloadBatch(LocalDate date, String batchTag) {
    return null;
  }

}
