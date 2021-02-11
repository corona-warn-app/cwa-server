package app.coronawarn.server.services.distribution.assembly.appconfig;

import app.coronawarn.server.common.protocols.internal.v2.AppFeature;
import app.coronawarn.server.common.protocols.internal.v2.AppFeatures;
import app.coronawarn.server.common.protocols.internal.v2.ApplicationConfigurationAndroid;
import app.coronawarn.server.common.protocols.internal.v2.ApplicationConfigurationIOS;
import app.coronawarn.server.common.protocols.internal.v2.DailySummariesConfig;
import app.coronawarn.server.common.protocols.internal.v2.DayPackageMetadata;
import app.coronawarn.server.common.protocols.internal.v2.DiagnosisKeysDataMapping;
import app.coronawarn.server.common.protocols.internal.v2.ExposureConfiguration;
import app.coronawarn.server.common.protocols.internal.v2.ExposureDetectionParametersAndroid;
import app.coronawarn.server.common.protocols.internal.v2.ExposureDetectionParametersIOS;
import app.coronawarn.server.common.protocols.internal.v2.HourPackageMetadata;
import app.coronawarn.server.common.protocols.internal.v2.KeyDownloadParametersAndroid;
import app.coronawarn.server.common.protocols.internal.v2.KeyDownloadParametersIOS;
import app.coronawarn.server.common.protocols.internal.v2.PPDDEventDrivenUserSurveyParametersAndroid;
import app.coronawarn.server.common.protocols.internal.v2.PPDDEventDrivenUserSurveyParametersCommon;
import app.coronawarn.server.common.protocols.internal.v2.PPDDEventDrivenUserSurveyParametersIOS;
import app.coronawarn.server.common.protocols.internal.v2.PPDDPrivacyPreservingAccessControlParametersAndroid;
import app.coronawarn.server.common.protocols.internal.v2.PPDDPrivacyPreservingAnalyticsParametersAndroid;
import app.coronawarn.server.common.protocols.internal.v2.PPDDPrivacyPreservingAnalyticsParametersCommon;
import app.coronawarn.server.common.protocols.internal.v2.PPDDPrivacyPreservingAnalyticsParametersIOS;
import app.coronawarn.server.common.protocols.internal.v2.RiskCalculationParameters;
import app.coronawarn.server.common.protocols.internal.v2.SemanticVersion;
import app.coronawarn.server.services.distribution.assembly.appconfig.parsing.v2.DeserializedDailySummariesConfig;
import app.coronawarn.server.services.distribution.assembly.appconfig.parsing.v2.DeserializedDiagnosisKeysDataMapping;
import app.coronawarn.server.services.distribution.assembly.appconfig.parsing.v2.DeserializedExposureConfiguration;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.AppConfigParameters.AndroidEventDrivenUserSurveyParameters;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.AppConfigParameters.AndroidExposureDetectionParameters;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.AppConfigParameters.AndroidKeyDownloadParameters;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.AppConfigParameters.AndroidPrivacyPreservingAnalyticsParameters;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.AppConfigParameters.DeserializedDayPackageMetadata;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.AppConfigParameters.DeserializedHourPackageMetadata;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.AppConfigParameters.IosEventDrivenUserSurveyParameters;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.AppConfigParameters.IosExposureDetectionParameters;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.AppConfigParameters.IosKeyDownloadParameters;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.AppConfigParameters.IosPrivacyPreservingAnalyticsParameters;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Provides the application configuration needed for mobile clients which use Exposure Notification
 * Framework V2. As of CWA version 1.7, Android and IOS configurations have been separated and
 * distributed in CDNs at different URLs. This Spring bean loads the default values defined in the
 * YAML configurations of each device type found under <code> /main-config/ </code> folder,
 * extends it with some Distribution Service global parameters and registers them for usage during
 * file archiving and bundling.
 */
@Configuration
public class ApplicationConfigurationV2PublicationConfig {

  /**
   * The location of the exposure configuration source files for Android and Ios.
   */
  private static final String V2_RISK_PARAMETERS_FILE = "main-config/v2/risk-calculation-parameters.yaml";
  private static final String ANDROID_V2_DATA_MAPPING_FILE = "main-config/v2/diagnosis-keys-data-mapping.yaml";
  private static final String ANDROID_V2_DAILY_SUMMARIES_FILE = "main-config/v2/daily-summaries-config.yaml";
  private static final String IOS_V2_EXPOSURE_CONFIGURATION_FILE = "main-config/v2/exposure-configuration.yaml";

