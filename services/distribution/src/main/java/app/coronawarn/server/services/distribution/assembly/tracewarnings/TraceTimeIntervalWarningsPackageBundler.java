package app.coronawarn.server.services.distribution.assembly.tracewarnings;


import static java.util.Collections.emptySet;

import app.coronawarn.server.common.persistence.domain.TraceTimeIntervalWarning;
import app.coronawarn.server.common.persistence.utils.CheckinsDateSpecification;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TraceTimeIntervalWarningsPackageBundler {

  private static final Logger logger =
      LoggerFactory.getLogger(TraceTimeIntervalWarningsPackageBundler.class);


  private final List<String> supportedCountries;

  /**
   * The hour at which the distribution runs.
   */
  private LocalDateTime distributionTime;

  /**
   * A map containing checkin warnings, mapped by the 10-minute interval since epoch.
   * This is the basis on which they will be distributed to the CDN.
   *
   * @see CheckinsDateSpecification#TEN_MINUTE_INTERVAL_DERIVATION
   */
  protected final Map<Integer, List<TraceTimeIntervalWarning>> distributableTraceTimeIntervalWarnings =
      new HashMap<>();


  /**
   * Constructs a TraceWarningsPackageBundler based on the specified service configuration.
   *
   * @param distributionServiceConfig configuration containing relevant attributes
   */
  public TraceTimeIntervalWarningsPackageBundler(
      DistributionServiceConfig distributionServiceConfig) {
    this.supportedCountries = List.of(distributionServiceConfig.getSupportedCountries());
  }

  /**
   * Sets the {@link TraceTimeIntervalWarning TraceTimeIntervalWarnings} contained by this
   * {@link TraceTimeIntervalWarningsPackageBundler} and the time at which the distribution runs.
   *
   * @param traceTimeIntervalWarnings The {@link TraceTimeIntervalWarning traceTimeIntervalWarnings}
   *        contained by this {@link TraceTimeIntervalWarningsPackageBundler}.
   * @param distributionTime The {@link LocalDateTime} at which the distribution runs.
   */
  public void setTraceTimeIntervalWarnings(
      Collection<TraceTimeIntervalWarning> traceTimeIntervalWarnings,
      LocalDateTime distributionTime) {
    this.distributionTime = distributionTime;
    createTraceWarningsDistributionMap(traceTimeIntervalWarnings);
  }

  public Set<Integer> getHoursSinceEpochWithDistributableWarnings(String country) {
    if (isCountrySupported(country)) {
      return this.distributableTraceTimeIntervalWarnings.keySet();
    }
    return emptySet();
  }

  public List<TraceTimeIntervalWarning> getTraceTimeWarningsForHour(Integer currentHourSinceEpoch) {
    return distributableTraceTimeIntervalWarnings.get(currentHourSinceEpoch);
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
    distributableTraceTimeIntervalWarnings.putAll(traceTimeIntervalWarnings.stream()
        .collect(Collectors.groupingBy(this::extractHourInterval, Collectors.toList())));
  }

  private Integer extractHourInterval(TraceTimeIntervalWarning warning) {
    // TODO Check if this is the correct timestamp to use
    return CheckinsDateSpecification.HOUR_SINCE_EPOCH_DERIVATION
        .apply(Long.valueOf(warning.getEndIntervalNumber() * 600));
  }
}