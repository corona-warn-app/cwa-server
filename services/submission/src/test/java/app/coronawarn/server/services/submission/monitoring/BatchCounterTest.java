

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
    LongStream.range(0, batchSize).forEach(ignoredValue -> batchCounter.increment());
    verify(meterCounter, times(1)).increment(batchSize);
  }

  @ParameterizedTest
  @ValueSource(longs = {2L, 4L, 7L})
  void doesNotIncrementIfLesserThanBatchSize(long batchSize) {
    BatchCounter batchCounter = new BatchCounter(meterRegistry, batchSize, COUNTER_TYPE);
    LongStream.range(0, batchSize - 1).forEach(ignoredValue -> batchCounter.increment());
    verify(meterCounter, never()).increment(batchSize);
  }
}
