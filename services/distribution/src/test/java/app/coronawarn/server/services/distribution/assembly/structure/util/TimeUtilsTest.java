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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;

class TimeUtilsTest {

  @Test
  void testGetCurrentUtcDateIsLocalDateNowInUtc() {
    assertEquals(LocalDate.now(ZoneOffset.UTC), TimeUtils.getUtcDate());
  }

  @Test
  void testGetUtcHour() {
    assertEquals(LocalDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.HOURS), TimeUtils.getCurrentUtcHour());
  }

  @Test
  void testGetNowIsLocalDateTimeInUtc() {
    final Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
    assertEquals(now, TimeUtils.getNow().truncatedTo(ChronoUnit.SECONDS));
    System.out.println(Instant.now());
  }

}
