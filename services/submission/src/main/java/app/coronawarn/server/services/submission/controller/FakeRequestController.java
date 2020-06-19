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
    scheduledExecutor.schedule(() -> deferredResult.setResult(ResponseEntity.ok().build()), delay, MILLISECONDS);
    return deferredResult;
  }
}
