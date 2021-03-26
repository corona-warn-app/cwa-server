package app.coronawarn.server.services.distribution.assembly.tracewarnings;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;

import app.coronawarn.server.common.persistence.domain.TraceTimeIntervalWarning;
import app.coronawarn.server.services.distribution.assembly.common.DistributionPackagesBundler;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.file.File;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TraceWarningsPackageBundler implements DistributionPackagesBundler<TraceTimeIntervalWarning> {

  /**
   * We only implement this for DE for now, but to be compatible with diagnosis keys, we need this sometimes.
   */
  private static final String COUNTRY = "de";

  private static final Logger logger = LoggerFactory.getLogger(TraceWarningsPackageBundler.class);

  protected final long expiryPolicyMinutes;
  protected final int minNumberOfKeysPerBundle;
  private final int maxNumberOfKeysPerBundle;
  private final String traceWarningPackagesPath;

  /**
   * The hour at which the distribution runs. This field is needed to prevent the run from distributing any keys that
   * have already been submitted but may only be distributed in the future (e.g. because they are not expired yet).
   */
  protected LocalDateTime distributionTime;

  /**
   * A map containing diagnosis keys, mapped by the LocalDateTime on which they may be
   * distributed.
   */
  protected final Map<LocalDateTime, List<TraceTimeIntervalWarning>>
      distributableTraceTimeIntervalWarnings = new HashMap<>();

  /**
   * Constructs a TraceWarningsPackageBundler based on the specified service configuration.
   *
   * @param distributionServiceConfig configuration containing relevant attributes
   */
  public TraceWarningsPackageBundler(DistributionServiceConfig distributionServiceConfig) {
    this.expiryPolicyMinutes = distributionServiceConfig.getExpiryPolicyMinutes();
    this.minNumberOfKeysPerBundle = distributionServiceConfig.getShiftingPolicyThreshold();
    this.maxNumberOfKeysPerBundle = distributionServiceConfig.getMaximumNumberOfKeysPerBundle();
    this.traceWarningPackagesPath = distributionServiceConfig.getApi().getTraceWarningPackagesPath();
  }

  /**
   * Sets the {@link TraceTimeIntervalWarning TraceTimeIntervalWarnings} contained by this
   * {@link TraceWarningsPackageBundler} and the time at which the distribution runs.
   *
   * @param traceTimeIntervalWarnings    The {@link TraceTimeIntervalWarning traceTimeIntervalWarnings}
   *                                     contained by this {@link TraceWarningsPackageBundler}.
   * @param distributionTime The {@link LocalDateTime} at which the distribution runs.
   */
  public void setTraceTimeIntervalWarnings(Collection<TraceTimeIntervalWarning> traceTimeIntervalWarnings,
      LocalDateTime distributionTime) {
    this.distributionTime = distributionTime;
    this.distributableTraceTimeIntervalWarnings.put(distributionTime, new ArrayList<>(traceTimeIntervalWarnings));
  }

  /**
   * Returns the {@link LocalDateTime} at which the distribution runs.
   *
   * @return time the distribution runs in LocalDateTime
   */
  public LocalDateTime getDistributionTime() {
    return this.distributionTime;
  }

  /**
   * Returns a set of all {@link LocalDate dates} on which {@link TraceTimeIntervalWarning traceTimeIntervalWarnings}
   * shall be distributed based on country.
   *
   * @param country Country to collect data for
   * @return set of LocalDate entries for the specified Country
   */
  public Set<LocalDate> getDatesWithDistributablePackages(String country) {
    if (isCountrySupported(country)) {
      return this.distributableTraceTimeIntervalWarnings.keySet().stream()
          .map(LocalDateTime::toLocalDate)
          .filter(date -> numberOfKeysForDateBelowMaximum(date, country))
          .collect(Collectors.toSet());
    }
    return emptySet();
  }

  public boolean numberOfKeysForDateBelowMaximum(LocalDate date, String country) {
    return numberOfKeysBelowMaximum(getDistributionDataForDate(date, country).size(), date);
  }

  /**
   * Returns a map of all {@link LocalDateTime hours} of a specified {@link LocalDate date} and country during which
   * {@link TraceTimeIntervalWarning diagnosis keys} shall be distributed.
   *
   * @param currentDate current time
   * @param country Country to search for data relating to
   * @return Set of LocalDateTime entries for specified Country at specified time
   */
  public Set<LocalDateTime> getHoursWithDistributablePackages(LocalDate currentDate, String country) {
    return this.distributableTraceTimeIntervalWarnings.keySet().stream()
        .filter(dateTime -> dateTime.toLocalDate().equals(currentDate))
        .filter(dateTime -> numberOfKeysForHourBelowMaximum(dateTime))
        .collect(Collectors.toSet());
  }

  private boolean numberOfKeysForHourBelowMaximum(LocalDateTime hour) {
    return numberOfKeysBelowMaximum(getDistributionDataForHour(hour, COUNTRY).size(), hour);
  }

  private boolean numberOfKeysBelowMaximum(int numberOfKeys, Temporal time) {
    if (numberOfKeys > maxNumberOfKeysPerBundle) {
      logger.error("Number of diagnosis keys ({}) for {} exceeds the configured maximum.", numberOfKeys, time);
      return false;
    } else {
      return true;
    }
  }

  /**
   * Returns all diagnosis keys that should be distributed on a specific date for a specific country.
   *
   * @param date date to search
   * @param country country to search
   * @return list of TraceTimeIntervalWarning entires submitted from specified Country on the specified day
   */
  public List<TraceTimeIntervalWarning> getDistributionDataForDate(LocalDate date, String country) {
    if (isCountrySupported(country)) {
      return this.distributableTraceTimeIntervalWarnings.keySet().stream()
          .filter(dateTime -> dateTime.toLocalDate().equals(date))
          .map(dateTime -> getDistributionDataForHour(dateTime, country))
          .flatMap(List::stream)
          .collect(Collectors.toList());
    }
    return emptyList();
  }

  /**
   * Returns all TraceTimeIntervalWarnings that should be distributed in a specific hour for a specific country.
   *
   * @param hour hour to search
   * @param country Country to search
   * @return list of DiagnosisKey entries matching the specified hour and specified Country
   */
  public List<TraceTimeIntervalWarning> getDistributionDataForHour(LocalDateTime hour, String country) {
    if (isCountrySupported(country)) {
      return Optional
          .ofNullable(this.distributableTraceTimeIntervalWarnings.get(hour))
          .orElse(emptyList());
    }
    return emptyList();
  }

  @Override
  public File<WritableOnDisk> createTemporaryExportFile(List<TraceTimeIntervalWarning> data, String country,
      long startTimestamp, long endTimestamp, DistributionServiceConfig distributionServiceConfig) {
    throw new RuntimeException("Not yet implemented for TraceTimeIntervalWarning");
  }

  @Override
  public String getPath() {
    return traceWarningPackagesPath;
  }

  private boolean isCountrySupported(String country) {
    return country.equalsIgnoreCase("DE");
  }
}
