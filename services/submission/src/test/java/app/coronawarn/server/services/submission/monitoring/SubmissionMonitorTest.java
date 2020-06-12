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

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.coronawarn.server.services.submission.config.SubmissionServiceConfig;
import app.coronawarn.server.services.submission.controller.FakeDelayManager;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.MeterRegistryMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SubmissionMonitorTest {

  private final Counter meterCounter = mock(Counter.class);
  private final SubmissionServiceConfig submissionServiceConfig = mock(SubmissionServiceConfig.class);
  private MeterRegistry meterRegistry;
  private SubmissionMonitor submissionMonitor;

  @BeforeEach
  void setup() {
    reset(meterCounter);
    reset(submissionServiceConfig);
    meterRegistry = spy(new MeterRegistryMock(meterCounter));
    when(submissionServiceConfig.getMonitoringBatchSize()).thenReturn(1L);
    submissionMonitor = new SubmissionMonitor(meterRegistry, submissionServiceConfig, mock(FakeDelayManager.class));
  }

  @Test
  void incrementFakeRequestCounterIncrementsEnclosedCounter() {
    submissionMonitor.incrementFakeRequestCounter();
    verify(meterCounter, times(1)).increment(anyDouble());
  }

  @Test
  void incrementInvalidTanRequestCounterIncrementsEnclosedCounter() {
    submissionMonitor.incrementInvalidTanRequestCounter();
    verify(meterCounter, times(1)).increment(anyDouble());
  }

  @Test
  void incrementRealRequestCounterIncrementsEnclosedCounter() {
    submissionMonitor.incrementRealRequestCounter();
    verify(meterCounter, times(1)).increment(anyDouble());
  }

  @Test
  void incrementRequestCounterIncrementsEnclosedCounter() {
    submissionMonitor.incrementRequestCounter();
    verify(meterCounter, times(1)).increment(anyDouble());
  }
}
