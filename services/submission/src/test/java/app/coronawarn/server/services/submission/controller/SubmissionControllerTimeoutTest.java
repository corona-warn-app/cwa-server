package app.coronawarn.server.services.submission.controller;

import static app.coronawarn.server.services.submission.controller.SubmissionPayloadMockData.buildMultipleKeys;
import static app.coronawarn.server.services.submission.controller.SubmissionPayloadMockData.buildPayloadWithPadding;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import app.coronawarn.server.services.submission.config.SubmissionServiceConfig;
import app.coronawarn.server.services.submission.verification.TanVerifier;
import java.net.URI;
import javax.servlet.AsyncListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockAsyncContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@TestInstance(Lifecycle.PER_CLASS)
@AutoConfigureMockMvc
public class SubmissionControllerTimeoutTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private SubmissionServiceConfig config;

  @MockBean
  private TanVerifier tanVerifier;

  @BeforeEach
  public void setUpMocks() {
    when(tanVerifier.verifyTan(anyString())).thenReturn(true);
  }

  @Test
  void checkTimeoutExceptionHandling() throws Exception {
    final URI SUBMISSION_URL = URI.create("/version/v1/diagnosis-keys");

    MvcResult result = mockMvc.perform(post(SUBMISSION_URL)
        .content(buildPayloadWithPadding(buildMultipleKeys(config)).toByteArray()).headers(HttpHeaderBuilder.builder()
            .contentTypeProtoBuf()
            .cwaAuth()
            .withoutCwaFake()
            .build()))
        .andReturn();
    MockAsyncContext ctx = (MockAsyncContext) result.getRequest().getAsyncContext();
    for (AsyncListener listener : ctx.getListeners()) {
      listener.onTimeout(null);
    }
    mockMvc.perform(asyncDispatch(result))
        .andExpect(status().isInternalServerError());
  }
}
