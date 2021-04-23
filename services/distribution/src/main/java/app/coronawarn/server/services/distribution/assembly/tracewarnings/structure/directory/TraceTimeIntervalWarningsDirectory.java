package app.coronawarn.server.services.distribution.assembly.tracewarnings.structure.directory;

import app.coronawarn.server.common.persistence.domain.TraceTimeIntervalWarning;
import app.coronawarn.server.common.shared.collection.ImmutableStack;
import app.coronawarn.server.services.distribution.assembly.component.CryptoProvider;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.decorator.indexing.IndexingDecoratorOnDisk;
import app.coronawarn.server.services.distribution.assembly.tracewarnings.TraceTimeIntervalWarningsPackageBundler;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;

public class TraceTimeIntervalWarningsDirectory extends DirectoryOnDisk {

  private final CryptoProvider cryptoProvider;
  private final DistributionServiceConfig distributionServiceConfig;
  private final TraceTimeIntervalWarningsPackageBundler traceWarningsBundler;

  /**
   * Creates an instance of the custom directory that includes the entire
   * {@link TraceTimeIntervalWarning} package structure as per the API specification.
   */
  public TraceTimeIntervalWarningsDirectory(
      TraceTimeIntervalWarningsPackageBundler traceWarningsBundler, CryptoProvider cryptoProvider,
      DistributionServiceConfig distributionServiceConfig) {
    super(distributionServiceConfig.getApi().getTraceWarningsPath());
    this.traceWarningsBundler = traceWarningsBundler;
    this.cryptoProvider = cryptoProvider;
    this.distributionServiceConfig = distributionServiceConfig;
  }

  @Override
  public void prepare(ImmutableStack<Object> indices) {
    this.addWritable(decorateCountryDirectory(new TraceTimeIntervalWarningsCountryDirectory(
        traceWarningsBundler, cryptoProvider, distributionServiceConfig)));
    super.prepare(indices);
  }

  private IndexDirectory<String, WritableOnDisk> decorateCountryDirectory(
      IndexDirectoryOnDisk<String> countryDirectory) {
    return new IndexingDecoratorOnDisk<>(countryDirectory,
        distributionServiceConfig.getOutputFileName());
  }
}
