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

package app.coronawarn.server.common.persistence;

import app.coronawarn.server.common.persistence.repository.DiagnosisKeyRepository;
import app.coronawarn.server.common.persistence.repository.FederationUploadKeyRepository;
import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.common.persistence.service.FederationUploadKeyService;
import app.coronawarn.server.common.persistence.service.common.ValidDiagnosisKeyFilter;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
@Configuration
public class TestApplication {

  @Bean
  ValidDiagnosisKeyFilter validKeysFilter() {
    return new ValidDiagnosisKeyFilter();
  }

  @Bean
  DiagnosisKeyService createDiagnosisKeyService(DiagnosisKeyRepository keyRepository) {
    return new DiagnosisKeyService(keyRepository, validKeysFilter());
  }

  @Bean
  FederationUploadKeyService createFederationUploadKeyService(FederationUploadKeyRepository keyRepository) {
    return new FederationUploadKeyService(keyRepository, validKeysFilter());
  }
}
