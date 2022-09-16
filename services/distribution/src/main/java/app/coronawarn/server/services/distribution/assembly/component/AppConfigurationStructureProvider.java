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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Reads configuration parameters from the respective files in the class path and builds a {@link
 * AppConfigurationDirectory} with them.
 */
@Component
@Profile("!revocation")
public class AppConfigurationStructureProvider {

  private final CryptoProvider cryptoProvider;
  private final DistributionServiceConfig distributionServiceConfig;
  private final ApplicationConfiguration applicationConfiguration;
  private final ApplicationConfigurationIOS applicationConfigurationV1Ios;
  private final ApplicationConfigurationAndroid applicationConfigurationV1Android;

  AppConfigurationStructureProvider(CryptoProvider cryptoProvider, DistributionServiceConfig distributionServiceConfig,
      ApplicationConfiguration applicationConfiguration, @Qualifier("applicationConfigurationV1Ios")
      ApplicationConfigurationIOS applicationConfigurationV1Ios, @Qualifier("applicationConfigurationV1Android")
      ApplicationConfigurationAndroid applicationConfigurationV1Android) {
    this.cryptoProvider = cryptoProvider;
    this.distributionServiceConfig = distributionServiceConfig;
    this.applicationConfiguration = applicationConfiguration;
    this.applicationConfigurationV1Ios = applicationConfigurationV1Ios;
    this.applicationConfigurationV1Android = applicationConfigurationV1Android;
  }

  public Directory<WritableOnDisk> getAppConfiguration() {
    return new AppConfigurationDirectory(applicationConfiguration, cryptoProvider, distributionServiceConfig);
  }

  /**
   * Returns a list containing the archives with Application Configuration for Android clients using ENF v2 as well as
   * signature file.
   *
   * @return an archive of app config archives for Android
   */
  public Writable<WritableOnDisk> getAppConfigurationV1ForAndroid() {
    return new AppConfigurationV2StructureProvider<ApplicationConfigurationAndroid>(
        applicationConfigurationV1Android, cryptoProvider, distributionServiceConfig,
        distributionServiceConfig.getApi().getAppConfigV2AndroidFileName())
        .getConfigurationArchive();
  }

  /**
   * Returns a list containing the archives with Application Configuration for IOS clients using ENF v2 as well as
   * signature file.
   *
   * @return an archive of app config archives for iOS
   */
  public Writable<WritableOnDisk> getAppConfigurationV1ForIos() {
    return new AppConfigurationV2StructureProvider<ApplicationConfigurationIOS>(
        applicationConfigurationV1Ios, cryptoProvider, distributionServiceConfig,
        distributionServiceConfig.getApi().getAppConfigV2IosFileName()).getConfigurationArchive();
  }
}
