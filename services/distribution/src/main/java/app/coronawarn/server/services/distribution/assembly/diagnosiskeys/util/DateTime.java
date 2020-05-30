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

package app.coronawarn.server.services.distribution.assembly.diagnosiskeys.util;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Methods for conversions of time/date data.
 */
public class DateTime {

  private DateTime() {
  }

  /**
   * Returns a set of all {@link LocalDate dates} that are associated with the submission timestamps of the specified
   * {@link DiagnosisKey diagnosis keys}.
   */
  public static Set<LocalDate> getDates(Collection<DiagnosisKey> diagnosisKeys) {
    return diagnosisKeys.stream()
        .map(DiagnosisKey::getSubmissionTimestamp)
        .map(timestamp -> LocalDate.ofEpochDay(timestamp / 24))
        .collect(Collectors.toSet());
  }

  /**
   * Returns a set of all {@link LocalDateTime hours} that are associated with the submission timestamps of the
   * specified {@link DiagnosisKey diagnosis keys} and the specified {@link LocalDate date}.
   */
  public static Set<LocalDateTime> getHours(LocalDate currentDate, Collection<DiagnosisKey> diagnosisKeys) {
    return diagnosisKeys.stream()
        .map(DiagnosisKey::getSubmissionTimestamp)
        .map(DateTime::getLocalDateTimeFromHoursSinceEpoch)
        .filter(currentDateTime -> currentDateTime.toLocalDate().equals(currentDate))
        .collect(Collectors.toSet());
  }

  /**
   * Creates a {@link LocalDateTime} based on the specified epoch timestamp.
   */
  public static LocalDateTime getLocalDateTimeFromHoursSinceEpoch(long timestamp) {
    return LocalDateTime.ofEpochSecond(TimeUnit.HOURS.toSeconds(timestamp), 0, ZoneOffset.UTC);
  }
}
