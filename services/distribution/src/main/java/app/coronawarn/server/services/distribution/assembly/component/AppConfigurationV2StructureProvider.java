package app.coronawarn.server.services.distribution.assembly.component;

import app.coronawarn.server.common.protocols.internal.v2.ApplicationConfigurationAndroid;
import app.coronawarn.server.common.protocols.internal.v2.ApplicationConfigurationIOS;
import app.coronawarn.server.services.distribution.assembly.appconfig.structure.directory.AppConfigurationDirectory;
import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Reads configuration parameters from the respective files in the class path and builds a {@link
 * AppConfigurationDirectory} with them.
 */
@Component
public class AppConfigurationV2StructureProvider {

  private final CryptoProvider cryptoProvider;
  private final DistributionServiceConfig distributionServiceConfig;
  private final ApplicationConfigurationIOS applicationConfigurationV2Ios;
  private final ApplicationConfigurationAndroid applicationConfigurationV2Android;

  AppConfigurationV2StructureProvider(CryptoProvider cryptoProvider,
      DistributionServiceConfig distributionServiceConfig, @Qualifier("applicationConfigurationV2Ios")
      ApplicationConfigurationIOS applicationConfigurationV2Ios, @Qualifier("applicationConfigurationV2Android")
      ApplicationConfigurationAndroid applicationConfigurationV2Android) {
    this.cryptoProvider = cryptoProvider;
    this.distributionServiceConfig = distributionServiceConfig;
    this.applicationConfigurationV2Ios = applicationConfigurationV2Ios;
    this.applicationConfigurationV2Android = applicationConfigurationV2Android;
  }

  /**
   * Returns a list containing the archives with Application Configuration for Android clients using ENF v2 as well as
   * signature file.
   *
   * @return an archive of app config archives for Android
   */
  @Qualifier("applicationConfigurationV2Android")
  public Writable<WritableOnDisk> getAppConfigurationV2ForAndroid() {
    return new app.coronawarn.server.services.distribution.assembly.appconfig.structure.directory.v2
        .AppConfigurationV2StructureProvider<ApplicationConfigurationAndroid>(
        applicationConfigurationV2Android, cryptoProvider, distributionServiceConfig,
        distributionServiceConfig.getApi().getAppConfigV2AndroidFileName())
        .getConfigurationArchive();
  }

  /**
   * Returns a list containing the archives with Application Configuration for IOS clients using ENF v2 as well as
   * signature file.
   *
   * @return an archive of app config archives for iOS
   */
  @Qualifier("applicationConfigurationV2Ios")
  public Writable<WritableOnDisk> getAppConfigurationV2ForIos() {
    return new app.coronawarn.server.services.distribution.assembly.appconfig.structure.directory.v2
        .AppConfigurationV2StructureProvider<ApplicationConfigurationIOS>(
        applicationConfigurationV2Ios, cryptoProvider, distributionServiceConfig,
        distributionServiceConfig.getApi().getAppConfigV2IosFileName()).getConfigurationArchive();
  }
}
