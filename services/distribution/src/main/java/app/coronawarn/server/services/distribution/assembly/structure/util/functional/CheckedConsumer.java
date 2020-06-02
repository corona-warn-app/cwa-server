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

package app.coronawarn.server.services.distribution.assembly.structure.util.functional;

import java.util.function.Consumer;

/**
 * Convert checked exceptions to unchecked exceptions in Consumers.
 */
@FunctionalInterface
public interface CheckedConsumer<T, E extends Exception> {

  void apply(T t) throws E;

  /**
   * Convert checked exceptions to unchecked exceptions in Consumers.
   */
  static <T> Consumer<T> uncheckedConsumer(CheckedConsumer<T, ? extends Exception> consumer) {
    return input -> {
      try {
        consumer.apply(input);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    };
  }
}
