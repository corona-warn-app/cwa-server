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

package app.coronawarn.server.services.distribution.assembly.diagnosiskeys;

import static app.coronawarn.server.services.distribution.common.Helpers.buildDiagnosisKeys;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@EnableConfigurationProperties(value = DistributionServiceConfig.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DistributionServiceConfig.class, ProdDiagnosisKeyBundler.class},
    initializers = ConfigFileApplicationContextInitializer.class)
class ProdDiagnosisKeyBundlerKeyRetrievalTest {

  @Autowired
  DistributionServiceConfig distributionServiceConfig;

  @Autowired
  DiagnosisKeyBundler bundler;

  @Test
  void testGetsAllDiagnosisKeys() {
    List<DiagnosisKey> diagnosisKeys = Stream
        .of(buildDiagnosisKeys(6, 50L, 5), buildDiagnosisKeys(6, 51L, 5), buildDiagnosisKeys(6, 52L, 5))
        .flatMap(List::stream)
        .collect(Collectors.toList());
    bundler.setDiagnosisKeys(diagnosisKeys, LocalDateTime.of(1970, 1, 5, 0, 0));
    assertThat(bundler.getAllDiagnosisKeys()).hasSize(15);
  }

  @Test
  void testGetDatesForEmptyList() {
    bundler.setDiagnosisKeys(emptySet(), LocalDateTime.of(1970, 1, 5, 0, 0));
    assertThat(bundler.getDatesWithDistributableDiagnosisKeys()).isEmpty();
  }

  @Test
  void testGetsDatesWithDistributableDiagnosisKeys() {
    List<DiagnosisKey> diagnosisKeys = Stream
        .of(buildDiagnosisKeys(6, 26L, 5), buildDiagnosisKeys(6, 50L, 1), buildDiagnosisKeys(6, 74L, 5))
        .flatMap(List::stream)
        .collect(Collectors.toList());
    bundler.setDiagnosisKeys(diagnosisKeys, LocalDateTime.of(1970, 1, 5, 0, 0));
    assertThat(bundler.getDatesWithDistributableDiagnosisKeys()).containsAll(List.of(
        LocalDate.of(1970, 1, 2),
        LocalDate.of(1970, 1, 4)
    ));
  }

  @ParameterizedTest
  @MethodSource("createDiagnosisKeysForEpochDay0")
  void testGetDatesForEpochDay0(Collection<DiagnosisKey> diagnosisKeys) {
    bundler.setDiagnosisKeys(diagnosisKeys, LocalDateTime.of(1970, 1, 5, 0, 0));
    var expDates = Set.of(LocalDate.ofEpochDay(2L));
    var actDates = bundler.getDatesWithDistributableDiagnosisKeys();
    assertThat(actDates).isEqualTo(expDates);
  }

