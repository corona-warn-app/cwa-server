/*
 * -
 *  * ---license-start
 *  * Corona-Warn-App
 *  * ---
 *  * Copyright (C) 2020 SAP SE and all other contributors
 *  * ---
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  * ---license-end
 *
 */

package app.coronawarn.server.services.submission.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

/**
 * Provides functionality for monitoring the application logic of the
 * {@link app.coronawarn.server.services.submission.controller.SubmissionController}.
 */
@Component
public class SubmissionControllerMonitor {
  private static final String SUBMISSION_CONTROLLER_REQUESTS_COUNTER_NAME = "submissionController.requests";

  private final MeterRegistry meterRegistry;

  private Counter realRequests;
  private Counter fakeRequests;
  private Counter invalidTanRequests;

  /**
   * Constructor for {@link SubmissionControllerMonitor}. Initializes
   * all metrics upon being called.
   * @param meterRegistry the meterRegistry
   */
  public SubmissionControllerMonitor(MeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;
    initializeCounters();
  }

  private void initializeCounters() {
    realRequests = Counter.builder(SUBMISSION_CONTROLLER_REQUESTS_COUNTER_NAME)
        .tag("type", "real")
        .description("")
        .register(meterRegistry);

    fakeRequests = Counter.builder(SUBMISSION_CONTROLLER_REQUESTS_COUNTER_NAME)
        .tag("type", "fake")
        .description("")
        .register(meterRegistry);

    invalidTanRequests = Counter.builder(SUBMISSION_CONTROLLER_REQUESTS_COUNTER_NAME)
        .tag("type", "invalidTan")
        .description("")
        .register(meterRegistry);
  }

  public void incrementReal() {
    realRequests.increment();
  }

  public void incrementFake() {
    fakeRequests.increment();
  }

  public void incrementInvalidTan() {
    invalidTanRequests.increment();
  }


}
