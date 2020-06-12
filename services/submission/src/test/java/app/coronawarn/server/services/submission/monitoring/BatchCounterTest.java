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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.MeterRegistryMock;
import java.util.stream.LongStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class BatchCounterTest {

  private static final String COUNTER_TYPE = "FooCounter";
  private MeterRegistry meterRegistry;
  private Counter meterCounter;

  @BeforeEach
  void setUpCounter() {
    meterCounter = mock(Counter.class);
    meterRegistry = spy(new MeterRegistryMock(meterCounter));
  }

  @ParameterizedTest
  @ValueSource(longs = {1L, 2L, 4L})
  void incrementSubmittedOnceIfBatchSizeReached(long batchSize) {
    BatchCounter batchCounter = new BatchCounter(meterRegistry, batchSize, COUNTER_TYPE);
    LongStream.range(0, batchSize).forEach(__ -> batchCounter.increment());
    verify(meterCounter, times(1)).increment(batchSize);
  }

  @ParameterizedTest
  @ValueSource(longs = {2L, 4L, 7L})
  void doesNotIncrementIfLesserThanBatchSize(long batchSize) {
    BatchCounter batchCounter = new BatchCounter(meterRegistry, batchSize, COUNTER_TYPE);
    LongStream.range(0, batchSize - 1).forEach(__ -> batchCounter.increment());
    verify(meterCounter, never()).increment(batchSize);
  }
}