  /**
   * Fetches the source configuration as a ApplicationConfigurationAndroid instance.
   * @param distributionServiceConfig type DistributionServiceConfig
   * @return android configuration
   * @throws UnableToLoadFileException if either the file access or subsequent yaml parsing fails.
   */
  @Bean
  public ApplicationConfigurationAndroid createAndroidV2Configuration(
      DistributionServiceConfig distributionServiceConfig) throws UnableToLoadFileException {

    RiskCalculationParameters.Builder riskCalculationParameterBuilder =
        YamlLoader.loadYamlIntoProtobufBuilder(V2_RISK_PARAMETERS_FILE,
            RiskCalculationParameters.Builder.class);

    DeserializedDiagnosisKeysDataMapping dataMapping = YamlLoader.loadYamlIntoClass(
        ANDROID_V2_DATA_MAPPING_FILE, DeserializedDiagnosisKeysDataMapping.class);

    DeserializedDailySummariesConfig dailySummaries = YamlLoader
        .loadYamlIntoClass(ANDROID_V2_DAILY_SUMMARIES_FILE, DeserializedDailySummariesConfig.class);

    return ApplicationConfigurationAndroid.newBuilder()
        .setRiskCalculationParameters(riskCalculationParameterBuilder)
        .setMinVersionCode(distributionServiceConfig.getAppVersions().getMinAndroidVersionCode())
        .setLatestVersionCode(
            distributionServiceConfig.getAppVersions().getLatestAndroidVersionCode())
        .setAppFeatures(buildAppFeatures(distributionServiceConfig))
        .addAllSupportedCountries(List.of(distributionServiceConfig.getSupportedCountries()))
        .setKeyDownloadParameters(buildKeyDownloadParametersAndroid(distributionServiceConfig))
        .setExposureDetectionParameters(
            buildExposureDetectionParametersAndroid(distributionServiceConfig))
        .setDailySummariesConfig(buildDailySummaries(dailySummaries))
        .setDiagnosisKeysDataMapping(buildDataMapping(dataMapping))
        .setEventDrivenUserSurveyParameters(buildAndroidEdusParameters(distributionServiceConfig))
        .setPrivacyPreservingAnalyticsParameters(buildAndroidPpaParameters(distributionServiceConfig))
        .build();
  }


  private PPDDEventDrivenUserSurveyParametersAndroid buildAndroidEdusParameters(
      DistributionServiceConfig distributionServiceConfig) {
    AndroidEventDrivenUserSurveyParameters androidEdusParams = distributionServiceConfig
        .getAppConfigParameters().getAndroidEventDrivenUserSurveyParameters();
    return PPDDEventDrivenUserSurveyParametersAndroid.newBuilder()
        .setCommon(PPDDEventDrivenUserSurveyParametersCommon.newBuilder()
            .setOtpQueryParameterName(androidEdusParams.getOtpQueryParameterName())
            .setSurveyOnHighRiskEnabled(androidEdusParams.getSurveyOnHighRiskEnabled())
            .setSurveyOnHighRiskUrl(androidEdusParams.getSurveyOnHighRiskUrl())
            .build())
        .setPpac(PPDDPrivacyPreservingAccessControlParametersAndroid.newBuilder()
            .setRequireBasicIntegrity(androidEdusParams.getRequireBasicIntegrity())
            .setRequireCTSProfileMatch(androidEdusParams.getRequireCtsProfileMatch())
            .setRequireEvaluationTypeBasic(androidEdusParams.getRequireEvaluationTypeBasic())
            .setRequireEvaluationTypeHardwareBacked(androidEdusParams.getRequireEvaluationTypeHardwareBacked())
            .build())
        .build();
  }

