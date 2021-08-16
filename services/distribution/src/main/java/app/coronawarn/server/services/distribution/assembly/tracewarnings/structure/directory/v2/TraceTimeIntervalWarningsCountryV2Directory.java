package app.coronawarn.server.services.distribution.assembly.tracewarnings.structure.directory.v2;

import app.coronawarn.server.common.persistence.domain.TraceTimeIntervalWarning;
import app.coronawarn.server.common.shared.collection.ImmutableStack;
import app.coronawarn.server.services.distribution.assembly.component.CryptoProvider;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.tracewarnings.TraceTimeIntervalWarningsPackageBundler;
import app.coronawarn.server.services.distribution.assembly.tracewarnings.structure.directory.decorator.HourIntervalIndexingV2Decorator;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.util.Optional;
import java.util.Set;

public class TraceTimeIntervalWarningsCountryV2Directory extends IndexDirectoryOnDisk<String> {

  protected final TraceTimeIntervalWarningsPackageBundler traceWarningsBundler;
  private final CryptoProvider cryptoProvider;
  private final DistributionServiceConfig distributionServiceConfig;

  /**
   * Creates an instance of the custom directory that includes the entire {@link TraceTimeIntervalWarning} package
   * structure for a country as per the API specification.
   */
  public TraceTimeIntervalWarningsCountryV2Directory(
      TraceTimeIntervalWarningsPackageBundler traceWarningsBundler, CryptoProvider cryptoProvider,
      DistributionServiceConfig distributionServiceConfig) {
    super(distributionServiceConfig.getApi().getCountryPath(),
        ignoredValue -> Set.of(distributionServiceConfig.getApi().getOriginCountry()),
        Object::toString);
    this.traceWarningsBundler = traceWarningsBundler;
    this.cryptoProvider = cryptoProvider;
    this.distributionServiceConfig = distributionServiceConfig;

  }

  @Override
  public void prepare(ImmutableStack<Object> indices) {
    this.addWritableToAll(ignoredValue -> Optional
        .of(decorateV2HourDirectory(
            new TraceTimeIntervalWarningsHourV2Directory(traceWarningsBundler, cryptoProvider,
                distributionServiceConfig)
        )));
    super.prepare(indices);
  }

  private IndexDirectory<Integer, WritableOnDisk> decorateV2HourDirectory(
      TraceTimeIntervalWarningsHourV2Directory hourDirectory) {
    return new HourIntervalIndexingV2Decorator(hourDirectory, traceWarningsBundler, distributionServiceConfig);
  }

}
