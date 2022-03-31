package app.coronawarn.server.services.distribution.assembly.component;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("revocation")
public class DccRevocationListStructureProvider {
  private final CryptoProvider cryptoProvider;
  private final DistributionServiceConfig distributionServiceConfig;

  public DccRevocationListStructureProvider(CryptoProvider cryptoProvider,
      DistributionServiceConfig distributionServiceConfig) {
    this.cryptoProvider = cryptoProvider;
    this.distributionServiceConfig = distributionServiceConfig;
  }
}
