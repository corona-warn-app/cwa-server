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

package app.coronawarn.server.services.submission.healthindicator;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import app.coronawarn.server.services.submission.controller.RequestExecutor;
import app.coronawarn.server.services.submission.verification.VerificationServerClient;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;


@TestPropertySource(properties = {"management.port="})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class VerificationServiceHealthIndicatorTest {

  @MockBean
  private VerificationServerClient verificationServerClient;

  @Autowired
  private WebApplicationContext context;

  private MockMvc mvc;

  @BeforeEach
  public void setup() {
    mvc = MockMvcBuilders.webAppContextSetup(context).build();
  }

  @Test
  void checkIsHealthyIfVerificationServerIsRunning() throws Exception {
    when(verificationServerClient.verifyTan(any())).thenThrow(FeignException.NotFound.class);
    mvc.perform(get("/actuator/health"))
        .andExpect(status().is2xxSuccessful()).andReturn();
  }

  @Test
  void checkIsUnhealthyIfVerificationServerIsDown() throws Exception {
    when(verificationServerClient.verifyTan(any())).thenThrow(FeignException.InternalServerError.class);
    mvc.perform(get("/actuator/health"))
        .andExpect(status().isServiceUnavailable()).andReturn();
  }

  @Test
  void checkIsNotReadyIfVerificationServerIsDown() throws Exception {
    when(verificationServerClient.verifyTan(any())).thenThrow(FeignException.InternalServerError.class);
    mvc.perform(get("/actuator/health/readiness"))
        .andExpect(status().isServiceUnavailable()).andReturn();
  }

  @Test
  void checkIsAliveEvenIfVerificationServerIsDown() throws Exception {
    when(verificationServerClient.verifyTan(any())).thenThrow(FeignException.InternalServerError.class);
    mvc.perform(get("/actuator/health/liveness"))
        .andExpect(status().is2xxSuccessful()).andReturn();
  }
}
