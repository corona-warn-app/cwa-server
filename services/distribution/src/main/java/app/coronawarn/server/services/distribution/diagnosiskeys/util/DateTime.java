package app.coronawarn.server.services.distribution.diagnosiskeys.util;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DateTime {

  /**
   * Calculates the number of days covered by {@code hours} (rounded up) Examples: {@code
   * (assert(getNumberOfDays(23) == 1); assert(getNumberOfDays(24) == 1); assert(getNumberOfDays(25)
   * == 2);}.
   */
  public static int getNumberOfDays(int hours) {
    return Maths.ceilDiv(hours, 24);
  }


  /**
   * Creates a list of all {@link LocalDate LocalDates} between {@code startDate} and {@code
   * numDays} later.
   */
  public static Set<LocalDate> getDates(Collection<DiagnosisKey> diagnosisKeys) {
    // TODO Doc
    return diagnosisKeys.stream()
        .map(DiagnosisKey::getSubmissionTimestamp)
        .map(timestamp -> LocalDate.ofEpochDay(timestamp / 24))
        .collect(Collectors.toSet());
  }


  /**
   * Creates a list of all {@link LocalDateTime LocalDateTimes} between {@code startDate} and {@code
   * currentDate} (at 00:00 UTC) plus {@code totalHours % 24}.
   */
  public static Set<LocalDateTime> getHours(LocalDate currentDate,
      Collection<DiagnosisKey> diagnosisKeys) {
    // TODO Doc
    return diagnosisKeys.stream()
        .map(DiagnosisKey::getSubmissionTimestamp)
        .map(DateTime::getLocalDateTimeFromHoursSinceEpoch)
        .filter(currentDateTime -> currentDateTime.toLocalDate().equals(currentDate))
        .collect(Collectors.toSet());
  }

  public static LocalDateTime getLocalDateTimeFromHoursSinceEpoch(long timestamp) {
      return LocalDateTime.ofEpochSecond(TimeUnit.HOURS.toSeconds(timestamp), 0, ZoneOffset.UTC);
  }
}
