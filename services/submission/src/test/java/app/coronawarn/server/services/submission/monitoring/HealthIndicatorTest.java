
package app.coronawarn.server.services.submission.monitoring;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import app.coronawarn.server.services.submission.verification.SrsVerifyClient;
import app.coronawarn.server.services.submission.verification.VerificationServerClient;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@TestPropertySource(properties = { "management.port=" })
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
class HealthIndicatorTest {

  @MockBean
  private VerificationServerClient verificationClient;

  @Autowired
  private WebApplicationContext context;

  private MockMvc mvc;

  @MockBean
  private SrsVerifyClient srsClient;

  @BeforeEach
  public void beforeEach() {
    mvc = MockMvcBuilders.webAppContextSetup(context).build();
  }

  @Test
  void checkIsAliveEvenIfVerificationServerIsDown() throws Exception {
    when(verificationClient.verifyTan(any())).thenThrow(FeignException.InternalServerError.class);
    when(srsClient.verifyOtp(any())).thenThrow(FeignException.InternalServerError.class);
    mvc.perform(get("/actuator/health/liveness")).andExpect(status().is2xxSuccessful()).andReturn();
  }

  @Test
  void checkIsHealthyIfVerificationServerIsRunning() throws Exception {
    when(verificationClient.verifyTan(any())).thenReturn(null);
    when(srsClient.verifyOtp(any())).thenReturn(null);
    mvc.perform(get("/actuator/health")).andExpect(status().is2xxSuccessful()).andReturn();
  }

  @Test
  void checkIsHealthyIfVerificationServerIsRunningAndExceptionIsThrown() throws Exception {
    when(verificationClient.verifyTan(any())).thenThrow(FeignException.NotFound.class);
    when(srsClient.verifyOtp(any())).thenThrow(FeignException.NotFound.class);
    mvc.perform(get("/actuator/health")).andExpect(status().is2xxSuccessful()).andReturn();
  }

  @Test
  void checkIsNotReadyIfVerificationServerIsDown() throws Exception {
    when(verificationClient.verifyTan(any())).thenThrow(FeignException.InternalServerError.class);
    when(srsClient.verifyOtp(any())).thenThrow(FeignException.InternalServerError.class);
    mvc.perform(get("/actuator/health/readiness")).andExpect(status().isServiceUnavailable()).andReturn();
  }

  @Test
  void checkIsUnhealthyIfOneServerIsDown() throws Exception {
    when(verificationClient.verifyTan(any())).thenReturn(null);
    when(srsClient.verifyOtp(any())).thenThrow(FeignException.InternalServerError.class);
    mvc.perform(get("/actuator/health")).andExpect(status().isServiceUnavailable()).andReturn();
  }

  @Test
  void checkIsUnhealthyIfOtherServerIsDown() throws Exception {
    when(verificationClient.verifyTan(any())).thenThrow(FeignException.InternalServerError.class);
    when(srsClient.verifyOtp(any())).thenReturn(null);
    mvc.perform(get("/actuator/health")).andExpect(status().isServiceUnavailable()).andReturn();
  }

  @Test
  void checkIsUnhealthyIfVerificationServerIsDown() throws Exception {
    when(verificationClient.verifyTan(any())).thenThrow(FeignException.InternalServerError.class);
    when(srsClient.verifyOtp(any())).thenThrow(FeignException.InternalServerError.class);
    mvc.perform(get("/actuator/health")).andExpect(status().isServiceUnavailable()).andReturn();
  }
}
