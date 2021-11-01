package app.coronawarn.server.services.distribution.assembly.tracewarnings;

import static app.coronawarn.server.common.persistence.service.utils.checkins.CheckinsDateSpecification.HOUR_SINCE_EPOCH_DERIVATION;

import app.coronawarn.server.common.persistence.domain.CheckInProtectedReports;
import app.coronawarn.server.common.persistence.domain.TraceTimeIntervalWarning;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * New packages with trace warnings shall be assembled and published to CDN, similar to the diagnosis keys. The packages
 * will be hourly and include all TraceTimeIntervalWarnings that have been submitted in the past X amount of days
 * (relative to distribution time), where X is the same app configuration parameter used for diagnosis key (currently
 * 14). An hour Package shall be named after the hour since epoch derived from the submission timestamp of the trace
 * warnings. The resulting package name should be a 6-digit number such as 448188.
 */
@Profile("!demo")
@Component
public class ProdTraceTimeIntervalWarningsPackageBundler extends TraceTimeIntervalWarningsPackageBundler {

  /**
   * Constructs a TraceWarningsPackageBundler based on the specified service configuration.
   */
  public ProdTraceTimeIntervalWarningsPackageBundler(DistributionServiceConfig distributionServiceConfig) {
    super(distributionServiceConfig);
  }

  /**
   * Sets the {@link TraceTimeIntervalWarning}s to package.
   *
   * @param traceTimeIntervalWarnings The {@link TraceTimeIntervalWarning traceTimeIntervalWarnings} contained by this
   *                                  {@link ProdTraceTimeIntervalWarningsPackageBundler}.
   * @param distributionTime          The {@link LocalDateTime} at which the distribution runs.
   * @deprecated because trace time warnings are being replaced by protected reports.
   */
  @Deprecated(since = "2.8")
  public void setTraceTimeIntervalWarnings(
      Collection<TraceTimeIntervalWarning> traceTimeIntervalWarnings,
      LocalDateTime distributionTime) {
    this.distributionTime = distributionTime;
    createTraceWarningsDistributionMap(traceTimeIntervalWarnings);
  }

  /**
   * Sets the {@link CheckInProtectedReports}s to package.
   *
   * @param checkInProtectedReports The {@link CheckInProtectedReports traceTimeIntervalWarnings} contained by this
   *                                {@link ProdTraceTimeIntervalWarningsPackageBundler}.
   * @param distributionTime        The {@link LocalDateTime} at which the distribution runs.
   */
  public void setCheckInProtectedReports(
      Collection<CheckInProtectedReports> checkInProtectedReports,
      LocalDateTime distributionTime) {
    this.distributionTime = distributionTime;
    createCheckInProtectedReportsMap(checkInProtectedReports);
  }

  /**
   * Create distribution map.
   *
   * @param traceTimeIntervalWarnings the base for creating the distribution map.
   * @deprecated because trace time warnings are being replaced by protected reports.
   */
  @Deprecated(since = "2.8")
  protected void createTraceWarningsDistributionMap(
      Collection<TraceTimeIntervalWarning> traceTimeIntervalWarnings) {
    distributableTraceTimeIntervalWarnings.putAll(
        traceTimeIntervalWarnings.stream()
            .filter(this::filterByDistributionTime)
            .collect(Collectors.groupingBy(warning -> (int) warning.getSubmissionTimestamp(), Collectors.toList())));
  }

  protected void createCheckInProtectedReportsMap(
      Collection<CheckInProtectedReports> checkInProtectedReports) {
    distributableCheckInProtectedReports.putAll(
        checkInProtectedReports.stream()
            .filter(this::filterByDistributionTime)
            .collect(Collectors.groupingBy(checkIn -> (int) checkIn.getSubmissionTimestamp(), Collectors.toList())));
  }

  /**
   * Filter by distribution time.
   *
   * @deprecated because trace time warnings are being replaced by protected reports.
   */
  @Deprecated(since = "2.8")
  private boolean filterByDistributionTime(TraceTimeIntervalWarning warning) {
    long oldestDateForCheckins =
        distributionTime.minusDays(daysInThePast).toEpochSecond(ZoneOffset.UTC);
    long latestDateForCheckins = distributionTime.toEpochSecond(ZoneOffset.UTC);
    long warningSubmissionTime = warning.getSubmissionTimestamp();
    return warningSubmissionTime > HOUR_SINCE_EPOCH_DERIVATION.apply(oldestDateForCheckins)
        && warningSubmissionTime < HOUR_SINCE_EPOCH_DERIVATION.apply(latestDateForCheckins);
  }

  private boolean filterByDistributionTime(CheckInProtectedReports checkInProtectedReports) {
    long oldestDateForCheckInProtectedReports =
        distributionTime.minusDays(daysInThePast).toEpochSecond(ZoneOffset.UTC);
    long latestDateForCheckInProtectedReports = distributionTime.toEpochSecond(ZoneOffset.UTC);
    long checkInSubmissionTime = checkInProtectedReports.getSubmissionTimestamp();
    return checkInSubmissionTime > HOUR_SINCE_EPOCH_DERIVATION.apply(oldestDateForCheckInProtectedReports)
        && checkInSubmissionTime < HOUR_SINCE_EPOCH_DERIVATION.apply(latestDateForCheckInProtectedReports);
  }
}
