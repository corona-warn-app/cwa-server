

package app.coronawarn.server.services.submission.controller;

import static app.coronawarn.server.services.submission.controller.SubmissionController.CWA_FILTERED_CHECKINS_HEADER;
import static app.coronawarn.server.services.submission.controller.SubmissionController.CWA_SAVED_CHECKINS_HEADER;
import static app.coronawarn.server.services.submission.controller.SubmissionController.SUBMISSION_ROUTE;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import app.coronawarn.server.services.submission.monitoring.SubmissionMonitor;
import io.micrometer.core.annotation.Timed;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

@RestController
@RequestMapping("/version/v1")
public class FakeRequestController {

  private final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(4);
  private final SubmissionMonitor submissionMonitor;
  private final FakeDelayManager fakeDelayManager;

  FakeRequestController(SubmissionMonitor submissionMonitor, FakeDelayManager fakeDelayManager) {
    this.submissionMonitor = submissionMonitor;
    this.fakeDelayManager = fakeDelayManager;
  }

  /**
   * Handles "fake" requests. The concept of fake (or "dummy") requests is a privacy preserving measure which is
   * characterized by having corona warn app send "dummy" requests in randomized intervals. These requests are not
   * triggering any diagnosis key processing/storage on the server but simply result in an HTTP response with status
   * code 200 (OK) after a dynamically calculated delay.
   *
   * @param fake The header flag, marking fake requests.
   * @return An empty response body and HTTP status code 200 (OK).
   */
  @PostMapping(value = SUBMISSION_ROUTE, headers = {"cwa-fake!=0"})
  @Timed(description = "Time spent handling fake submission.")
  public DeferredResult<ResponseEntity<Void>> fakeRequest(@RequestHeader("cwa-fake") Integer fake) {
    submissionMonitor.incrementRequestCounter();
    submissionMonitor.incrementFakeRequestCounter();
    long delay = fakeDelayManager.getJitteredFakeDelay();
    DeferredResult<ResponseEntity<Void>> deferredResult = new DeferredResult<>();
    ResponseEntity response = ResponseEntity.ok()
        .header(CWA_FILTERED_CHECKINS_HEADER, String.valueOf(0))
        .header(CWA_SAVED_CHECKINS_HEADER, String.valueOf(0))
        .build();
    scheduledExecutor.schedule(() -> deferredResult.setResult(response), delay, MILLISECONDS);
    return deferredResult;
  }
}
