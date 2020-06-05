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

import app.coronawarn.server.services.submission.config.SubmissionServiceConfig;
import app.coronawarn.server.services.submission.controller.SubmissionController;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Provides functionality for monitoring the application logic of the
 * {@link app.coronawarn.server.services.submission.controller.SubmissionController}.
 */
@Component
@ConfigurationProperties(prefix = "services.submission.monitoring")
public class SubmissionControllerMonitor {

  private static final String SUBMISSION_CONTROLLER_REQUESTS_COUNTER_NAME = "submissionController.requests";
  private static final String SUBMISSION_CONTROLLER_CURRENT_FAKE_DELAY = "submissionController.fakeDelay";

  private final MeterRegistry meterRegistry;

  private final Integer batchSize;
  private Counter realRequests;
  private Double realRequestsBatch = 0.;
  private Counter fakeRequests;
  private Double fakeRequestsBatch = 0.;
  private Counter invalidTanRequests;
  private Double invalidTanRequestsBatch = 0.;

  /**
   * Constructor for {@link SubmissionControllerMonitor}. Initializes all counters upon being called.
   *
   * @param meterRegistry the meterRegistry
   */
  protected SubmissionControllerMonitor(MeterRegistry meterRegistry, SubmissionServiceConfig submissionServiceConfig) {
    this.meterRegistry = meterRegistry;
    this.batchSize = submissionServiceConfig.getMonitoringBatchSize();
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

  /**
   * Initializes the gauges of this monitor.
   *
   * @param submissionController init
   */
  public void initializeGauges(SubmissionController submissionController) {
    Gauge.builder(SUBMISSION_CONTROLLER_CURRENT_FAKE_DELAY, submissionController,
        __ -> submissionController.getFakeDelay())
        .description("")
        .register(meterRegistry);
  }

  /**
   * Increment request counter in steps of batch size. This is done to prevent being able to retrace requests using time
   * stamps.
   */
  public void incrementReal() {
    if (realRequestsBatch < batchSize) {
      realRequestsBatch++;
    } else {
      realRequests.increment(realRequestsBatch);
      realRequestsBatch = 1.;
    }
  }

  /**
   * Increment fake request counter in steps of batch size. This is done to prevent being able to retrace requests using
   * time stamps.
   */
  public void incrementFake() {
    if (fakeRequestsBatch < batchSize) {
      fakeRequestsBatch++;
    } else {
      fakeRequests.increment(fakeRequestsBatch);
      fakeRequestsBatch = 1.;
    }
  }

  /**
   * Increment invalid tan request counter in steps of batch size. This is done to prevent being able to retrace
   * requests using time stamps.
   */
  public void incrementInvalidTan() {
    if (invalidTanRequestsBatch < batchSize) {
      invalidTanRequestsBatch++;
    } else {
      invalidTanRequests.increment(invalidTanRequestsBatch);
      invalidTanRequestsBatch = 1.;
    }
  }


}
