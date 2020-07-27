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
