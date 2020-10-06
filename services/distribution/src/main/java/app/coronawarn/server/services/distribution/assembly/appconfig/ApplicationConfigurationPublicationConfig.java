

package app.coronawarn.server.services.distribution.assembly.appconfig;

import app.coronawarn.server.common.protocols.internal.AppFeatures;
import app.coronawarn.server.common.protocols.internal.ApplicationConfiguration;
import app.coronawarn.server.common.protocols.internal.ApplicationConfiguration.Builder;
import app.coronawarn.server.common.protocols.internal.ApplicationVersionConfiguration;
import app.coronawarn.server.common.protocols.internal.ApplicationVersionInfo;
import app.coronawarn.server.common.protocols.internal.SemanticVersion;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.AppVersions;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



/**
 * Provides the application configuration needed for the mobile client. Contains all necessary sub-configs, including:
 * <ul>
 *   <li>Exposure Configuration</li>
 *   <li>Risk Score Classification</li>
 *   <li>App Config, e.g. minimum risk threshold</li>
 * </ul>
 *
 * <p>The application config is fetched from the master-config folder.</p>
 */
@Configuration
public class ApplicationConfigurationPublicationConfig {

  /**
   * The location of the exposure configuration master file.
   */
  public static final String MASTER_FILE = "master-config/app-config.yaml";

  /**
   * Fetches the master configuration as a ApplicationConfiguration instance.
   *
   * @return the exposure configuration as ApplicationConfiguration
   * @throws UnableToLoadFileException when the file/transformation did not succeed
   */
  @Bean
  public ApplicationConfiguration createMasterConfiguration(DistributionServiceConfig distributionServiceConfig)
      throws UnableToLoadFileException {

    return YamlLoader.loadYamlIntoProtobufBuilder(MASTER_FILE, Builder.class)
        .setAppFeatures(
            AppFeatures.newBuilder().addAllAppFeatures(distributionServiceConfig.getAppFeaturesProto()).build()
        )
        .addAllSupportedCountries(List.of(distributionServiceConfig.getSupportedCountries()))
        .setAppVersion(buildApplicationVersionConfiguration(distributionServiceConfig))
        .build();
  }

  /**
   * Fetches the master configuration as a ApplicationConfiguration instance.
   *
   * @return test.
   */
  public ApplicationVersionConfiguration buildApplicationVersionConfiguration(
      DistributionServiceConfig distributionServiceConfig) {
    AppVersions appVersions = distributionServiceConfig.getAppVersions();
    return ApplicationVersionConfiguration.newBuilder()
        .setAndroid(buildApplicationVersionInfo(appVersions.getLatestAndroid(), appVersions.getMinAndroid()))
        .setIos(buildApplicationVersionInfo(appVersions.getLatestIos(), appVersions.getMinIos()))
        .build();
  }

  private ApplicationVersionInfo buildApplicationVersionInfo(String latestVersion, String minVersion) {
    return ApplicationVersionInfo.newBuilder()
        .setLatest(buildSemanticVersion(latestVersion))
        .setMin(buildSemanticVersion(minVersion))
        .build();
  }

  private SemanticVersion buildSemanticVersion(String version) {
    return SemanticVersion.newBuilder()
        .setMajor(getSemanticVersionNumber(version, 0))
        .setMinor(getSemanticVersionNumber(version, 1))
        .setPatch(getSemanticVersionNumber(version, 2))
        .build();
  }

  private int getSemanticVersionNumber(String version, int position) {
    String[] items = version.split("\\.");
    return Integer.valueOf(items[position]);
  }
}
