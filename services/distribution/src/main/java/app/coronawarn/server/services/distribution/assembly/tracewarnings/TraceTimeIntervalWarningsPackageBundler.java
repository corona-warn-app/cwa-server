package app.coronawarn.server.services.distribution.assembly.tracewarnings;


import static app.coronawarn.server.common.persistence.utils.CheckinsDateSpecification.HOUR_SINCE_EPOCH_DERIVATION;

import app.coronawarn.server.common.persistence.domain.TraceTimeIntervalWarning;
import app.coronawarn.server.common.persistence.utils.CheckinsDateSpecification;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * New packages with trace warnings shall be assembled and published to CDN, similar to the diagnosis keys. The packages
 * will be hourly and include all TraceTimeIntervalWarnings that have been submitted in the past X ammount of days
 * (relative to distribution time), where X is the same app configuration paramter used for diagnosis key (currently
 * 14). An hour Package shall be named after the hour since epoch derived from the submission timestamp of the trace
 * warnings. The resulting package name should be a 6-digit number such as 448188.
 */
public class TraceTimeIntervalWarningsPackageBundler {

  private static final Logger logger =
      LoggerFactory.getLogger(TraceTimeIntervalWarningsPackageBundler.class);


  private final List<String> supportedCountries;
  /**
   * The hour at which the distribution runs.
   */
  private LocalDateTime distributionTime;
  /**
   * Data will be distributed for X days back starting from distribution time, where X is the variable below.
   */
  private Integer daysInThePast;
  /**
   * A map containing trace warnings, mapped by hours since epoch computed from their submission timestamp. This is the
   * basis on which they will be distributed to the CDN.
   *
   * @see CheckinsDateSpecification#HOUR_SINCE_EPOCH_DERIVATION
   */
  private final Map<Integer, List<TraceTimeIntervalWarning>> distributableTraceTimeIntervalWarnings =
      new HashMap<>();


  /**
   * Constructs a TraceWarningsPackageBundler based on the specified service configuration.
   *
   * @param distributionServiceConfig configuration containing relevant attributes
   */
  public TraceTimeIntervalWarningsPackageBundler(
      DistributionServiceConfig distributionServiceConfig) {
    this.supportedCountries = List.of(distributionServiceConfig.getSupportedCountries());
    daysInThePast = distributionServiceConfig.getRetentionDays();
  }

  /**
   * Sets the {@link TraceTimeIntervalWarning}s to package.
   *
   * @param traceTimeIntervalWarnings The {@link TraceTimeIntervalWarning traceTimeIntervalWarnings} contained by this
   *                                  {@link TraceTimeIntervalWarningsPackageBundler}.
   * @param distributionTime          The {@link LocalDateTime} at which the distribution runs.
   */
  public void setTraceTimeIntervalWarnings(
      Collection<TraceTimeIntervalWarning> traceTimeIntervalWarnings,
      LocalDateTime distributionTime) {
    this.distributionTime = distributionTime;
    createTraceWarningsDistributionMap(traceTimeIntervalWarnings);
  }

  /**
   * Returns all available hourly (since epoch) data for distribution.
   */
  public Set<Integer> getHoursForDistributableWarnings(String country) {
    if (isCountrySupported(country)) {
      final Optional<Integer> oldestOptional = getOldestHourWithDistributableWarnings(country);
      final Optional<Integer> latestOptional = getLatestHourWithDistributableWarnings(country);
      if (oldestOptional.isPresent() && latestOptional.isPresent()) {
        return IntStream.range(oldestOptional.get(), latestOptional.get() + 1).boxed()
            .collect(Collectors.toSet());
      }
      return Collections.emptySet();
    }
    return Collections.emptySet();
  }

  /**
   * Fetch the oldest hour with distributable trace time interval warnings that ar present in the distribution map.
   *
   * @param country support country.
   * @return optional containing the value of the min hour.
   */
  public Optional<Integer> getOldestHourWithDistributableWarnings(String country) {
    if (isCountrySupported(country)) {
      return this.distributableTraceTimeIntervalWarnings.keySet().stream().min(Integer::compareTo);
    }
    return Optional.empty();
  }

  /**
   * Fetch the latest hour with distributable trace time interval warnings that ar present in the distribution map.
   *
   * @param country support country.
   * @return optional containing the value of the max hour.
   */
  public Optional<Integer> getLatestHourWithDistributableWarnings(String country) {
    if (isCountrySupported(country)) {
      return this.distributableTraceTimeIntervalWarnings.keySet().stream().max(Integer::compareTo);
    }
    return Optional.empty();
  }

  /**
   * Returns the trace time warnings ready to be distributed for the given hour since epoch.
   */
  public List<TraceTimeIntervalWarning> getTraceTimeWarningsForHour(Integer currentHourSinceEpoch) {
    return distributableTraceTimeIntervalWarnings.getOrDefault(currentHourSinceEpoch, Collections.emptyList());
  }

  private boolean isCountrySupported(String country) {
    if (!supportedCountries.contains(country)) {
      logger.warn("The country {} received is not included in the list of supported countries", country);
      return false;
    }
    return true;
  }

  private void createTraceWarningsDistributionMap(
      Collection<TraceTimeIntervalWarning> traceTimeIntervalWarnings) {
    distributableTraceTimeIntervalWarnings.putAll(
        traceTimeIntervalWarnings.stream()
            .filter(this::filterByDistributionTime)
            .collect(Collectors.groupingBy(warning -> (int) warning.getSubmissionTimestamp(), Collectors.toList())));
  }

  private boolean filterByDistributionTime(TraceTimeIntervalWarning warning) {
    long oldestDateForCheckins =
        distributionTime.minusDays(daysInThePast).toEpochSecond(ZoneOffset.UTC);
    long latestDateForCheckins = distributionTime.toEpochSecond(ZoneOffset.UTC);
    long warningSubmissionTime = warning.getSubmissionTimestamp();
    return warningSubmissionTime > HOUR_SINCE_EPOCH_DERIVATION.apply(oldestDateForCheckins)
        && warningSubmissionTime <= HOUR_SINCE_EPOCH_DERIVATION.apply(latestDateForCheckins);
  }
}
