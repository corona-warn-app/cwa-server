package app.coronawarn.server.services.distribution.assembly.appconfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import app.coronawarn.server.common.protocols.internal.v2.ApplicationConfigurationAndroid;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;

/**
 * Provides the application configuration needed for mobile clients which use Exposure Notification
 * Framework V2. As of CWA version 1.7, Android and IOS configurations have been separated and
 * distributed in CDNs at different URLs. This Spring bean loads the default values defined in the
 * YAML configurations of each device type found under <code> /master-config/ </code> folder,
 * extends it with some Distribution Service global parameters and registers them for usage during
 * file archiving & bundling.
 */
@Configuration
public class ApplicationConfigurationV2PublicationConfig {

  /**
   * The location of the exposure configuration master files for Android and Ios.
   */
  public static final String ANDROID_V2_MASTER_FILE = "master-config/v2/app-config_android.yaml";

  /**
   * Fetches the master configuration as a ApplicationConfigurationAndroid instance.
   */
  @Bean
  public ApplicationConfigurationAndroid createAndroidV2Configuration(DistributionServiceConfig distributionServiceConfig)
      throws UnableToLoadFileException {
    return YamlLoader.loadYamlIntoProtobufBuilder(ANDROID_V2_MASTER_FILE, ApplicationConfigurationAndroid.Builder.class)
        .build();
  }
}
