

package app.coronawarn.server.services.distribution.assembly.component;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.domain.TraceTimeIntervalWarning;
import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.common.persistence.service.TraceTimeIntervalWarningService;
import app.coronawarn.server.services.distribution.assembly.diagnosiskeys.DiagnosisKeyBundler;
import app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.directory.DistributionDirectory;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.util.TimeUtils;
import app.coronawarn.server.services.distribution.assembly.tracewarnings.TraceWarningsPackageBundler;
import app.coronawarn.server.services.distribution.assembly.transformation.EnfParameterAdapter;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Retrieves stored diagnosis keys and builds a {@link DistributionDirectory} with them.
 */
@Component
public class DistributionStructureProvider {

  private static final Logger logger = LoggerFactory
      .getLogger(DistributionStructureProvider.class);

  private final DiagnosisKeyBundler diagnosisKeyBundler;
  private final DiagnosisKeyService diagnosisKeyService;
  private final TraceWarningsPackageBundler traceWarningPackageBundler;
  private final TraceTimeIntervalWarningService traceTimeIntervalWarningService;
  private final CryptoProvider cryptoProvider;
  private final DistributionServiceConfig distributionServiceConfig;
  private final EnfParameterAdapter enfParameterEncoder;

  /**
   * Creates a new DiagnosisKeysStructureProvider.
   */
  DistributionStructureProvider(DiagnosisKeyService diagnosisKeyService,
      TraceTimeIntervalWarningService traceTimeIntervalWarningService,
      CryptoProvider cryptoProvider,
      DistributionServiceConfig distributionServiceConfig, DiagnosisKeyBundler diagnosisKeyBundler,
      TraceWarningsPackageBundler traceWarningPackageBundler,
      EnfParameterAdapter enfParameterEncoder) {
    this.diagnosisKeyService = diagnosisKeyService;
    this.traceTimeIntervalWarningService = traceTimeIntervalWarningService;
    this.cryptoProvider = cryptoProvider;
    this.distributionServiceConfig = distributionServiceConfig;
    this.diagnosisKeyBundler = diagnosisKeyBundler;
    this.traceWarningPackageBundler = traceWarningPackageBundler;
    this.enfParameterEncoder = enfParameterEncoder;
  }

  /**
   * Get directory for diagnosis keys from database.
   *
   * @return the directory
   */
  public Directory<WritableOnDisk> getDiagnosisKeys() {
    logger.debug("Querying diagnosis keys from the database...");
    Collection<DiagnosisKey> diagnosisKeys = diagnosisKeyService.getDiagnosisKeys();
    diagnosisKeyBundler.setDiagnosisKeys(enfParameterEncoder.adaptKeys(diagnosisKeys), TimeUtils.getCurrentUtcHour());
    return new DistributionDirectory(diagnosisKeyBundler, cryptoProvider, distributionServiceConfig);
  }

  /**
   * Get directory for trace warnings from database.
   *
   * @return the directory
   */
  public Directory<WritableOnDisk> getTraceWarnings() {
    logger.debug("Querying trace warnings from the database...");
    Collection<TraceTimeIntervalWarning> traceTimeIntervalWarnings =
        traceTimeIntervalWarningService.getTraceTimeIntervalWarning();
    traceWarningPackageBundler.setTraceTimeIntervalWarnings(traceTimeIntervalWarnings, TimeUtils.getCurrentUtcHour());
    return new DistributionDirectory(traceWarningPackageBundler, cryptoProvider, distributionServiceConfig);
  }
}
