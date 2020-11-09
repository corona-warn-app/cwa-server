

package app.coronawarn.server.services.distribution.assembly.component;

import app.coronawarn.server.common.protocols.internal.ApplicationConfiguration;
import app.coronawarn.server.services.distribution.assembly.appconfig.structure.directory.AppConfigurationDirectory;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import org.springframework.stereotype.Component;

/**
 * Reads configuration parameters from the respective files in the class path and builds a {@link
 * AppConfigurationDirectory} with them.
 */
@Component
public class AppConfigurationStructureProvider {

  private final CryptoProvider cryptoProvider;
  private final DistributionServiceConfig distributionServiceConfig;
  private final ApplicationConfiguration applicationConfiguration;

  AppConfigurationStructureProvider(CryptoProvider cryptoProvider, DistributionServiceConfig distributionServiceConfig,
      ApplicationConfiguration applicationConfiguration) {
    this.cryptoProvider = cryptoProvider;
    this.distributionServiceConfig = distributionServiceConfig;
    this.applicationConfiguration = applicationConfiguration;
  }

  public Directory<WritableOnDisk> getAppConfiguration() {
    return new AppConfigurationDirectory(applicationConfiguration, cryptoProvider, distributionServiceConfig);
  }

  public Directory<WritableOnDisk> getAppConfigurationV2ForAndroid() {
    //todo
    return null;
  }

  public Directory<WritableOnDisk> getAppConfigurationV2ForIos() {
    //todo
    return null;
  }
}
