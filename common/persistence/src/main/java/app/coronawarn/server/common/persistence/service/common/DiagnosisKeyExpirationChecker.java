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

package app.coronawarn.server.common.persistence.service.common;


import static java.time.ZoneOffset.UTC;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import org.springframework.stereotype.Component;


@Component
public class DiagnosisKeyExpirationChecker {

  public static final long ROLLING_PERIOD_MINUTES_INTERVAL = 10;

  /**
   * The submission timestamp is counted in 1 hour intervals since epoch.
   */
  public static final long ONE_HOUR_INTERVAL_SECONDS = TimeUnit.HOURS.toSeconds(1);

  /**
   * The rolling start interval number is counted in 10 minute intervals since epoch.
   */
  public static final long TEN_MINUTES_INTERVAL_SECONDS = TimeUnit.MINUTES.toSeconds(ROLLING_PERIOD_MINUTES_INTERVAL);

  private static final Map<ChronoUnit, Function<Duration, Long>> TIME_CONVERTERS
       = Map.of(ChronoUnit.SECONDS, (duration) -> duration.toSeconds(),
                ChronoUnit.MINUTES, (duration) -> duration.toMinutes(),
                ChronoUnit.HOURS, (duration) -> duration.toHours());

  /**
   * Returns true if the given diagnosis key has expired conforming to both its rolling
   * period and the given policy.
   */
  public boolean canShareKeyAtTime(DiagnosisKey key, ExpirationPolicy policy, LocalDateTime timeToShare) {
    LocalDateTime earliestTimeToShare = getEarliestTimeForSharingKey(key, policy);
    return timeToShare.isAfter(earliestTimeToShare) || timeToShare.isEqual(earliestTimeToShare);
  }

  /**
   * Calculates the earliest point in time at which the specified {@link DiagnosisKey} can be shared with 3rd parties,
   * while respecting the expiry policy and the submission timestamp. According to DPP rules, before keys are allowed
   * to be shared, they must be expired for a configured amount of time.
   *
   * @return {@link LocalDateTime} at which the specified {@link DiagnosisKey} can be shared.
   */
  public LocalDateTime getEarliestTimeForSharingKey(DiagnosisKey diagnosisKey, ExpirationPolicy policy) {
    LocalDateTime submissionDateTime = getSubmissionDateTime(diagnosisKey);
    LocalDateTime expiryDateTime = getRollingPeriodExpiryTime(diagnosisKey);
    long timeBetweenExpiryAndSubmission = TIME_CONVERTERS.get(policy.getTimeUnit())
        .apply(Duration.between(expiryDateTime, submissionDateTime));
    if (timeBetweenExpiryAndSubmission <= policy.getExpirationTime()) {
      // truncatedTo floors the value, so we need to add an hour to the DISTRIBUTION_PADDING to compensate that.
      return expiryDateTime.plusMinutes(policy.getExpirationTime() + 60).truncatedTo(ChronoUnit.HOURS);
    } else {
      return submissionDateTime;
    }
  }

  /**
   * Returns the end of the rolling time window that a {@link DiagnosisKey} was active for as a {@link LocalDateTime}.
   */
  private LocalDateTime getRollingPeriodExpiryTime(DiagnosisKey diagnosisKey) {
    return LocalDateTime
        .ofEpochSecond(diagnosisKey.getRollingStartIntervalNumber() * TEN_MINUTES_INTERVAL_SECONDS, 0, UTC)
        .plusMinutes(diagnosisKey.getRollingPeriod() * ROLLING_PERIOD_MINUTES_INTERVAL);
  }

  /**
   * Returns the submission timestamp of a {@link DiagnosisKey} as a {@link LocalDateTime}.
   */
  private LocalDateTime getSubmissionDateTime(DiagnosisKey diagnosisKey) {
    return LocalDateTime.ofEpochSecond(diagnosisKey.getSubmissionTimestamp() * ONE_HOUR_INTERVAL_SECONDS, 0, UTC);
  }
}
