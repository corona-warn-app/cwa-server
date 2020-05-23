/*
 * Corona-Warn-App
 *
 * SAP SE and all other contributors /
 * copyright owners license this file to you under the Apache
 * License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package app.coronawarn.server.services.distribution.assembly.diagnosiskeys.util;

import static app.coronawarn.server.services.distribution.common.Helpers.buildDiagnosisKeyForDateTime;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.services.distribution.common.Helpers;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DateTimeTest {

  @Test
  void testGetDatesForEmptyList() {
    assertThat(DateTime.getDates(emptyList())).isEmpty();
  }

  @ParameterizedTest
  @MethodSource("createDiagnosisKeysForEpochDay0")
  void testGetDatesForEpochDay0(DiagnosisKey diagnosisKey) {
    var expDates = Set.of(LocalDate.ofEpochDay(0L));
    var actDates = DateTime.getDates(Set.of(diagnosisKey));

    assertThat(actDates)
        .withFailMessage(
            "Failed for submission timestamp: " + diagnosisKey.getSubmissionTimestamp())
        .isEqualTo(expDates);
  }

  private static Stream<Arguments> createDiagnosisKeysForEpochDay0() {
    return Stream.of(
        buildDiagnosisKeyForDateTime(LocalDateTime.of(1970, 1, 1, 0, 0)),
        buildDiagnosisKeyForDateTime(LocalDateTime.of(1970, 1, 1, 1, 0)),
        buildDiagnosisKeyForDateTime(LocalDateTime.of(1970, 1, 1, 23, 59, 59))
    ).map(Arguments::of);
  }

  @Test
  void testGetDatesFor2Days() {
    var diagnosisKeys = Set.of(
        buildDiagnosisKeyForDateTime(LocalDateTime.of(1970, 1, 1, 1, 0)),
        buildDiagnosisKeyForDateTime(LocalDateTime.of(1970, 1, 2, 1, 0)));
    var expDates = Set.of(LocalDate.ofEpochDay(0L), LocalDate.ofEpochDay(1L));

    assertThat(DateTime.getDates(diagnosisKeys)).isEqualTo(expDates);
  }

  @ParameterizedTest
  @MethodSource("createDiagnosisKeysForEpochDay1And3")
  void testGetHoursReturnsHoursOnlyForSpecifiedDate(Set<DiagnosisKey> diagnosisKeys) {
    var expHours = Set.of(
        LocalDateTime.of(1970, 1, 2, 0, 0),
        LocalDateTime.of(1970, 1, 2, 5, 0));

    var diagnosisKeysIncludingExpHours = new HashSet<>(diagnosisKeys);
    diagnosisKeysIncludingExpHours.addAll(expHours.stream()
        .map(Helpers::buildDiagnosisKeyForDateTime).collect(Collectors.toSet()));

    var actHours = DateTime.getHours(LocalDate.ofEpochDay(1L), diagnosisKeysIncludingExpHours);

    assertThat(actHours).isEqualTo(expHours);
  }

  private static Stream<Arguments> createDiagnosisKeysForEpochDay1And3() {
    return Stream.of(
        emptySet(),
        Set.of(buildDiagnosisKeyForDateTime(LocalDateTime.of(1970, 1, 1, 23, 59))),
        Set.of(
            buildDiagnosisKeyForDateTime(LocalDateTime.of(1970, 1, 1, 23, 59, 59)),
            buildDiagnosisKeyForDateTime(LocalDateTime.of(1970, 1, 3, 0, 0)))
    ).map(Arguments::of);
  }
}
