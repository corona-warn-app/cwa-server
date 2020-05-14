package app.coronawarn.server.services.distribution.diagnosiskeys.structure;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.services.distribution.diagnosiskeys.util.DateTime;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class DateTimeTest {

  @Test
  public void testGetDatesForEmptyList() {
    Assertions.assertEquals(emptySet(), DateTime.getDates(emptyList()));
  }

  @ParameterizedTest
  @MethodSource("createDiagnosisKeysForEpochDay0")
  public void testGetDatesForEpochDay0(DiagnosisKey diagnosisKey) {
    var expDates = Set.of(LocalDate.ofEpochDay(0L));
    var actDates = DateTime.getDates(Set.of(diagnosisKey));

    Assertions.assertEquals(expDates, actDates,
        "Failed for submission timestamp: " + diagnosisKey.getSubmissionTimestamp());
  }

  private static Stream<Arguments> createDiagnosisKeysForEpochDay0() {
    return Stream.of(
        buildDiagnosisKeyForDateTime(LocalDateTime.of(1970, 1, 1, 0, 0)),
        buildDiagnosisKeyForDateTime(LocalDateTime.of(1970, 1, 1, 1, 0)),
        buildDiagnosisKeyForDateTime(LocalDateTime.of(1970, 1, 1, 23, 59, 59))
    ).map(Arguments::of);
  }

  @Test
  public void testGetDatesFor2Days() {
    var diagnosisKeys = Set.of(
        buildDiagnosisKeyForDateTime(LocalDateTime.of(1970, 1, 1, 1, 0)),
        buildDiagnosisKeyForDateTime(LocalDateTime.of(1970, 1, 2, 1, 0)));
    var expDates = Set.of(LocalDate.ofEpochDay(0L), LocalDate.ofEpochDay(1L));

    Assertions.assertEquals(expDates, DateTime.getDates(diagnosisKeys));
  }

  private static DiagnosisKey buildDiagnosisKeyForDateTime(LocalDateTime dateTime) {
    return DiagnosisKey.builder().
        withRollingStartNumber(0L)
        .withRollingPeriod(1L)
        .withTransmissionRiskLevel(2)
        .withSubmissionTimestamp(dateTime.toEpochSecond(ZoneOffset.UTC) / 3600).build();
  }

  @ParameterizedTest
  @MethodSource("createDiagnosisKeysForEpochDay1And3")
  public void testGetHoursReturnsHoursOnlyForSpecifiedDate(Set<DiagnosisKey> diagnosisKeys) {
    var expHours = Set.of(
        LocalDateTime.of(1970, 1, 2, 0, 0),
        LocalDateTime.of(1970, 1, 2, 5, 0));

    diagnosisKeys.addAll(expHours.stream()
        .map(DateTimeTest::buildDiagnosisKeyForDateTime).collect(Collectors.toSet()));

    var actHours = DateTime.getHours(LocalDate.ofEpochDay(1L), diagnosisKeys);

    Assertions.assertEquals(expHours, actHours);
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