  private static Stream<Arguments> createDiagnosisKeysForEpochDay0() {
    return Stream.of(
        buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 3, 0, 0), 5),
        buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 3, 1, 0), 5),
        buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 3, 23, 59, 59), 5)
    ).map(Arguments::of);
  }

  @Test
  void testGetDatesFor2Days() {
    List<DiagnosisKey> diagnosisKeys = Stream
        .of(buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 3, 1, 0), 5),
            buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 4, 1, 0), 5))
        .flatMap(List::stream)
        .collect(Collectors.toList());
    bundler.setDiagnosisKeys(diagnosisKeys, LocalDateTime.of(1970, 1, 5, 0, 0));
    var expDates = Set.of(LocalDate.ofEpochDay(2L), LocalDate.ofEpochDay(3L));
    assertThat(bundler.getDatesWithDistributableDiagnosisKeys()).isEqualTo(expDates);
  }

  @Test
  void testGetHoursForEmptyList() {
    bundler.setDiagnosisKeys(emptySet(), LocalDateTime.of(1970, 1, 5, 0, 0));
    assertThat(bundler.getHoursWithDistributableDiagnosisKeys(LocalDate.of(1970, 1, 3))).isEmpty();
  }

  @Test
  void testGetsHoursWithDistributableDiagnosisKeys() {
    List<DiagnosisKey> diagnosisKeys = Stream
        .of(buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 2, 4, 0), 5),
            buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 2, 5, 0), 1),
            buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 2, 6, 0), 5))
        .flatMap(List::stream)
        .collect(Collectors.toList());
    bundler.setDiagnosisKeys(diagnosisKeys, LocalDateTime.of(1970, 1, 5, 0, 0));
    assertThat(bundler.getHoursWithDistributableDiagnosisKeys(LocalDate.of(1970, 1, 2))).containsAll(List.of(
        LocalDateTime.of(1970, 1, 2, 4, 0, 0),
        LocalDateTime.of(1970, 1, 2, 6, 0, 0)
    ));
  }

  @Test
  void testGetsDiagnosisKeysForDate() {
    List<DiagnosisKey> diagnosisKeys = Stream
        .of(buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 2, 2, 0), 5),
            buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 3, 2, 0), 1),
            buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 4, 2, 0), 5))
        .flatMap(List::stream)
        .collect(Collectors.toList());
    bundler.setDiagnosisKeys(diagnosisKeys, LocalDateTime.of(1970, 1, 5, 20, 0));
    assertThat(bundler.getDiagnosisKeysForDate(LocalDate.of(1970, 1, 1))).hasSize(0);
    assertThat(bundler.getDiagnosisKeysForDate(LocalDate.of(1970, 1, 2))).hasSize(5);
    assertThat(bundler.getDiagnosisKeysForDate(LocalDate.of(1970, 1, 3))).hasSize(0);
    assertThat(bundler.getDiagnosisKeysForDate(LocalDate.of(1970, 1, 4))).hasSize(6);
    assertThat(bundler.getDiagnosisKeysForDate(LocalDate.of(1970, 1, 5))).hasSize(0);
  }

  @Test
  void testEmptyListWhenGettingDiagnosisKeysForDateBeforeEarliestDiagnosisKey() {
    List<DiagnosisKey> diagnosisKeys = buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 2, 4, 0), 5);
    bundler.setDiagnosisKeys(diagnosisKeys, LocalDateTime.of(1970, 1, 5, 0, 0));
    assertThat(bundler.getDiagnosisKeysForDate(LocalDate.of(1970, 1, 1))).hasSize(0);
  }

  @Test
  void testGetsDiagnosisKeysForHour() {
    List<DiagnosisKey> diagnosisKeys = Stream
        .of(buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 2, 4, 0), 5),
            buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 2, 5, 0), 1),
            buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 2, 6, 0), 5))
        .flatMap(List::stream)
        .collect(Collectors.toList());
    bundler.setDiagnosisKeys(diagnosisKeys, LocalDateTime.of(1970, 1, 5, 0, 0));
    assertThat(bundler.getDiagnosisKeysForHour(LocalDateTime.of(1970, 1, 2, 3, 0))).hasSize(0);
    assertThat(bundler.getDiagnosisKeysForHour(LocalDateTime.of(1970, 1, 2, 4, 0))).hasSize(5);
    assertThat(bundler.getDiagnosisKeysForHour(LocalDateTime.of(1970, 1, 2, 5, 0))).hasSize(0);
    assertThat(bundler.getDiagnosisKeysForHour(LocalDateTime.of(1970, 1, 2, 6, 0))).hasSize(6);
    assertThat(bundler.getDiagnosisKeysForHour(LocalDateTime.of(1970, 1, 2, 7, 0))).hasSize(0);
  }

  @Test
  void testEmptyListWhenGettingDiagnosisKeysForHourBeforeEarliestDiagnosisKey() {
    List<DiagnosisKey> diagnosisKeys = buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 2, 4, 0), 5);
    bundler.setDiagnosisKeys(diagnosisKeys, LocalDateTime.of(1970, 1, 5, 0, 0));
    assertThat(bundler.getDiagnosisKeysForHour(LocalDateTime.of(1970, 1, 1, 0, 0, 0))).hasSize(0);
  }
}
