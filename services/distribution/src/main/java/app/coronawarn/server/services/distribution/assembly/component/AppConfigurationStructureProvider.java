

package app.coronawarn.server.services.distribution.assembly.component;

import app.coronawarn.server.common.protocols.internal.ApplicationConfiguration;
import app.coronawarn.server.common.protocols.internal.v2.ApplicationConfigurationAndroid;
import app.coronawarn.server.common.protocols.internal.v2.ApplicationConfigurationIOS;
import app.coronawarn.server.services.distribution.assembly.appconfig.structure.directory.AppConfigurationDirectory;
import app.coronawarn.server.services.distribution.assembly.appconfig.structure.directory.v2.AppConfigurationV2StructureProvider;
import app.coronawarn.server.services.distribution.assembly.structure.Writable;
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
  private final ApplicationConfigurationIOS applicationConfigurationV2Ios;
  private final ApplicationConfigurationAndroid applicationConfigurationV2Android;

  AppConfigurationStructureProvider(CryptoProvider cryptoProvider, DistributionServiceConfig distributionServiceConfig,
      ApplicationConfiguration applicationConfiguration, ApplicationConfigurationIOS applicationConfigurationV2Ios,
      ApplicationConfigurationAndroid applicationConfigurationV2Android) {
    this.cryptoProvider = cryptoProvider;
    this.distributionServiceConfig = distributionServiceConfig;
    this.applicationConfiguration = applicationConfiguration;
    this.applicationConfigurationV2Ios = applicationConfigurationV2Ios;
    this.applicationConfigurationV2Android = applicationConfigurationV2Android;
  }

  public Directory<WritableOnDisk> getAppConfiguration() {
    return new AppConfigurationDirectory(applicationConfiguration, cryptoProvider, distributionServiceConfig);
  }

  /**
   * Returns a list containing the archives with Application Configuration for
   * Android clients using ENF v2 as well as signature file.
   */
  public Writable<WritableOnDisk> getAppConfigurationV2ForAndroid() {
    return new AppConfigurationV2StructureProvider<ApplicationConfigurationAndroid>(
        applicationConfigurationV2Android, cryptoProvider, distributionServiceConfig,
        distributionServiceConfig.getApi().getAppConfigV2AndroidFileName())
            .getConfigurationArchive();
  }

  /**
   * Returns a list containing the archives with Application Configuration for
   * IOS clients using ENF v2 as well as signature file.
   */
  public Writable<WritableOnDisk>  getAppConfigurationV2ForIos() {
    return new AppConfigurationV2StructureProvider<ApplicationConfigurationIOS>(
        applicationConfigurationV2Ios, cryptoProvider, distributionServiceConfig,
        distributionServiceConfig.getApi().getAppConfigV2IosFileName()).getConfigurationArchive();
  }
}
