package app.coronawarn.server.services.distribution.assembly.tracewarnings.structure.directory;

import app.coronawarn.server.services.distribution.assembly.component.CryptoProvider;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import app.coronawarn.server.services.distribution.assembly.tracewarnings.TraceTimeIntervalWarningsPackageBundler;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.util.Optional;
import java.util.Set;

public class TraceTimeWarningsCountryDirectory extends IndexDirectoryOnDisk<String> {

  private TraceTimeIntervalWarningsPackageBundler traceWarningsBundler;
  private CryptoProvider cryptoProvider;
  private DistributionServiceConfig distributionServiceConfig;

  public TraceTimeWarningsCountryDirectory(TraceTimeIntervalWarningsPackageBundler traceWarningsBundler,
      CryptoProvider cryptoProvider, DistributionServiceConfig distributionServiceConfig) {
    super("DE",
        ignoredValue -> Set.of(distributionServiceConfig.getApi().getOriginCountry()),
        Object::toString);
    this.traceWarningsBundler = traceWarningsBundler;
    this.cryptoProvider = cryptoProvider;
    this.distributionServiceConfig = distributionServiceConfig;
  }

  @Override
  public void prepare(ImmutableStack<Object> indices) {
    this.addWritableToAll(ignoredValue -> Optional
        .of(new TraceTimeWarningsHourDirectory(traceWarningsBundler,
            cryptoProvider, distributionServiceConfig)));
    super.prepare(indices);
  }
}
