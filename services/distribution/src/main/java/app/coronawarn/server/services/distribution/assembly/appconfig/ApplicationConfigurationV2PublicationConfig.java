package app.coronawarn.server.services.distribution.assembly.appconfig;

import app.coronawarn.server.common.protocols.internal.v2.AppFeature;
import app.coronawarn.server.common.protocols.internal.v2.AppFeatures;
import app.coronawarn.server.common.protocols.internal.v2.ApplicationConfigurationAndroid;
import app.coronawarn.server.common.protocols.internal.v2.ApplicationConfigurationIOS;
import app.coronawarn.server.common.protocols.internal.v2.DayPackageMetadata;
import app.coronawarn.server.common.protocols.internal.v2.ExposureDetectionParametersAndroid;
import app.coronawarn.server.common.protocols.internal.v2.HourPackageMetadata;
import app.coronawarn.server.common.protocols.internal.v2.KeyDownloadParametersAndroid;
import app.coronawarn.server.common.protocols.internal.v2.SemanticVersion;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.AppConfigParameters.AndroidExposureDetectionParameters;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.AppConfigParameters.AndroidKeyDownloadParameters;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.AppConfigParameters.DeserializedDayPackageMetadata;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.AppConfigParameters.DeserializedHourPackageMetadata;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
  public static final String IOS_V2_MASTER_FILE = "master-config/v2/app-config-ios.yaml";
  public static final String ANDROID_V2_MASTER_FILE = "master-config/v2/app-config-android.yaml";

  /**
   * Fetches the master configuration as a ApplicationConfigurationAndroid instance.
   */
  @Bean
  public ApplicationConfigurationAndroid createAndroidV2Configuration(DistributionServiceConfig distributionServiceConfig)
      throws UnableToLoadFileException {
    return YamlLoader.loadYamlIntoProtobufBuilder(ANDROID_V2_MASTER_FILE, ApplicationConfigurationAndroid.Builder.class)
        .setMinVersionCode(distributionServiceConfig.getAppVersions().getMinAndroidVersionCode())
        .setLatestVersionCode(distributionServiceConfig.getAppVersions().getLatestAndroidVersionCode())
        .setAppFeatures(buildAppFeatures(distributionServiceConfig))
        .addAllSupportedCountries(List.of(distributionServiceConfig.getSupportedCountries()))
        .setKeyDownloadParameters(buildKeyDownloadParametersAndroid(distributionServiceConfig))
        .setExposureDetectionParameters(buildExposureDetectionParametersAndroid(distributionServiceConfig))
        .build();
  }

  private AppFeatures buildAppFeatures(DistributionServiceConfig distributionServiceConfig) {
    List<AppFeature> v2Features = distributionServiceConfig
        .getAppFeatures().stream().map(feature -> AppFeature.newBuilder()
            .setLabel(feature.getLabel()).setValue(feature.getValue()).build())
        .collect(Collectors.toList());
    return AppFeatures.newBuilder().addAllAppFeatures(v2Features).build();
  }

  private KeyDownloadParametersAndroid buildKeyDownloadParametersAndroid(
      DistributionServiceConfig distributionServiceConfig) {
    AndroidKeyDownloadParameters androidKeyDownloadParameters =
        distributionServiceConfig.getAppConfigParameters().getAndroidKeyDownloadParameters();
    return KeyDownloadParametersAndroid.newBuilder()
        .setOverallTimeoutInSeconds(androidKeyDownloadParameters.getOverallTimeoutInSeconds())
        .setDownloadTimeoutInSeconds(androidKeyDownloadParameters.getDownloadTimeoutInSeconds())
        .addAllCachedDayPackagesToUpdateOnETagMismatch(buildCachedDayPackagesToUpdateOnETagMismatch(
            androidKeyDownloadParameters.getCachedDayPackagesToUpdateOnETagMismatch()))
        .addAllCachedHourPackagesToUpdateOnETagMismatch(buildCachedHourPackagesToUpdateOnETagMismatch(
            androidKeyDownloadParameters.getCachedHourPackagesToUpdateOnETagMismatch()))
        .build();
  }

  /**
   * Fetches the master configuration as a ApplicationConfigurationAndroid instance.
   */
  @Bean
  public ApplicationConfigurationIOS createIosV2Configuration(DistributionServiceConfig distributionServiceConfig)
      throws UnableToLoadFileException {
    return YamlLoader.loadYamlIntoProtobufBuilder(IOS_V2_MASTER_FILE, ApplicationConfigurationIOS.Builder.class)
        .addAllSupportedCountries(List.of(distributionServiceConfig.getSupportedCountries()))
        .setMinVersion(buildSemanticVersion(distributionServiceConfig.getAppVersions().getMinIos()))
        .setLatestVersion(buildSemanticVersion(distributionServiceConfig.getAppVersions().getLatestIos()))
        .build();
  }

  private app.coronawarn.server.common.protocols.internal.v2.SemanticVersion buildSemanticVersion(String version) {
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

  private ExposureDetectionParametersAndroid buildExposureDetectionParametersAndroid(
      DistributionServiceConfig distributionServiceConfig) {
    AndroidExposureDetectionParameters androidExposureDetectionParameters =
        distributionServiceConfig.getAppConfigParameters().getAndroidExposureDetectionParameters();
    return ExposureDetectionParametersAndroid.newBuilder()
        .setMaxExposureDetectionsPerInterval(androidExposureDetectionParameters.getMaxExposureDetectionsPerInterval())
        .setOverallTimeoutInSeconds(androidExposureDetectionParameters.getOverallTimeoutInSeconds())
        .build();
  }

  private List<DayPackageMetadata> buildCachedDayPackagesToUpdateOnETagMismatch(
      List<DeserializedDayPackageMetadata> deserializedDayPackage) {
    return deserializedDayPackage.stream().map(deserializedConfig ->
        DayPackageMetadata.newBuilder()
            .setRegion(deserializedConfig.getRegion())
            .setDate(deserializedConfig.getDate())
            .setEtag(deserializedConfig.getEtag())
            .build()
    ).collect(Collectors.toList());
  }

  private List<HourPackageMetadata> buildCachedHourPackagesToUpdateOnETagMismatch(
      List<DeserializedHourPackageMetadata> deserializedHourPackage) {
    return deserializedHourPackage.stream().map(deserializedHourConfig ->
        HourPackageMetadata.newBuilder()
            .setRegion(deserializedHourConfig.getRegion())
            .setDate(deserializedHourConfig.getDate())
            .setHour(deserializedHourConfig.getHour())
            .setEtag(deserializedHourConfig.getEtag())
            .build()).collect(Collectors.toList());
  }
}
