package app.coronawarn.server.services.distribution.assembly.component;

import static app.coronawarn.server.common.shared.util.TimeUtils.getCurrentUtcHour;

import app.coronawarn.server.common.persistence.domain.CheckInProtectedReports;
import app.coronawarn.server.common.persistence.domain.TraceTimeIntervalWarning;
import app.coronawarn.server.common.persistence.service.TraceTimeIntervalWarningService;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.tracewarnings.TraceTimeIntervalWarningsPackageBundler;
import app.coronawarn.server.services.distribution.assembly.tracewarnings.structure.directory.TraceTimeIntervalWarningsDirectory;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TraceTimeIntervalWarningsStructureProvider {

  private static final Logger logger =
      LoggerFactory.getLogger(TraceTimeIntervalWarningsStructureProvider.class);

  private final TraceTimeIntervalWarningService traceWarningsService;
  private final TraceTimeIntervalWarningsPackageBundler traceWarningsBundler;
  private final CryptoProvider cryptoProvider;
  private final DistributionServiceConfig distributionServiceConfig;

  /**
   * Create an instance.
   */
  public TraceTimeIntervalWarningsStructureProvider(
      TraceTimeIntervalWarningService traceWarningsService,
      TraceTimeIntervalWarningsPackageBundler traceWarningsBundler, CryptoProvider cryptoProvider,
      DistributionServiceConfig distributionServiceConfig) {
    this.traceWarningsService = traceWarningsService;
    this.traceWarningsBundler = traceWarningsBundler;
    this.cryptoProvider = cryptoProvider;
    this.distributionServiceConfig = distributionServiceConfig;
  }

  /**
   * Get directory for {@link TraceTimeIntervalWarning}s from database.
   *
   * @return the directory
   * @deprecated because trace time warnings are not longer supported and replaced by encrypted checkins.
   */
  @Deprecated(since = "2.8")
  public Directory<WritableOnDisk> getTraceWarningsDirectory() {
    logger.debug("Querying trace time interval warnings from the database...");
    Collection<TraceTimeIntervalWarning> traceWarnings =
        traceWarningsService.getTraceTimeIntervalWarnings();
    traceWarningsBundler.setTraceTimeIntervalWarnings(traceWarnings, getCurrentUtcHour());
    return new TraceTimeIntervalWarningsDirectory(traceWarningsBundler, cryptoProvider,
        distributionServiceConfig);
  }


  /**
   * Get directory for {@link TraceTimeIntervalWarning}s from database.
   *
   * @return the directory
   */
  public Directory<WritableOnDisk> getCheckInProtectedReportsDirectory() {
    logger.debug("Querying check in protected reports from the database...");
    Collection<CheckInProtectedReports> checkInProtectedReports =
        traceWarningsService.getCheckInProtectedReports();
    traceWarningsBundler.setCheckInProtectedReports(checkInProtectedReports, getCurrentUtcHour());
    return new TraceTimeIntervalWarningsDirectory(traceWarningsBundler, cryptoProvider,
        distributionServiceConfig);
  }
}
