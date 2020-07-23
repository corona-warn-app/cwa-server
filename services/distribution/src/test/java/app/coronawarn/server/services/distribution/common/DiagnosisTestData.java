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

package app.coronawarn.server.services.distribution.common;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A simple container class able to create diagnosis keys test data, given a set of days or day intervals expresed in
 * <code>java.time.*</code> structures.
 */
public final class DiagnosisTestData {

  private List<DiagnosisKey> diagnosisKeys;

  private DiagnosisTestData() {
  }

  public List<DiagnosisKey> getDiagnosisKeys() {
    return diagnosisKeys;
  }

  /**
   * @return An instance that contains diagnosis keys computed for the given interval of days. Each day will have a
   * number of keys equal to the given <code>casesPerDay</code> parameter, recorded, for simplicity, in the last hour of
   * the day.
   */
  public static DiagnosisTestData of(LocalDate fromDay, LocalDate untilDay, int casesPerDay) {

    int numberOfDays = (int) ChronoUnit.DAYS.between(fromDay, untilDay) + 1;
    DiagnosisTestData testData = new DiagnosisTestData();

    testData.diagnosisKeys = computeDiagnosisKeys(testData, fromDay, numberOfDays, casesPerDay);
    return testData;
  }

  private static List<DiagnosisKey> computeDiagnosisKeys(DiagnosisTestData testData, LocalDate fromDay,
      int numberOFDays, int casesPerDay) {
    return IntStream.range(0, numberOFDays)
        .mapToObj(day ->
            randomDiagnosisKeys(fromDay.atStartOfDay().plusDays(day).plusHours(23), casesPerDay)
        ).flatMap(List::stream).collect(Collectors.toList());
  }

  private static List<DiagnosisKey> randomDiagnosisKeys(LocalDateTime submissionTime, int casesPerDay) {
    long timestamp = submissionTime.toEpochSecond(ZoneOffset.UTC) / 3600;
    return IntStream.range(0, casesPerDay)
        .mapToObj(dayCounter ->
            DiagnosisKey.builder().withKeyData(randomKeyData())
                .withRollingStartIntervalNumber(600)
                .withTransmissionRiskLevel(5).withSubmissionTimestamp(timestamp)
                .build())
        .collect(Collectors.toList());
  }

  private static byte[] randomKeyData() {
    byte[] exposureKeys = new byte[16];
    Random random = new Random();
    random.nextBytes(exposureKeys);
    return exposureKeys;
  }
}
