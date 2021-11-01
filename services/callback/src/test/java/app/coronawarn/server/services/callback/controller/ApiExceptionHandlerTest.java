package app.coronawarn.server.services.callback.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import app.coronawarn.server.common.persistence.domain.FederationBatchSourceSystem;
import app.coronawarn.server.common.persistence.service.FederationBatchInfoService;
import app.coronawarn.server.services.callback.config.CallbackServiceConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith({MockitoExtension.class})
class ApiExceptionHandlerTest {

  private static final String ENDPOINT = "/version/v1/callback";

  private MockMvc mockMvc;

  @Mock
  private FederationBatchInfoService serviceMock;
  @InjectMocks
  private CallbackController callbackController;
  @Mock
  private CallbackServiceConfig callbackServiceConfig;

  @BeforeEach
  public void setup() {
    this.mockMvc = standaloneSetup(callbackController).setControllerAdvice(new ApiExceptionHandler()).build();
  }

  @Test
  void testUnexpectedExceptionsTriggersStatusCode500() throws Exception {
    when(callbackServiceConfig.getSourceSystem()).thenReturn(FederationBatchSourceSystem.EFGS);
    doThrow(RuntimeException.class).when(serviceMock).save(any());
    mockMvc.perform(get(ENDPOINT).param("batchTag", "batchTag").param("date", "2020-05-05"))
        .andExpect(status().isInternalServerError());
  }

  @Test
  void testMissingBatchTagTriggersStatusCode400() throws Exception {
    mockMvc.perform(get(ENDPOINT)).andExpect(status().isBadRequest());
    mockMvc.perform(get(ENDPOINT).param("date", "2020-11-31")).andExpect(status().isBadRequest());
  }
}