  private PPDDPrivacyPreservingAnalyticsParametersAndroid buildAndroidPpaParameters(
      DistributionServiceConfig distributionServiceConfig) {
    AndroidPrivacyPreservingAnalyticsParameters androidPpaParams = distributionServiceConfig
        .getAppConfigParameters().getAndroidPrivacyPreservingAnalyticsParameters();
    return PPDDPrivacyPreservingAnalyticsParametersAndroid.newBuilder()
        .setCommon(PPDDPrivacyPreservingAnalyticsParametersCommon.newBuilder()
            .setProbabilityToSubmit(androidPpaParams.getProbabilityToSubmit())
            .setProbabilityToSubmitExposureWindows(androidPpaParams.getProbabilityToSubmitExposureWindows())
            .setHoursSinceTestRegistrationToSubmitTestResultMetadata(
                androidPpaParams.getHoursSinceTestRegistrationToSubmitTestResultMetadata())
            .setHoursSinceTestResultToSubmitKeySubmissionMetadata(
                androidPpaParams.getHoursSinceTestToSubmitKeySubmissionMetadata())
            .build())
        .setPpac(PPDDPrivacyPreservingAccessControlParametersAndroid.newBuilder()
            .setRequireBasicIntegrity(androidPpaParams.getRequireBasicIntegrity())
            .setRequireCTSProfileMatch(androidPpaParams.getRequireCtsProfileMatch())
            .setRequireEvaluationTypeBasic(androidPpaParams.getRequireEvaluationTypeBasic())
            .setRequireEvaluationTypeHardwareBacked(androidPpaParams.getRequireEvaluationTypeHardwareBacked())
            .build())
        .build();
  }

  private DiagnosisKeysDataMapping buildDataMapping(
      DeserializedDiagnosisKeysDataMapping dataMapping) {
    return DiagnosisKeysDataMapping.newBuilder()
        .setInfectiousnessWhenDaysSinceOnsetMissing(
            dataMapping.getInfectiousnessWhenDaysSinceOnsetMissing())
        .setReportTypeWhenMissing(dataMapping.getReportTypeWhenMissing())
        .putAllDaysSinceOnsetToInfectiousness(dataMapping.getDaysSinceOnsetToInfectiousness())
        .build();
  }

  private DailySummariesConfig buildDailySummaries(
      DeserializedDailySummariesConfig dailySummaries) {
    return DailySummariesConfig.newBuilder()
        .addAllAttenuationBucketThresholdDb(dailySummaries.getAttenuationBucketThresholdDb())
        .addAllAttenuationBucketWeights(dailySummaries.getAttenuationBucketWeights())
        .setDaysSinceExposureThreshold(dailySummaries.getDaysSinceExposureThreshold())
        .setMinimumWindowScore(dailySummaries.getMinimumWindowScore())
        .putAllReportTypeWeights(dailySummaries.getReportTypeWeights())
        .putAllInfectiousnessWeights(dailySummaries.getInfectiousnessWeights()).build();
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
        .addAllRevokedDayPackages(buildRevokedDayPackages(
            androidKeyDownloadParameters.getRevokedDayPackages()))
        .addAllRevokedHourPackages(buildRevokedHourPackages(
            androidKeyDownloadParameters.getRevokedHourPackages()))
        .build();
  }

