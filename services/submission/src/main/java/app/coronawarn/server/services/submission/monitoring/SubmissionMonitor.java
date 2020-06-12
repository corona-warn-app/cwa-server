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

package app.coronawarn.server.services.submission.monitoring;

import app.coronawarn.server.services.submission.config.SubmissionServiceConfig;
import app.coronawarn.server.services.submission.controller.FakeDelayManager;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Provides functionality for monitoring the diagnosis key submission handling logic.
 */
@Component
@ConfigurationProperties(prefix = "services.submission.monitoring")
public class SubmissionMonitor {

  private static final String SUBMISSION_CONTROLLER_CURRENT_FAKE_DELAY = "submission_controller.fake_delay_seconds";

  private final MeterRegistry meterRegistry;
  private final long batchSize;
  private BatchCounter requests;
  private BatchCounter realRequests;
  private BatchCounter fakeRequests;
  private BatchCounter invalidTanRequests;

  /**
   * Constructor for {@link SubmissionMonitor}. Initializes all counters to 0 upon being called.
   *
   * @param meterRegistry the meterRegistry
   * @param fakeDelayManager the fake delay manager whose fake delay will be monitored
   */
  protected SubmissionMonitor(
      MeterRegistry meterRegistry, SubmissionServiceConfig submissionServiceConfig, FakeDelayManager fakeDelayManager) {
    this.meterRegistry = meterRegistry;
    this.batchSize = submissionServiceConfig.getMonitoringBatchSize();
    initializeCounters();
    initializeGauges(fakeDelayManager);
  }

  /**
   * We count the following values.
   *  <ul>
   *    <li> All requests that reach the controllers.
   *    <li> As part of all, the number of requests that are not fake.
   *    <li> As part of all, the number of requests that are fake.
   *    <li> As part of all, the number of requests for that the TAN-validation failed.
   *  </ul>
   */
  private void initializeCounters() {
    requests = new BatchCounter(meterRegistry, batchSize, "all");
    realRequests = new BatchCounter(meterRegistry, batchSize, "real");
    fakeRequests = new BatchCounter(meterRegistry, batchSize, "fake");
    invalidTanRequests = new BatchCounter(meterRegistry, batchSize, "invalidTan");
  }

  /**
   * Initializes the gauges for the {@link FakeDelayManager} that is being monitored. Currently, only the delay time of
   * fake requests is measured.
   *
   * @param fakeDelayManager the fake request handler for which the gauges shall be initialized
   */
  private void initializeGauges(FakeDelayManager fakeDelayManager) {
    Gauge.builder(SUBMISSION_CONTROLLER_CURRENT_FAKE_DELAY, fakeDelayManager,
        __ -> fakeDelayManager.getFakeDelayInSeconds())
        .description("The time that fake requests are delayed to make them indistinguishable from real requests.")
        .register(meterRegistry);
  }

  public void incrementRequestCounter() {
    requests.increment();
  }

  public void incrementRealRequestCounter() {
    realRequests.increment();
  }

  public void incrementFakeRequestCounter() {
    fakeRequests.increment();
  }

  public void incrementInvalidTanRequestCounter() {
    invalidTanRequests.increment();
  }
}
