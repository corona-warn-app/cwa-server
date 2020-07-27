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

package app.coronawarn.server.services.distribution.assembly.structure.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

public class TimeUtils {

  private static Instant now;

  private TimeUtils() {
  }

  /**
   * Returns the UTC date and time at the beginning of the current hour.
   */
  public static LocalDateTime getCurrentUtcHour() {
    return LocalDateTime.ofInstant(getNow().truncatedTo(ChronoUnit.HOURS), ZoneOffset.UTC);
  }

  /**
   * Returns the UTC date.
   */
  public static LocalDate getUtcDate() {
    return getCurrentUtcHour().toLocalDate();
  }

  /**
   * Returns the UTC {@link Instant} time or creates a new instance if called the first time.
   */
  public static Instant getNow() {
    if (now == null) {
      now = Instant.now();
    }
    return now;
  }

  /**
   * Injects UTC instant time value.
   *
   * @param instant an {@link Instant} object.
   */
  public static void setNow(Instant instant) {
    now = instant;
  }
}
