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

package app.coronawarn.server.services.submission.controller;

import static app.coronawarn.server.services.submission.controller.RequestExecutor.buildOkHeaders;
import static app.coronawarn.server.services.submission.controller.RequestExecutor.buildPayloadWithOneKey;
import static app.coronawarn.server.services.submission.controller.RequestExecutor.setCwaFakeHeader;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.OK;

import app.coronawarn.server.services.submission.monitoring.SubmissionMonitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"disable-ssl-client-verification", "disable-ssl-client-verification-verify-hostname"})
class FakeRequestControllerTest {

  @Autowired
  private RequestExecutor executor;

  @MockBean
  private FakeDelayManager fakeDelayManager;

  @MockBean
  private SubmissionMonitor submissionMonitor;

  @BeforeEach
  public void setUpMocks() {
    when(fakeDelayManager.getJitteredFakeDelay()).thenReturn(1000L);
  }

  @Test
  void fakeRequestHandling() {
    HttpHeaders headers = buildOkHeaders();
    setCwaFakeHeader(headers, "1");

    ResponseEntity<Void> actResponse = executor.executePost(buildPayloadWithOneKey(), headers);

    verify(fakeDelayManager, times(1)).getJitteredFakeDelay();
    verify(fakeDelayManager, never()).updateFakeRequestDelay(anyLong());
    assertThat(actResponse.getStatusCode()).isEqualTo(OK);
  }

  @Test
  void checkFakeRequestHandlingIsMonitored() {
    HttpHeaders headers = buildOkHeaders();
    setCwaFakeHeader(headers, "1");

    executor.executePost(buildPayloadWithOneKey(), headers);

    verify(submissionMonitor, times(1)).incrementRequestCounter();
    verify(submissionMonitor, never()).incrementRealRequestCounter();
    verify(submissionMonitor, times(1)).incrementFakeRequestCounter();
    verify(submissionMonitor, never()).incrementInvalidTanRequestCounter();
  }
}
