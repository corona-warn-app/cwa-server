package app.coronawarn.server.services.distribution.assembly.common;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public interface DistributionPackagesBundler<T> {

  /**
   * The submission timestamp is counted in 1 hour intervals since epoch.
   */
  public static final long ONE_HOUR_INTERVAL_SECONDS = TimeUnit.HOURS.toSeconds(1);

  /**
   * The rolling start interval number is counted in 10 minute intervals since epoch.
   */
  public static final long TEN_MINUTES_INTERVAL_SECONDS = TimeUnit.MINUTES.toSeconds(10);

  LocalDateTime getDistributionTime();

  List<T> getDiagnosisKeysForDate(LocalDate currentDate, String country);

  Set<LocalDate> getDatesWithDistributableDiagnosisKeys(String country);

  Set<LocalDateTime> getHoursWithDistributableDiagnosisKeys(LocalDate peek, String country);

  List<T> getDiagnosisKeysForHour(LocalDateTime currentHour, String country);
}
