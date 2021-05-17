

package app.coronawarn.server.services.distribution.assembly.appconfig;

import app.coronawarn.server.common.protocols.internal.AppFeatures;
import app.coronawarn.server.common.protocols.internal.ApplicationConfiguration;
import app.coronawarn.server.common.protocols.internal.ApplicationConfiguration.Builder;
import app.coronawarn.server.common.protocols.internal.ApplicationVersionConfiguration;
import app.coronawarn.server.common.protocols.internal.ApplicationVersionInfo;
import app.coronawarn.server.common.protocols.internal.DayPackageMetadata;
import app.coronawarn.server.common.protocols.internal.ExposureDetectionParametersAndroid;
import app.coronawarn.server.common.protocols.internal.ExposureDetectionParametersIOS;
import app.coronawarn.server.common.protocols.internal.HourPackageMetadata;
import app.coronawarn.server.common.protocols.internal.KeyDownloadParametersAndroid;
import app.coronawarn.server.common.protocols.internal.KeyDownloadParametersIOS;
import app.coronawarn.server.common.protocols.internal.SemanticVersion;
import app.coronawarn.server.common.shared.exception.UnableToLoadFileException;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.AppConfigParameters.AndroidExposureDetectionParameters;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.AppConfigParameters.AndroidKeyDownloadParameters;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.AppConfigParameters.DeserializedDayPackageMetadata;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.AppConfigParameters.DeserializedHourPackageMetadata;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.AppConfigParameters.IosExposureDetectionParameters;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.AppConfigParameters.IosKeyDownloadParameters;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.AppVersions;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * Provides the application configuration needed for the mobile client. Contains all necessary sub-configs, including:
 * <ul>
 *   <li>Exposure Configuration</li>
 *   <li>Risk Score Classification</li>
 *   <li>App Config, e.g. minimum risk threshold</li>
 *   <li>Supported countries</li>
 *   <li>Min/Max application version configuration</li>
 *   <li>AndroidKeyDownloadParameters configuration</li>
 *   <li>IosKeyDownloadParameters configuration</li>
 *   <li>AndroidExposureDetectionParameters configuration</li>
 *   <li>IosExposureDetectionParameters configuration</li>
 * </ul>
 *
 * <p>The application config is fetched from the main-config folder. Furthermore it's extended with the
 * list of supported countries and the min/max application versions for android/ios. </p>
 */
@Configuration
public class ApplicationConfigurationPublicationConfig {

  /**
   * The location of the exposure configuration source file.
   */
  public static final String MAIN_FILE = "main-config/app-config.yaml";

  /**
   * Fetches the main configuration as a ApplicationConfiguration instance.
   * @param distributionServiceConfig type DistributionServiceConfig
   * @return the exposure configuration as ApplicationConfiguration
   * @throws UnableToLoadFileException when the file/transformation did not succeed
   */
  @Bean
  public ApplicationConfiguration createMainConfiguration(DistributionServiceConfig distributionServiceConfig)
      throws UnableToLoadFileException {

    return YamlLoader.loadYamlIntoProtobufBuilder(MAIN_FILE, Builder.class)
        .setAppFeatures(
            AppFeatures.newBuilder().addAllAppFeatures(distributionServiceConfig.getAppFeaturesProto()).build()
        )
        .addAllSupportedCountries(List.of(distributionServiceConfig.getSupportedCountries()))
        .setAppVersion(buildApplicationVersionConfiguration(distributionServiceConfig))
        .setAndroidKeyDownloadParameters(buildKeyDownloadParametersAndroid(distributionServiceConfig))
        .setIosKeyDownloadParameters(buildKeyDownloadParametersIos(distributionServiceConfig))
        .setAndroidExposureDetectionParameters(buildExposureDetectionParametersAndroid(distributionServiceConfig))
        .setIosExposureDetectionParameters(buildExposureDetectionParametersIos(distributionServiceConfig))
        .build();
  }

