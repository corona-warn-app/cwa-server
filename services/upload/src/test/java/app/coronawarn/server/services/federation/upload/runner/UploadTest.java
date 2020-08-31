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

import static app.coronawarn.server.services.federation.upload.utils.MockData.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.repository.FederationUploadKeyRepository;
import app.coronawarn.server.services.federation.upload.DiagnosisKeyBatchAssembler;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class UploadTest {

  @Autowired
  private Upload upload;

  @MockBean
  private FederationUploadKeyRepository uploadKeyRepository;

  @SpyBean
  private DiagnosisKeyBatchAssembler batchAssembler;

  @Test
  void batchesShouldBeCreatedFromPendingUploadKeys() throws Exception {
    List<DiagnosisKey> testKeys = generateRandomDiagnosisKeys(true, 20);
    Mockito.when(uploadKeyRepository.findAllUploadableKeys()).thenReturn(testKeys);
    upload.run(null);
    verify(batchAssembler, times(1)).assembleDiagnosisKeyBatch(testKeys);
  }

}
