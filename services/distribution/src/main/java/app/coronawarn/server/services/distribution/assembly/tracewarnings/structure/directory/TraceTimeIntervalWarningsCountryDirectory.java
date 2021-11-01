package app.coronawarn.server.services.distribution.assembly.tracewarnings.structure.directory;

import app.coronawarn.server.common.persistence.domain.TraceTimeIntervalWarning;
import app.coronawarn.server.common.shared.collection.ImmutableStack;
import app.coronawarn.server.services.distribution.assembly.component.CryptoProvider;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.tracewarnings.TraceTimeIntervalWarningsPackageBundler;
import app.coronawarn.server.services.distribution.assembly.tracewarnings.structure.directory.decorator.HourIntervalIndexingV1Decorator;
import app.coronawarn.server.services.distribution.assembly.tracewarnings.structure.directory.decorator.HourIntervalIndexingV2Decorator;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TraceTimeIntervalWarningsCountryDirectory extends IndexDirectoryOnDisk<String> {

  private static final String VERSION_V2 = "v2";
  private static final String VERSION_V1 = "v1";
  private final String version;
  protected final TraceTimeIntervalWarningsPackageBundler traceWarningsBundler;
  private final CryptoProvider cryptoProvider;
  private final DistributionServiceConfig distributionServiceConfig;

  private static final Logger logger = LoggerFactory.getLogger(TraceTimeIntervalWarningsCountryDirectory.class);

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
    if (this.version.equals(VERSION_V1)) {
      this.addWritableToAll(ignoredValue -> Optional
          .of(decorateV1HourDirectory(
              new TraceTimeIntervalWarningsHourV1Directory(traceWarningsBundler, cryptoProvider,
                  distributionServiceConfig)
          )));
    } else if (this.version.equals(VERSION_V2)) {
      logger.debug("Preparing encrypted checkins for version {}", this.version);
      this.addWritableToAll(ignoredValue -> Optional
          .of(decorateV2HourDirectory(
              new TraceTimeIntervalWarningsHourV2Directory(traceWarningsBundler, cryptoProvider,
                  distributionServiceConfig)
          )));
    }
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


  private IndexDirectory<Integer, WritableOnDisk> decorateV2HourDirectory(
      TraceTimeIntervalWarningsHourV2Directory hourDirectory) {
    return new HourIntervalIndexingV2Decorator(hourDirectory, traceWarningsBundler, distributionServiceConfig);
  }

}
