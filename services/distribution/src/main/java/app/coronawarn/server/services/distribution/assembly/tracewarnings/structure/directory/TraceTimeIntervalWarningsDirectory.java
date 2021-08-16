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
import app.coronawarn.server.services.distribution.assembly.tracewarnings.structure.directory.v1.TraceTimeIntervalWarningsCountryV1Directory;
import app.coronawarn.server.services.distribution.assembly.tracewarnings.structure.directory.v2.TraceTimeIntervalWarningsCountryV2Directory;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;

public class TraceTimeIntervalWarningsDirectory extends DirectoryOnDisk {

  private static final String VERSION_V1 = "v1";
  private static final String VERSION_V2 = "v2";
  private final CryptoProvider cryptoProvider;
  private final DistributionServiceConfig distributionServiceConfig;
  private final TraceTimeIntervalWarningsPackageBundler traceWarningsBundler;

  /**
   * Creates an instance of the custom directory that includes the entire {@link TraceTimeIntervalWarning} package
   * structure as per the API specification.
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
    String version = (String) indices.peek();
    if (version.equals(VERSION_V1)) {
      this.addWritable(decorateCountryDirectory(new TraceTimeIntervalWarningsCountryV1Directory(
          traceWarningsBundler, cryptoProvider, distributionServiceConfig)));
    } else if (version.equals(VERSION_V2)) {
      this.addWritable(decorateCountryDirectory(new TraceTimeIntervalWarningsCountryV2Directory(
          traceWarningsBundler, cryptoProvider, distributionServiceConfig)));
    }
    super.prepare(indices);
  }

  private IndexDirectory<String, WritableOnDisk> decorateCountryDirectory(
      IndexDirectoryOnDisk<String> countryDirectory) {
    return new IndexingDecoratorOnDisk<>(countryDirectory,
        distributionServiceConfig.getOutputFileName());
  }
}
