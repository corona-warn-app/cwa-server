package app.coronawarn.server.services.distribution.assembly.tracewarnings;


import app.coronawarn.server.common.persistence.domain.CheckInProtectedReports;
import app.coronawarn.server.common.persistence.domain.TraceTimeIntervalWarning;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
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
@Profile("demo")
@Component
public class DemoTraceTimeIntervalWarningsPackageBundler extends TraceTimeIntervalWarningsPackageBundler {

  /**
   * Constructs a TraceWarningsPackageBundler based on the specified service configuration.
   *
   * @param distributionServiceConfig configuration containing relevant attributes
   */
  public DemoTraceTimeIntervalWarningsPackageBundler(
      DistributionServiceConfig distributionServiceConfig) {
    super(distributionServiceConfig);
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
            .collect(Collectors.groupingBy(warning -> (int) warning.getSubmissionTimestamp(), Collectors.toList())));
  }

  @Override
  protected void createCheckInProtectedReportsMap(Collection<CheckInProtectedReports> checkInProtectedReports) {
    distributableCheckInProtectedReports.putAll(
        checkInProtectedReports.stream()
            .collect(Collectors
                .groupingBy(checkInProtectedReport ->
                    (int) checkInProtectedReport.getSubmissionTimestamp(), Collectors.toList())));
  }
}
