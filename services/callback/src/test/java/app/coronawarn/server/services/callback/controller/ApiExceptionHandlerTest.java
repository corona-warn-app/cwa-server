package app.coronawarn.server.services.callback.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import app.coronawarn.server.common.persistence.service.FederationBatchInfoService;

@RunWith(MockitoJUnitRunner.class)
class ApiExceptionHandlerTest {

  private static final String ENDPOINT = "/version/v1/callback";

  private MockMvc mockMvc;

  @Mock
  private FederationBatchInfoService serviceMock;
  private CallbackController callbackController;

  @BeforeEach
  public void setup() {
    MockitoAnnotations.initMocks(this);
    callbackController = new CallbackController(serviceMock);
    this.mockMvc = MockMvcBuilders.standaloneSetup(callbackController).setControllerAdvice(new ApiExceptionHandler())
        .build();
  }

  @Test
  public void testUnexpectedExceptionsTriggersStatusCode500() throws Exception {
    doThrow(new RuntimeException("Unexpected Exception")).when(serviceMock).save(any());

    mockMvc.perform(get(ENDPOINT).param("batchTag", "SOMEBATCHTAG").param("date", "2020-11-31"))
        .andExpect(status().isInternalServerError());
  }

  @Test
  public void testMissingBatchTagTriggersStatusCode400() throws Exception {
    mockMvc.perform(get(ENDPOINT)).andExpect(status().isBadRequest());
    mockMvc.perform(get(ENDPOINT).param("date", "2020-11-31")).andExpect(status().isBadRequest());
  }
}
