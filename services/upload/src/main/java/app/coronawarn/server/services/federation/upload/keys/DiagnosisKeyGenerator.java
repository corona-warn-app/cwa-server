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

package app.coronawarn.server.services.federation.upload.keys;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.domain.FederationUploadKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import app.coronawarn.server.services.federation.upload.config.UploadServiceConfig;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("testdata")
public class DiagnosisKeyGenerator implements DiagnosisKeyLoader {

  private final Random random = new Random();

  private static final Logger logger = LoggerFactory.getLogger(DiagnosisKeyGenerator.class);

  private final UploadServiceConfig uploadServiceConfig;

  public DiagnosisKeyGenerator(UploadServiceConfig uploadServiceConfig) {
    this.uploadServiceConfig = uploadServiceConfig;
  }

  private FederationUploadKey generateKey(int ignoredValue) {
    byte[] randomKeyData = new byte[16];
    random.nextBytes(randomKeyData);
    return FederationUploadKey.from(DiagnosisKey.builder()
        .withKeyData(randomKeyData)
        .withRollingStartIntervalNumber(1)
        .withTransmissionRiskLevel(1)
        .withReportType(ReportType.CONFIRMED_CLINICAL_DIAGNOSIS)
        .withConsentToFederation(true)
        .withCountryCode("DE")
        .withVisitedCountries(List.of("DE"))
        .build());
  }

  @Override
  public List<FederationUploadKey> loadDiagnosisKeys() {
    var keys = uploadServiceConfig.getTestData().getKeys();
    logger.info("Generating {} fake diagnosis keys for upload", keys);
    return IntStream.range(0, keys)
        .mapToObj(this::generateKey)
        .collect(Collectors.toList());
  }
}
