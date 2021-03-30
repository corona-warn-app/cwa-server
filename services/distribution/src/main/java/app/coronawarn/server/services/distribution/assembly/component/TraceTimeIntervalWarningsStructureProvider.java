package app.coronawarn.server.services.distribution.assembly.component;

import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import app.coronawarn.server.common.persistence.domain.TraceTimeIntervalWarning;
import app.coronawarn.server.common.persistence.service.TraceTimeIntervalWarningService;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.util.TimeUtils;
import app.coronawarn.server.services.distribution.assembly.tracewarnings.TraceTimeIntervalWarningsPackageBundler;
import app.coronawarn.server.services.distribution.assembly.tracewarnings.structure.directory.TraceTimeWarningsDirectory;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;

public class TraceTimeIntervalWarningsStructureProvider {

  private static final Logger logger =
      LoggerFactory.getLogger(TraceTimeIntervalWarningsStructureProvider.class);

  private final TraceTimeIntervalWarningService traceWarningsService;
  private final TraceTimeIntervalWarningsPackageBundler traceWarningsBundler;
  private final CryptoProvider cryptoProvider;
  private final DistributionServiceConfig distributionServiceConfig;

  public TraceTimeIntervalWarningsStructureProvider(
      TraceTimeIntervalWarningService traceWarningsService,
      TraceTimeIntervalWarningsPackageBundler traceWarningsBundler,
      CryptoProvider cryptoProvider,
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
   */
  public Directory<WritableOnDisk> getTraceWarningsDirectory() {
    logger.debug("Querying trace time interval warnings from the database...");
    Collection<TraceTimeIntervalWarning> traceWarnings =
        traceWarningsService.getTraceTimeIntervalWarning();
    traceWarningsBundler.setTraceTimeIntervalWarnings(traceWarnings, TimeUtils.getCurrentUtcHour());
    return new TraceTimeWarningsDirectory(traceWarningsBundler, cryptoProvider,
        distributionServiceConfig);
  }
}
