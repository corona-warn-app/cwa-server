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

package app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.directory;

import static app.coronawarn.server.services.distribution.assembly.diagnosiskeys.util.DateTime.ONE_HOUR_INTERVAL_SECONDS;
import static app.coronawarn.server.services.distribution.assembly.diagnosiskeys.util.DateTime.TEN_MINUTES_INTERVAL_SECONDS;
import static java.time.ZoneOffset.UTC;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class DistributionDateTimeCalculator {

  /**
   * Minimum time in minutes after key expiration after which it can be distributed.
   */
  public static final long DISTRIBUTION_PADDING = 120L;

  private DistributionDateTimeCalculator() {
  }

  /**
   * Calculates the earliest point in time at which the specified {@link DiagnosisKey} can be distributed. Before keys
   * are allowed to be distributed, they must be expired for a configured amount of time.
   *
   * @return {@link LocalDateTime} at which the specified {@link DiagnosisKey} can be distributed.
   */
  public static LocalDateTime getDistributionDateTime(DiagnosisKey diagnosisKey) {
    LocalDateTime submissionDateTime = LocalDateTime
        .ofEpochSecond(diagnosisKey.getSubmissionTimestamp() * ONE_HOUR_INTERVAL_SECONDS, 0, UTC);
    LocalDateTime keyExpiryDateTime = LocalDateTime
        .ofEpochSecond(diagnosisKey.getRollingStartIntervalNumber() * TEN_MINUTES_INTERVAL_SECONDS, 0, UTC)
        .plusMinutes(diagnosisKey.getRollingPeriod() * 10L);

    if (Duration.between(keyExpiryDateTime, submissionDateTime).toMinutes() <= DISTRIBUTION_PADDING) {
      // truncatedTo floors the value, so we need to add an hour to the DISTRIBUTION_PADDING to compensate that.
      return keyExpiryDateTime.plusMinutes(DISTRIBUTION_PADDING + 60).truncatedTo(ChronoUnit.HOURS);
    }

    return submissionDateTime;
  }
}
