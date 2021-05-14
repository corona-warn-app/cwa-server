

package app.coronawarn.server.services.submission.controller;

import static app.coronawarn.server.services.submission.controller.SubmissionPayloadMockData.buildPayloadWithOneKey;
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
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
class FakeRequestControllerTest {

  @Autowired
  private RequestExecutor executor;

  @MockBean
  private FakeDelayManager fakeDelayManager;

  @MockBean
  private SubmissionMonitor submissionMonitor;

  private HttpHeaders headers;

  @BeforeEach
  public void setUpMocks() {
    when(fakeDelayManager.getJitteredFakeDelay()).thenReturn(1000L);
    headers = HttpHeaderBuilder.builder()
        .contentTypeProtoBuf()
        .cwaAuth()
        .withCwaFake()
        .build();
  }

  @Test
  void fakeRequestHandling() {
    ResponseEntity<Void> actResponse = executor.executePost(buildPayloadWithOneKey(), headers);

    verify(fakeDelayManager, times(1)).getJitteredFakeDelay();
    verify(fakeDelayManager, never()).updateFakeRequestDelay(anyLong());
    assertThat(actResponse.getStatusCode()).isEqualTo(OK);
  }

  @Test
  void checkFakeRequestHandlingIsMonitored() {
    executor.executePost(buildPayloadWithOneKey(), headers);

    verify(submissionMonitor, times(1)).incrementRequestCounter();
    verify(submissionMonitor, never()).incrementRealRequestCounter();
    verify(submissionMonitor, times(1)).incrementFakeRequestCounter();
    verify(submissionMonitor, never()).incrementInvalidTanRequestCounter();
  }
}