  private KeyDownloadParametersIOS buildKeyDownloadParametersIos(
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
   * Fetches the source configuration as a ApplicationConfigurationAndroid instance.
   *
   * @param distributionServiceConfig config attributes to retrieve
   * @return iOS configuration
   * @throws UnableToLoadFileException if either the file access or subsequent yaml parsing fails.
   */
  @Bean
  public ApplicationConfigurationIOS createIosV2Configuration(DistributionServiceConfig distributionServiceConfig)
      throws UnableToLoadFileException {

    RiskCalculationParameters.Builder riskCalculationParameterBuilder =
        YamlLoader.loadYamlIntoProtobufBuilder(V2_RISK_PARAMETERS_FILE,
            RiskCalculationParameters.Builder.class);

    DeserializedExposureConfiguration exposureConfiguration = YamlLoader.loadYamlIntoClass(
        IOS_V2_EXPOSURE_CONFIGURATION_FILE, DeserializedExposureConfiguration.class);

    return ApplicationConfigurationIOS.newBuilder()
        .addAllSupportedCountries(List.of(distributionServiceConfig.getSupportedCountries()))
        .setMinVersion(buildSemanticVersion(distributionServiceConfig.getAppVersions().getMinIos()))
        .setLatestVersion(buildSemanticVersion(distributionServiceConfig.getAppVersions().getLatestIos()))
        .setRiskCalculationParameters(riskCalculationParameterBuilder)
        .setAppFeatures(buildAppFeatures(distributionServiceConfig))
        .setExposureConfiguration(
            buildExposureConfigurationFromDeserializedExposureConfiguration(exposureConfiguration))
        .setKeyDownloadParameters(buildKeyDownloadParametersIos(distributionServiceConfig))
        .setExposureDetectionParameters(buildExposureDetectionParametersIos(distributionServiceConfig))
        .setEventDrivenUserSurveyParameters(buildIosEdusParameters(distributionServiceConfig))
        .setPrivacyPreservingAnalyticsParameters(buildIosPpaParameters(distributionServiceConfig))
        .build();
  }

  private PPDDPrivacyPreservingAnalyticsParametersIOS buildIosPpaParameters(
      DistributionServiceConfig distributionServiceConfig) {
    IosPrivacyPreservingAnalyticsParameters iosPpaParams = distributionServiceConfig
        .getAppConfigParameters().getIosPrivacyPreservingAnalyticsParameters();
    return PPDDPrivacyPreservingAnalyticsParametersIOS.newBuilder()
        .setCommon(PPDDPrivacyPreservingAnalyticsParametersCommon.newBuilder()
            .setProbabilityToSubmit(iosPpaParams.getProbabilityToSubmit())
            .setProbabilityToSubmitExposureWindows(iosPpaParams.getProbabilityToSubmitExposureWindows())
            .setHoursSinceTestRegistrationToSubmitTestResultMetadata(
                iosPpaParams.getHoursSinceTestRegistrationToSubmitTestResultMetadata())
            .setHoursSinceTestResultToSubmitKeySubmissionMetadata(
                iosPpaParams.getHoursSinceTestToSubmitKeySubmissionMetadata())

            .build())
        .build();
  }

  private PPDDEventDrivenUserSurveyParametersIOS buildIosEdusParameters(
      DistributionServiceConfig distributionServiceConfig) {
    IosEventDrivenUserSurveyParameters iosEdusParams = distributionServiceConfig
        .getAppConfigParameters().getIosEventDrivenUserSurveyParameters();
    return PPDDEventDrivenUserSurveyParametersIOS.newBuilder()
        .setCommon(PPDDEventDrivenUserSurveyParametersCommon.newBuilder()
            .setOtpQueryParameterName(iosEdusParams.getOtpQueryParameterName())
            .setSurveyOnHighRiskEnabled(iosEdusParams.getSurveyOnHighRiskEnabled())
            .setSurveyOnHighRiskUrl(iosEdusParams.getSurveyOnHighRiskUrl())
            .build())
        .build();
  }

  private ExposureConfiguration buildExposureConfigurationFromDeserializedExposureConfiguration(
      DeserializedExposureConfiguration deserializedExposureConfiguration) {
    return ExposureConfiguration.newBuilder()
        .addAllAttenuationDurationThresholds(deserializedExposureConfiguration.getAttenuationDurationThresholds())
        .putAllInfectiousnessForDaysSinceOnsetOfSymptoms(deserializedExposureConfiguration
            .getInfectiousnessForDaysSinceOnsetOfSymptoms())
        .setReportTypeNoneMap(deserializedExposureConfiguration.getReportTypeNoneMap())
        .setImmediateDurationWeight(deserializedExposureConfiguration.getImmediateDurationWeight())
        .setMediumDurationWeight(deserializedExposureConfiguration.getMediumDurationWeight())
        .setNearDurationWeight(deserializedExposureConfiguration.getNearDurationWeight())
        .setOtherDurationWeight(deserializedExposureConfiguration.getOtherDurationWeight())
        .setDaysSinceLastExposureThreshold(deserializedExposureConfiguration.getDaysSinceLastExposureThreshold())
        .setInfectiousnessStandardWeight(deserializedExposureConfiguration.getInfectiousnessStandardWeight())
        .setInfectiousnessHighWeight(deserializedExposureConfiguration.getInfectiousnessHighWeight())
        .setReportTypeConfirmedTestWeight(deserializedExposureConfiguration.getReportTypeConfirmedTestWeight())
        .setReportTypeConfirmedClinicalDiagnosisWeight(deserializedExposureConfiguration
            .getReportTypeConfirmedClinicalDiagnosisWeight())
        .setReportTypeSelfReportedWeight(deserializedExposureConfiguration.getReportTypeSelfReportedWeight())
        .setReportTypeRecursiveWeight(deserializedExposureConfiguration.getReportTypeRecursiveWeight())
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

  private ExposureDetectionParametersIOS buildExposureDetectionParametersIos(
      DistributionServiceConfig distributionServiceConfig) {
    IosExposureDetectionParameters iosExposureDetectionParameters =
        distributionServiceConfig.getAppConfigParameters().getIosExposureDetectionParameters();
    return ExposureDetectionParametersIOS.newBuilder()
        .setMaxExposureDetectionsPerInterval(iosExposureDetectionParameters.getMaxExposureDetectionsPerInterval())
        .build();
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
