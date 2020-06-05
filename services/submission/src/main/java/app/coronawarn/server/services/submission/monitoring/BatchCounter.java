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

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

/**
 * Batch counter for counting requests for monitoring. Counts up in batches, given batch size. This way, single requests
 * cannot be traced to semantics of the counter by comparing time stamps.
 */
public class BatchCounter {

  private static final String SUBMISSION_CONTROLLER_REQUESTS_COUNTER_NAME = "submissionController.requests";
  private static final String SUBMISSION_CONTROLLER_REQUESTS_COUNTER_DESCRIPTION
      = "Counts requests to the Submission Controller.";

  private final int batchSize;
  private final Counter counter;
  private Double batch = 0.;

  BatchCounter(MeterRegistry meterRegistry, int batchSize, String type) {
    this.batchSize = batchSize;
    counter = Counter.builder(SUBMISSION_CONTROLLER_REQUESTS_COUNTER_NAME)
        .tag("type", type)
        .description(SUBMISSION_CONTROLLER_REQUESTS_COUNTER_DESCRIPTION)
        .register(meterRegistry);
  }

  /**
   * Increments the {@link BatchCounter}. If the batch size is reached, it is provided to monitoring, else, the internal
   * counter is incremented.
   */
  public void increment() {
    if (batch < batchSize) {
      batch++;
    } else {
      counter.increment(batch);
      batch = 1.;
    }
  }
}
