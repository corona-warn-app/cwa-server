package app.coronawarn.server.services.distribution.assembly.tracewarnings.structure.directory;

import app.coronawarn.server.common.persistence.domain.TraceTimeIntervalWarning;
import app.coronawarn.server.common.shared.collection.ImmutableStack;
import app.coronawarn.server.services.distribution.assembly.component.CryptoProvider;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.tracewarnings.TraceTimeIntervalWarningsPackageBundler;
import app.coronawarn.server.services.distribution.assembly.tracewarnings.structure.directory.decorator.HourIntervalIndexingDecorator;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.util.Optional;
import java.util.Set;

public class TraceTimeIntervalWarningsCountryDirectory extends IndexDirectoryOnDisk<String> {

  private final String version;
  private TraceTimeIntervalWarningsPackageBundler traceWarningsBundler;
  private CryptoProvider cryptoProvider;
  private DistributionServiceConfig distributionServiceConfig;

  /**
   * Creates an instance of the custom directory that includes the entire {@link TraceTimeIntervalWarning} package
   * structure for a country as per the API specification.
   */
  public TraceTimeIntervalWarningsCountryDirectory(
      TraceTimeIntervalWarningsPackageBundler traceWarningsBundler, CryptoProvider cryptoProvider,
      DistributionServiceConfig distributionServiceConfig, String version) {
    super(distributionServiceConfig.getApi().getCountryPath(),
        ignoredValue -> Set.of(distributionServiceConfig.getApi().getOriginCountry()),
        Object::toString);
    this.traceWarningsBundler = traceWarningsBundler;
    this.cryptoProvider = cryptoProvider;
    this.distributionServiceConfig = distributionServiceConfig;
    this.version = version;
  }

  @Override
  public void prepare(ImmutableStack<Object> indices) {
    this.addWritableToAll(ignoredValue -> Optional
        .of(decorateHourDirectory(
            new TraceTimeIntervalWarningsHourDirectory(traceWarningsBundler, cryptoProvider,
                distributionServiceConfig, version)
        )));
    super.prepare(indices);
  }

  private IndexDirectory<Integer, WritableOnDisk> decorateHourDirectory(
      TraceTimeIntervalWarningsHourDirectory hourDirectory) {
    return new HourIntervalIndexingDecorator(hourDirectory, traceWarningsBundler, distributionServiceConfig);
  }
}
