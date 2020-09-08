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

import static app.coronawarn.server.common.persistence.service.DiagnosisKeyServiceTestHelper.getKeySubmittedHoursAfterMidnightExpiration;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;

class DiagnosisKeyExpirationCheckerTest {

  private final KeySharingPoliciesChecker sharingPoliciesChecker = new KeySharingPoliciesChecker();


  @ParameterizedTest
  @MethodSource("notExpiredKeysDataset")
  void shouldComputeThatKeyIsNotExpired(DiagnosisKey key, ExpirationPolicy expirationPolicy, LocalDateTime shareTime) {
    assertFalse(sharingPoliciesChecker.canShareKeyAtTime(key, expirationPolicy, shareTime));
  }

  @ParameterizedTest
  @MethodSource("expiredKeysDataset")
  void shouldComputeThatKeyIsExpired(DiagnosisKey key, ExpirationPolicy expirationPolicy, LocalDateTime shareTime) {
    assertTrue(sharingPoliciesChecker.canShareKeyAtTime(key, expirationPolicy, shareTime));
  }

  private static Stream<Arguments> expiredKeysDataset() {
    LocalDateTime midnight = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT);
    return Stream.of(
        Arguments.of(getKeySubmittedHoursAfterMidnightExpiration(1), ExpirationPolicy.of(60, ChronoUnit.MINUTES), midnight.plusHours(2)),
        Arguments.of(getKeySubmittedHoursAfterMidnightExpiration(2), ExpirationPolicy.of(60, ChronoUnit.MINUTES), midnight.plusHours(3)),
        Arguments.of(getKeySubmittedHoursAfterMidnightExpiration(2), ExpirationPolicy.of(120, ChronoUnit.MINUTES), midnight.plusHours(3)),
        Arguments.of(getKeySubmittedHoursAfterMidnightExpiration(4), ExpirationPolicy.of(120, ChronoUnit.MINUTES), midnight.plusHours(4))
    );
  }

  private static Stream<Arguments> notExpiredKeysDataset() {
    LocalDateTime midnight = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT);
    return Stream.of(
        Arguments.of(getKeySubmittedHoursAfterMidnightExpiration(1), ExpirationPolicy.of(120, ChronoUnit.MINUTES), midnight.plusHours(1).plusMinutes(30)),
        Arguments.of(getKeySubmittedHoursAfterMidnightExpiration(2), ExpirationPolicy.of(180, ChronoUnit.MINUTES), midnight.plusHours(2).plusMinutes(30)),
        Arguments.of(getKeySubmittedHoursAfterMidnightExpiration(3), ExpirationPolicy.of(240, ChronoUnit.MINUTES), midnight.plusHours(3).plusMinutes(30))
    );
  }
}