  /**
   * Fetches the source configuration as a ApplicationConfiguration instance.
   * @param distributionServiceConfig type ApplicationVersionConfiguration
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

  /**
   * Fetches the source configuration as a KeyDownloadParametersAndroid instance.
   * @param distributionServiceConfig type DistributionServiceConfig
   * @return test.
   */
  public KeyDownloadParametersAndroid buildKeyDownloadParametersAndroid(
      DistributionServiceConfig distributionServiceConfig) {
    AndroidKeyDownloadParameters androidKeyDownloadParameters =
        distributionServiceConfig.getAppConfigParameters().getAndroidKeyDownloadParameters();
    return KeyDownloadParametersAndroid.newBuilder()
        .setOverallTimeoutInSeconds(androidKeyDownloadParameters.getOverallTimeoutInSeconds())
        .setDownloadTimeoutInSeconds(androidKeyDownloadParameters.getDownloadTimeoutInSeconds())
        .addAllRevokedDayPackages(buildRevokedDayPackages(
            androidKeyDownloadParameters.getRevokedDayPackages()))
        .addAllRevokedHourPackages(buildRevokedHourPackages(
            androidKeyDownloadParameters.getRevokedHourPackages()))
        .build();
  }

  /**
   * Fetches the source configuration as a KeyDownloadParametersIOS instance.
   * @param distributionServiceConfig type DistributionServiceConfig
   * @return test.
   */
  public KeyDownloadParametersIOS buildKeyDownloadParametersIos(
      DistributionServiceConfig distributionServiceConfig) {
    IosKeyDownloadParameters iosKeyDownloadParameters =
        distributionServiceConfig.getAppConfigParameters().getIosKeyDownloadParameters();
    return KeyDownloadParametersIOS.newBuilder()
        .addAllRevokedDayPackages(buildRevokedDayPackages(
            iosKeyDownloadParameters.getRevokedDayPackages()))
        .addAllRevokedHourPackages(buildRevokedHourPackages(
            iosKeyDownloadParameters.getRevokedHourPackages()))
        .build();
  }

  /**
   * Fetches the source configuration as a ExposureDetectionParametersAndroid instance.
   * @param distributionServiceConfig type DistributionServiceConfig
   * @return test.
   */
  public ExposureDetectionParametersAndroid buildExposureDetectionParametersAndroid(
      DistributionServiceConfig distributionServiceConfig) {
    AndroidExposureDetectionParameters androidExposureDetectionParameters =
        distributionServiceConfig.getAppConfigParameters().getAndroidExposureDetectionParameters();
    return ExposureDetectionParametersAndroid.newBuilder()
        .setMaxExposureDetectionsPerInterval(androidExposureDetectionParameters.getMaxExposureDetectionsPerInterval())
        .setOverallTimeoutInSeconds(androidExposureDetectionParameters.getOverallTimeoutInSeconds())
        .build();
  }

  /**
   * Fetches the source configuration as a ExposureDetectionParametersIOS instance.
   * @param distributionServiceConfig type DistributionServiceConfig
   * @return test.
   */
  public ExposureDetectionParametersIOS buildExposureDetectionParametersIos(
      DistributionServiceConfig distributionServiceConfig) {
    IosExposureDetectionParameters iosExposureDetectionParameters =
        distributionServiceConfig.getAppConfigParameters().getIosExposureDetectionParameters();
    return ExposureDetectionParametersIOS.newBuilder()
        .setMaxExposureDetectionsPerInterval(iosExposureDetectionParameters.getMaxExposureDetectionsPerInterval())
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

  private List<DayPackageMetadata> buildRevokedDayPackages(
      List<DeserializedDayPackageMetadata> deserializedDayPackage) {
    return deserializedDayPackage.stream().map(deserializedConfig ->
        DayPackageMetadata.newBuilder()
            .setRegion(deserializedConfig.getRegion())
            .setDate(deserializedConfig.getDate())
            .setEtag(deserializedConfig.getEtag())
            .build()
    ).collect(Collectors.toList());
  }

  private List<HourPackageMetadata> buildRevokedHourPackages(
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
