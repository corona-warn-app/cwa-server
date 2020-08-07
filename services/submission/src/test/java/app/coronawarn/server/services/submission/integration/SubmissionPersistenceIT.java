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

package app.coronawarn.server.services.submission.integration;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static app.coronawarn.server.services.submission.assertions.SubmissionAssertions.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload;
import app.coronawarn.server.services.submission.config.SubmissionServiceConfig;
import app.coronawarn.server.services.submission.controller.FakeDelayManager;
import app.coronawarn.server.services.submission.controller.RequestExecutor;
import app.coronawarn.server.services.submission.verification.TanVerifier;

/**
 * This test serves more like a dev tool which helps with debugging production issues.
 * It inserts keys parsed from a proto buf file whos content was captured by the mobile
 * client during requests to the server. The content of the current test resource file
 * can be quckly replaced durng the investigation of an issue.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({ "disable-ssl-client-verification", "disable-ssl-client-verification-verify-hostname" })
@Sql(scripts = {"classpath:db/clean_db_state.sql"},
                executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
public class SubmissionPersistenceIT {

  @Autowired
  private DiagnosisKeyService diagnosisKeyService;

  @Autowired
  private RequestExecutor executor;

  @Autowired
  private SubmissionServiceConfig config;

  @MockBean
  private TanVerifier tanVerifier;

  @MockBean
  private FakeDelayManager fakeDelayManager;

  @BeforeEach
  public void setUpMocks() {
    when(tanVerifier.verifyTan(anyString())).thenReturn(true);
    when(fakeDelayManager.getJitteredFakeDelay()).thenReturn(1000L);
  }

  @Disabled("Because the content of the .pb file becomes old and retention time passes, this test will fail. "
      + "Enable when debugging of a new payload is required.")
  @ParameterizedTest
  @ValueSource(strings = { "src/test/resources/payload/mobile-client-payload.pb" })
  public void testKeyInsertionWithMobileClientProtoBuf(String testFile) throws IOException {
    Path path = Paths.get(testFile);
    InputStream input = new FileInputStream(path.toFile());
    SubmissionPayload payload = SubmissionPayload.parseFrom(input);

    executor.executePost(payload);
    assertElementsCorrespondToEachOther(payload.getKeysList(), diagnosisKeyService.getDiagnosisKeys(), config);
  }
}
