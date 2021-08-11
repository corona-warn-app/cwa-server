package app.coronawarn.server.services.distribution.assembly.tracewarnings.structure.directory;

import app.coronawarn.server.common.shared.functional.Formatter;
import app.coronawarn.server.common.shared.functional.IndexFunction;
import app.coronawarn.server.services.distribution.assembly.component.CryptoProvider;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.archive.Archive;
import app.coronawarn.server.services.distribution.assembly.structure.archive.decorator.signing.DistributionArchiveSigningDecorator;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.tracewarnings.TraceTimeIntervalWarningsPackageBundler;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;

/**
 * Abstract class to represent common logic related to the hour directory of check ins.
 */
public abstract class AbstractTraceTimeIntervalWarningsHourDirectory extends IndexDirectoryOnDisk<Integer> {


  protected TraceTimeIntervalWarningsPackageBundler traceWarningsBundler;
  protected CryptoProvider cryptoProvider;
  protected DistributionServiceConfig distributionServiceConfig;

  /**
   * Constructor for abstract trace time interval warning hour directory.
   *
   * @param bundler                   bundle to fetch related check ins.
   * @param cryptoProvider            the crypto provide for signing.
   * @param distributionServiceConfig the config
   * @param indexFunction             the index function that will be applied for sub directories.
   * @param indexFormatter            the formatter.
   */
  protected AbstractTraceTimeIntervalWarningsHourDirectory(
      TraceTimeIntervalWarningsPackageBundler bundler,
      CryptoProvider cryptoProvider,
      DistributionServiceConfig distributionServiceConfig,
      IndexFunction<Integer> indexFunction,
      Formatter<Integer> indexFormatter) {
    super(distributionServiceConfig.getApi().getHourPath(), indexFunction, indexFormatter);
    this.traceWarningsBundler = bundler;
    this.cryptoProvider = cryptoProvider;
    this.distributionServiceConfig = distributionServiceConfig;

  }

  protected Directory<WritableOnDisk> decorateTraceWarningArchives(Archive<WritableOnDisk> archive) {
    return new DistributionArchiveSigningDecorator(archive, cryptoProvider, distributionServiceConfig);
  }
}
