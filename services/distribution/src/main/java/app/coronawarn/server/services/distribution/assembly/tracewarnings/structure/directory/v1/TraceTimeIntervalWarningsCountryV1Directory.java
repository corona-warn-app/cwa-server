package app.coronawarn.server.services.distribution.assembly.tracewarnings.structure.directory.v1;

import app.coronawarn.server.common.persistence.domain.TraceTimeIntervalWarning;
import app.coronawarn.server.common.shared.collection.ImmutableStack;
import app.coronawarn.server.services.distribution.assembly.component.CryptoProvider;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.tracewarnings.TraceTimeIntervalWarningsPackageBundler;
import app.coronawarn.server.services.distribution.assembly.tracewarnings.structure.directory.decorator.HourIntervalIndexingV1Decorator;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.util.Optional;
import java.util.Set;

/**
 * Country directory for v1 implementation of check ins.
 */
@Deprecated(since = "2.8")
public class TraceTimeIntervalWarningsCountryV1Directory extends IndexDirectoryOnDisk<String> {

  protected final TraceTimeIntervalWarningsPackageBundler traceWarningsBundler;
  private final CryptoProvider cryptoProvider;
  private final DistributionServiceConfig distributionServiceConfig;

  /**
   * Creates an instance of the custom directory that includes the entire {@link TraceTimeIntervalWarning} package
   * structure for a country as per the API specification.
   *
   * @deprecated in favor of checkin protected results.
   */
  @Deprecated(since = "2.8")
  public TraceTimeIntervalWarningsCountryV1Directory(
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
        .of(decorateV1HourDirectory(
            new TraceTimeIntervalWarningsHourV1Directory(traceWarningsBundler, cryptoProvider,
                distributionServiceConfig)
        )));
    super.prepare(indices);
  }

  /**
   * Decorate v1 directory.
   *
   * @param hourDirectory the directory to decorate.
   * @deprecated because trace time warnings are being replaced by protected reports.
   */
  @Deprecated(since = "2.8")
  private IndexDirectory<Integer, WritableOnDisk> decorateV1HourDirectory(
      TraceTimeIntervalWarningsHourV1Directory hourDirectory) {
    return new HourIntervalIndexingV1Decorator(hourDirectory, traceWarningsBundler, distributionServiceConfig);
  }
}
