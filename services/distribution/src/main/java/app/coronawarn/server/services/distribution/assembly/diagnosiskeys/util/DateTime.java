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

  /**
   * Returns a set of all {@link LocalDate dates} that are associated with the submission timestamps
   * of the specified {@link DiagnosisKey diagnosis keys}.
   */
  public static Set<LocalDate> getDates(Collection<DiagnosisKey> diagnosisKeys) {
    return diagnosisKeys.stream()
        .map(DiagnosisKey::getSubmissionTimestamp)
        .map(timestamp -> LocalDate.ofEpochDay(timestamp / 24))
        .collect(Collectors.toSet());
  }


  /**
   * Returns a set of all {@link LocalDateTime hours} that are associated with the submission
   * timestamps of the specified {@link DiagnosisKey diagnosis keys} and the specified {@link
   * LocalDate date}.
   */
  public static Set<LocalDateTime> getHours(LocalDate currentDate,
      Collection<DiagnosisKey> diagnosisKeys) {
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
