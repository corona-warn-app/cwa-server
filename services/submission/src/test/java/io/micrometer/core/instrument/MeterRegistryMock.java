

package io.micrometer.core.instrument;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

/**
 * Used to get access to the {@link Counter} instance in BatchCounterTest.
 */
public class MeterRegistryMock extends SimpleMeterRegistry {

  final Counter counter;

  public MeterRegistryMock(Counter counter) {
    this.counter = counter;
  }

  @Override
  Counter counter(Meter.Id id) {
    return counter;
  }
}
