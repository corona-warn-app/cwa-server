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

public abstract class AbstractTraceTimeIntervalWarningsHourDirectory extends IndexDirectoryOnDisk<Integer> {


  protected TraceTimeIntervalWarningsPackageBundler traceWarningsBundler;
  protected CryptoProvider cryptoProvider;
  protected DistributionServiceConfig distributionServiceConfig;

  /**
   * @param bundler
   * @param cryptoProvider
   * @param distributionServiceConfig
   * @param indexFunction
   * @param indexFormatter
   */
  public AbstractTraceTimeIntervalWarningsHourDirectory(
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
