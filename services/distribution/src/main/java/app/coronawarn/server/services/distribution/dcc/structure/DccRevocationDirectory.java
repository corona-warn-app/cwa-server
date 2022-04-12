package app.coronawarn.server.services.distribution.dcc.structure;

import app.coronawarn.server.common.shared.collection.ImmutableStack;
import app.coronawarn.server.services.distribution.assembly.component.CryptoProvider;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectoryOnDisk;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.util.Collections;

public class DccRevocationDirectory extends IndexDirectoryOnDisk<String> {

  private final CryptoProvider cryptoProvider;
  private final DistributionServiceConfig distributionServiceConfig;

  /**
   * Dcc Revocation list structure directory.
   */
  public DccRevocationDirectory(DistributionServiceConfig distributionServiceConfig, CryptoProvider cryptoProvider) {
    super(distributionServiceConfig.getDccRevocation().getDccRevocationDirectory(),
        ignoredValue -> Collections.emptySet(),
        Object::toString);
    this.distributionServiceConfig = distributionServiceConfig;
    this.cryptoProvider = cryptoProvider;
  }

  @Override
  public void prepare(ImmutableStack<Object> indices) {
    super.prepare(indices);
  }

}
