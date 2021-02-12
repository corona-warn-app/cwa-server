package app.coronawarn.server.services.distribution.config;

import app.coronawarn.server.common.protocols.external.exposurenotification.SignatureInfo;
import app.coronawarn.server.services.distribution.utils.SerializationUtils;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "services.distribution")
@Validated
public class DistributionServiceConfig {

  private static final String PATH_REGEX = "^[/]?[a-zA-Z0-9_]{1,1024}(/[a-zA-Z0-9_]{1,1024}){0,256}[/]?$";
  private static final String FILE_NAME_REGEX = "^[a-zA-Z0-9_-]{1,1024}$";
  private static final String FILE_NAME_WITH_TYPE_REGEX = "^[a-zA-Z0-9_-]{1,1024}\\.[a-z]{1,64}$";
  private static final String CHAR_AND_NUMBER_REGEX = "^[a-zA-Z0-9_-]{1,1024}$";
  private static final String CHAR_NUMBER_AND_SPACE_REGEX = "^[a-zA-Z0-9_\\s]{1,32}$";
  private static final String NO_WHITESPACE_REGEX = "^[\\S]+$";
  private static final String URL_REGEX = "^http[s]?://[a-zA-Z0-9-]{1,1024}([\\./][a-zA-Z0-9-]{1,1024}){0,256}[/]?$";
  private static final String NUMBER_REGEX = "^[0-9]{1,256}$";
  private static final String VERSION_REGEX = "^v[0-9]{1,256}$";
  private static final String ALGORITHM_OID_REGEX = "^[0-9]{1,256}[\\.[0-9]{1,256}]{0,256}$";
  private static final String BUNDLE_REGEX = "^[a-z-]{1,256}[\\.[a-z-]{1,256}]{0,256}$";
  private static final String PRIVATE_KEY_REGEX =
      "^(classpath:|file:[/]{1,8})[a-zA-Z0-9_-]{1,256}[/[a-zA-Z0-9_-]{1,256}]{0,256}(.pem)?$";

  private Paths paths;
  private TestData testData;
  @Min(0)
  @Max(28)
  private Integer retentionDays;
  @Min(120)
  @Max(720)
  private Integer expiryPolicyMinutes;
  @Min(0)
  @Max(200)
  private Integer shiftingPolicyThreshold;
  @Min(600000)
  @Max(750000)
  private Integer maximumNumberOfKeysPerBundle;
  @Pattern(regexp = FILE_NAME_REGEX)
  private String outputFileName;
  private Boolean includeIncompleteDays;
  private Boolean includeIncompleteHours;
  private String euPackageName;
  private Boolean applyPoliciesForAllCountries;
  private String cardIdSequence;
  private TekExport tekExport;
  private Signature signature;
  private Api api;
  private ObjectStore objectStore;
  private List<AppFeature> appFeatures;
  @NotEmpty
  private String[] supportedCountries;
  private AppVersions appVersions;
  private AppConfigParameters appConfigParameters;
  private StatisticsConfig statistics;

  public Paths getPaths() {
    return paths;
  }

  public void setPaths(Paths paths) {
    this.paths = paths;
  }

  public TestData getTestData() {
    return testData;
  }

  public void setTestData(TestData testData) {
    this.testData = testData;
  }

  public Integer getRetentionDays() {
    return retentionDays;
  }

  public void setRetentionDays(Integer retentionDays) {
    this.retentionDays = retentionDays;
  }

  public Integer getExpiryPolicyMinutes() {
    return expiryPolicyMinutes;
  }

  public void setExpiryPolicyMinutes(Integer expiryPolicyMinutes) {
    this.expiryPolicyMinutes = expiryPolicyMinutes;
  }

  public Integer getShiftingPolicyThreshold() {
    return shiftingPolicyThreshold;
  }

  public void setShiftingPolicyThreshold(Integer shiftingPolicyThreshold) {
    this.shiftingPolicyThreshold = shiftingPolicyThreshold;
  }

  public Integer getMaximumNumberOfKeysPerBundle() {
    return this.maximumNumberOfKeysPerBundle;
  }

  public void setMaximumNumberOfKeysPerBundle(Integer maximumNumberOfKeysPerBundle) {
    this.maximumNumberOfKeysPerBundle = maximumNumberOfKeysPerBundle;
  }

  public String getOutputFileName() {
    return outputFileName;
  }

  public void setOutputFileName(String outputFileName) {
    this.outputFileName = outputFileName;
  }

  public Boolean getIncludeIncompleteDays() {
    return includeIncompleteDays;
  }

  public void setIncludeIncompleteDays(Boolean includeIncompleteDays) {
    this.includeIncompleteDays = includeIncompleteDays;
  }

  public Boolean getIncludeIncompleteHours() {
    return includeIncompleteHours;
  }

  public void setIncludeIncompleteHours(Boolean includeIncompleteHours) {
    this.includeIncompleteHours = includeIncompleteHours;
  }

  public String getEuPackageName() {
    return euPackageName;
  }

  public void setEuPackageName(String euPackageName) {
    this.euPackageName = euPackageName;
  }

  public Boolean getApplyPoliciesForAllCountries() {
    return applyPoliciesForAllCountries;
  }

  public void setApplyPoliciesForAllCountries(Boolean applyPoliciesForAllCountries) {
    this.applyPoliciesForAllCountries = applyPoliciesForAllCountries;
  }

  public String getCardIdSequence() {
    return cardIdSequence;
  }

  public void setCardIdSequence(String cardIdSequence) {
    this.cardIdSequence = cardIdSequence;
  }

  public TekExport getTekExport() {
    return tekExport;
  }

  public void setTekExport(TekExport tekExport) {
    this.tekExport = tekExport;
  }

  public Signature getSignature() {
    return signature;
  }

  public void setSignature(Signature signature) {
    this.signature = signature;
  }

  public Api getApi() {
    return api;
  }

  public void setApi(Api api) {
    this.api = api;
  }

  public ObjectStore getObjectStore() {
    return objectStore;
  }

  public void setObjectStore(
      ObjectStore objectStore) {
    this.objectStore = objectStore;
  }

  public List<AppFeature> getAppFeatures() {
    return appFeatures;
  }

  public void setAppFeatures(List<AppFeature> appFeatures) {
    this.appFeatures = appFeatures;
  }

  public String[] getSupportedCountries() {
    return supportedCountries;
  }

  public void setSupportedCountries(String[] supportedCountries) {
    this.supportedCountries = supportedCountries;
  }

  public AppVersions getAppVersions() {
    return appVersions;
  }

  public void setAppVersions(AppVersions appVersions) {
    this.appVersions = appVersions;
  }

  public AppConfigParameters getAppConfigParameters() {
    return appConfigParameters;
  }

  public void setAppConfigParameters(AppConfigParameters appConfigParameters) {
    this.appConfigParameters = appConfigParameters;
  }

  public StatisticsConfig getStatistics() {
    return statistics;
  }

  public void setStatistics(StatisticsConfig statistics) {
    this.statistics = statistics;
  }

  /**
   * Get app features as list of protobuf objects.
   *
   * @return list of {@link app.coronawarn.server.common.protocols.internal.AppFeature}
   */
  public List<app.coronawarn.server.common.protocols.internal.AppFeature> getAppFeaturesProto() {
    return getAppFeatures().stream()
        .map(appFeature -> app.coronawarn.server.common.protocols.internal.AppFeature.newBuilder()
            .setLabel(appFeature.getLabel())
            .setValue(appFeature.getValue()).build())
        .collect(Collectors.toList());
  }

  public static class StatisticsConfig {

    private Double trendCalculationThreshold;

    private String statisticPath;

    private String accessKey;

    private String secretKey;

    private String endpoint;

    private String bucket;

    public String getStatisticPath() {
      return statisticPath;
    }

    public void setStatisticPath(String statisticPath) {
      this.statisticPath = statisticPath;
    }

    public Double getTrendCalculationThreshold() {
      return trendCalculationThreshold;
    }

    public void setTrendCalculationThreshold(Double trendCalculationThreshold) {
      this.trendCalculationThreshold = trendCalculationThreshold;
    }

    public String getAccessKey() {
      return accessKey;
    }

    public void setAccessKey(String accessKey) {
      this.accessKey = accessKey;
    }

    public String getSecretKey() {
      return secretKey;
    }

    public void setSecretKey(String secretKey) {
      this.secretKey = secretKey;
    }

    public String getEndpoint() {
      return endpoint;
    }

    public void setEndpoint(String endpoint) {
      this.endpoint = endpoint;
    }

    public String getBucket() {
      return bucket;
    }

    public void setBucket(String bucket) {
      this.bucket = bucket;
    }
  }

  public static class TekExport {

    @Pattern(regexp = FILE_NAME_WITH_TYPE_REGEX)
    private String fileName;
    @Pattern(regexp = CHAR_NUMBER_AND_SPACE_REGEX)
    private String fileHeader;
    @Min(0)
    @Max(32)
    private Integer fileHeaderWidth;

    public String getFileName() {
      return fileName;
    }

    public void setFileName(String fileName) {
      this.fileName = fileName;
    }

    public String getFileHeader() {
      return fileHeader;
    }

    public void setFileHeader(String fileHeader) {
      this.fileHeader = fileHeader;
    }

    public Integer getFileHeaderWidth() {
      return fileHeaderWidth;
    }

    public void setFileHeaderWidth(Integer fileHeaderWidth) {
      this.fileHeaderWidth = fileHeaderWidth;
    }
  }

  public static class TestData {

    private Integer seed;
    private Integer exposuresPerHour;
    private boolean distributionTestdataConsentToFederation;

    public Integer getSeed() {
      return seed;
    }

    public void setSeed(Integer seed) {
      this.seed = seed;
    }

    public Integer getExposuresPerHour() {
      return exposuresPerHour;
    }

    public void setExposuresPerHour(Integer exposuresPerHour) {
      this.exposuresPerHour = exposuresPerHour;
    }

    public boolean getDistributionTestdataConsentToFederation() {
      return distributionTestdataConsentToFederation;
    }

    public void setDistributionTestdataConsentToFederation(boolean distributionTestdataConsentToFederation) {
      this.distributionTestdataConsentToFederation = distributionTestdataConsentToFederation;
    }
  }

  public static class Paths {

    @Pattern(regexp = PRIVATE_KEY_REGEX)
    private String privateKey;
    @Pattern(regexp = PATH_REGEX)
    private String output;

    public String getPrivateKey() {
      return privateKey;
    }

    public void setPrivateKey(String privateKey) {
      this.privateKey = privateKey;
    }

    public String getOutput() {
      return output;
    }

    public void setOutput(String output) {
      this.output = output;
    }
  }

  public static class Api {

    @Pattern(regexp = CHAR_AND_NUMBER_REGEX)
    private String versionPath;
    @Pattern(regexp = VERSION_REGEX)
    private String versionV1;
    @Pattern(regexp = CHAR_AND_NUMBER_REGEX)
    private String countryPath;
    @Pattern(regexp = CHAR_AND_NUMBER_REGEX)
    private String originCountry;
    @Pattern(regexp = CHAR_AND_NUMBER_REGEX)
    private String datePath;
    @Pattern(regexp = CHAR_AND_NUMBER_REGEX)
    private String hourPath;
    @Pattern(regexp = CHAR_AND_NUMBER_REGEX)
    private String diagnosisKeysPath;
    @Pattern(regexp = CHAR_AND_NUMBER_REGEX)
    private String parametersPath;
    @Pattern(regexp = CHAR_AND_NUMBER_REGEX)
    private String appConfigFileName;
    @Pattern(regexp = CHAR_AND_NUMBER_REGEX)
    private String appConfigV2IosFileName;
    @Pattern(regexp = CHAR_AND_NUMBER_REGEX)
    private String appConfigV2AndroidFileName;
    @Pattern(regexp = CHAR_AND_NUMBER_REGEX)
    private String statisticsFileName;

    public String getStatisticsFileName() {
      return statisticsFileName;
    }

    public void setStatisticsFileName(String statisticsFileName) {
      this.statisticsFileName = statisticsFileName;
    }

    public String getAppConfigV2IosFileName() {
      return appConfigV2IosFileName;
    }

    public void setAppConfigV2IosFileName(String appConfigV2IosFileName) {
      this.appConfigV2IosFileName = appConfigV2IosFileName;
    }

    public String getAppConfigV2AndroidFileName() {
      return appConfigV2AndroidFileName;
    }

    public void setAppConfigV2AndroidFileName(String appConfigV2AndroidFileName) {
      this.appConfigV2AndroidFileName = appConfigV2AndroidFileName;
    }

    public String getVersionPath() {
      return versionPath;
    }

    public void setVersionPath(String versionPath) {
      this.versionPath = versionPath;
    }

    public String getVersionV1() {
      return versionV1;
    }

    public void setVersionV1(String versionV1) {
      this.versionV1 = versionV1;
    }

    public String getCountryPath() {
      return countryPath;
    }

    public void setCountryPath(String countryPath) {
      this.countryPath = countryPath;
    }

    public String getDatePath() {
      return datePath;
    }

    public void setDatePath(String datePath) {
      this.datePath = datePath;
    }

    public String getHourPath() {
      return hourPath;
    }

    public void setHourPath(String hourPath) {
      this.hourPath = hourPath;
    }

    public String getDiagnosisKeysPath() {
      return diagnosisKeysPath;
    }

    public void setDiagnosisKeysPath(String diagnosisKeysPath) {
      this.diagnosisKeysPath = diagnosisKeysPath;
    }

    public String getParametersPath() {
      return parametersPath;
    }

    public void setParametersPath(String parametersPath) {
      this.parametersPath = parametersPath;
    }

    public String getAppConfigFileName() {
      return appConfigFileName;
    }

    public void setAppConfigFileName(String appConfigFileName) {
      this.appConfigFileName = appConfigFileName;
    }

    public String getOriginCountry() {
      return originCountry;
    }

    public void setOriginCountry(String originCountry) {
      this.originCountry = originCountry;
    }
  }

  public static class Signature {

    @Pattern(regexp = BUNDLE_REGEX)
    private String appBundleId;
    private String androidPackage;
    @Pattern(regexp = NUMBER_REGEX)
    private String verificationKeyId;
    @Pattern(regexp = VERSION_REGEX)
    private String verificationKeyVersion;
    @Pattern(regexp = ALGORITHM_OID_REGEX)
    private String algorithmOid;
    @Pattern(regexp = CHAR_AND_NUMBER_REGEX)
    private String algorithmName;
    @Pattern(regexp = FILE_NAME_WITH_TYPE_REGEX)
    private String fileName;
    @Pattern(regexp = CHAR_AND_NUMBER_REGEX)
    private String securityProvider;

    public String getAppBundleId() {
      return appBundleId;
    }

    public void setAppBundleId(String appBundleId) {
      this.appBundleId = appBundleId;
    }

    public String getAndroidPackage() {
      return androidPackage;
    }

    public void setAndroidPackage(String androidPackage) {
      this.androidPackage = androidPackage;
    }

    public String getVerificationKeyId() {
      return verificationKeyId;
    }

    public void setVerificationKeyId(String verificationKeyId) {
      this.verificationKeyId = verificationKeyId;
    }

    public String getVerificationKeyVersion() {
      return verificationKeyVersion;
    }

    public void setVerificationKeyVersion(String verificationKeyVersion) {
      this.verificationKeyVersion = verificationKeyVersion;
    }

    public String getAlgorithmOid() {
      return algorithmOid;
    }

    public void setAlgorithmOid(String algorithmOid) {
      this.algorithmOid = algorithmOid;
    }

    public String getAlgorithmName() {
      return algorithmName;
    }

    public void setAlgorithmName(String algorithmName) {
      this.algorithmName = algorithmName;
    }

    public String getFileName() {
      return fileName;
    }

    public void setFileName(String fileName) {
      this.fileName = fileName;
    }

    public String getSecurityProvider() {
      return securityProvider;
    }

    public void setSecurityProvider(String securityProvider) {
      this.securityProvider = securityProvider;
    }

    /**
     * Returns the static {@link SignatureInfo} configured in the application properties.
     *
     * @return SignatureInfo
     */
    public SignatureInfo getSignatureInfo() {
      return SignatureInfo.newBuilder()
          .setAppBundleId(this.getAppBundleId())
          .setVerificationKeyVersion(this.getVerificationKeyVersion())
          .setVerificationKeyId(this.getVerificationKeyId())
          .setSignatureAlgorithm(this.getAlgorithmOid())
          .build();
    }
  }

  public static class ObjectStore {

    @Pattern(regexp = NO_WHITESPACE_REGEX)
    private String accessKey;
    @Pattern(regexp = NO_WHITESPACE_REGEX)
    private String secretKey;
    @Pattern(regexp = URL_REGEX)
    private String endpoint;
    @Min(1)
    @Max(65535)
    private Integer port;
    @Pattern(regexp = CHAR_AND_NUMBER_REGEX)
    private String bucket;
    private Boolean setPublicReadAclOnPutObject;
    @Min(1)
    @Max(64)
    private Integer maxNumberOfFailedOperations;
    @Min(1)
    @Max(64)
    private Integer maxNumberOfS3Threads;
    private Boolean forceUpdateKeyfiles;
    @Max(Integer.MAX_VALUE)
    private Integer hourFileRetentionDays;

    public String getAccessKey() {
      return accessKey;
    }

    public void setAccessKey(String accessKey) {
      this.accessKey = accessKey;
    }

    public String getSecretKey() {
      return secretKey;
    }

    public void setSecretKey(String secretKey) {
      this.secretKey = secretKey;
    }

    public String getEndpoint() {
      return endpoint;
    }

    public void setEndpoint(String endpoint) {
      this.endpoint = endpoint;
    }

    public Integer getPort() {
      return port;
    }

    public void setPort(Integer port) {
      this.port = port;
    }

    public String getBucket() {
      return bucket;
    }

    public void setBucket(String bucket) {
      this.bucket = bucket;
    }

    public Boolean isSetPublicReadAclOnPutObject() {
      return setPublicReadAclOnPutObject;
    }

    public void setSetPublicReadAclOnPutObject(Boolean setPublicReadAclOnPutObject) {
      this.setPublicReadAclOnPutObject = setPublicReadAclOnPutObject;
    }

    public Integer getMaxNumberOfFailedOperations() {
      return maxNumberOfFailedOperations;
    }

    public void setMaxNumberOfFailedOperations(Integer maxNumberOfFailedOperations) {
      this.maxNumberOfFailedOperations = maxNumberOfFailedOperations;
    }

    public Integer getMaxNumberOfS3Threads() {
      return maxNumberOfS3Threads;
    }

    public void setMaxNumberOfS3Threads(Integer maxNumberOfS3Threads) {
      this.maxNumberOfS3Threads = maxNumberOfS3Threads;
    }

    public Boolean getForceUpdateKeyfiles() {
      return forceUpdateKeyfiles;
    }

    public void setForceUpdateKeyfiles(Boolean forceUpdateKeyfiles) {
      this.forceUpdateKeyfiles = forceUpdateKeyfiles;
    }

    public Integer getHourFileRetentionDays() {
      return hourFileRetentionDays;
    }

    public void setHourFileRetentionDays(Integer hourFileRetentionDays) {
      this.hourFileRetentionDays = hourFileRetentionDays;
    }
  }

  public static class AppFeature {

    private String label;
    private Integer value;

    public String getLabel() {
      return label;
    }

    public void setLabel(String label) {
      this.label = label;
    }

    public Integer getValue() {
      return value;
    }

    public void setValue(Integer value) {
      this.value = value;
    }
  }

  public static class AppVersions {

    private String latestIos;
    private String minIos;
    private String latestAndroid;
    private String minAndroid;
    @PositiveOrZero
    private Integer latestAndroidVersionCode;
    @PositiveOrZero
    private Integer minAndroidVersionCode;

    public String getLatestIos() {
      return latestIos;
    }

    public void setLatestIos(String latestIos) {
      this.latestIos = latestIos;
    }

    public String getMinIos() {
      return minIos;
    }

    public void setMinIos(String minIos) {
      this.minIos = minIos;
    }

    public String getLatestAndroid() {
      return latestAndroid;
    }

    public void setLatestAndroid(String latestAndroid) {
      this.latestAndroid = latestAndroid;
    }

    public String getMinAndroid() {
      return minAndroid;
    }

    public void setMinAndroid(String minAndroid) {
      this.minAndroid = minAndroid;
    }

    public Integer getLatestAndroidVersionCode() {
      return latestAndroidVersionCode;
    }

    public void setLatestAndroidVersionCode(Integer latestAndroidVersionCode) {
      this.latestAndroidVersionCode = latestAndroidVersionCode;
    }

    public Integer getMinAndroidVersionCode() {
      return minAndroidVersionCode;
    }

    public void setMinAndroidVersionCode(Integer minAndroidVersionCode) {
      this.minAndroidVersionCode = minAndroidVersionCode;
    }
  }

  public static class AppConfigParameters {

    private IosKeyDownloadParameters iosKeyDownloadParameters;
    private AndroidKeyDownloadParameters androidKeyDownloadParameters;
    private IosExposureDetectionParameters iosExposureDetectionParameters;
    private AndroidExposureDetectionParameters androidExposureDetectionParameters;
    private IosEventDrivenUserSurveyParameters iosEventDrivenUserSurveyParameters;
    private AndroidEventDrivenUserSurveyParameters androidEventDrivenUserSurveyParameters;
    private IosPrivacyPreservingAnalyticsParameters iosPrivacyPreservingAnalyticsParameters;
    private AndroidPrivacyPreservingAnalyticsParameters androidPrivacyPreservingAnalyticsParameters;

    public IosEventDrivenUserSurveyParameters getIosEventDrivenUserSurveyParameters() {
      return iosEventDrivenUserSurveyParameters;
    }

    public void setIosEventDrivenUserSurveyParameters(
        IosEventDrivenUserSurveyParameters iosEventDrivenUserSurveyParameters) {
      this.iosEventDrivenUserSurveyParameters = iosEventDrivenUserSurveyParameters;
    }

    public AndroidEventDrivenUserSurveyParameters getAndroidEventDrivenUserSurveyParameters() {
      return androidEventDrivenUserSurveyParameters;
    }

    public void setAndroidEventDrivenUserSurveyParameters(
        AndroidEventDrivenUserSurveyParameters androidEventDrivenUserSurveyParameters) {
      this.androidEventDrivenUserSurveyParameters = androidEventDrivenUserSurveyParameters;
    }

    public IosPrivacyPreservingAnalyticsParameters getIosPrivacyPreservingAnalyticsParameters() {
      return iosPrivacyPreservingAnalyticsParameters;
    }

    public void setIosPrivacyPreservingAnalyticsParameters(
        IosPrivacyPreservingAnalyticsParameters iosPrivacyPreservingAnalyticsParameters) {
      this.iosPrivacyPreservingAnalyticsParameters = iosPrivacyPreservingAnalyticsParameters;
    }

    public AndroidPrivacyPreservingAnalyticsParameters getAndroidPrivacyPreservingAnalyticsParameters() {
      return androidPrivacyPreservingAnalyticsParameters;
    }

    public void setAndroidPrivacyPreservingAnalyticsParameters(
        AndroidPrivacyPreservingAnalyticsParameters androidPrivacyPreservingAnalyticsParameters) {
      this.androidPrivacyPreservingAnalyticsParameters = androidPrivacyPreservingAnalyticsParameters;
    }

    public IosKeyDownloadParameters getIosKeyDownloadParameters() {
      return iosKeyDownloadParameters;
    }

    public void setIosKeyDownloadParameters(IosKeyDownloadParameters iosKeyDownloadParameters) {
      this.iosKeyDownloadParameters = iosKeyDownloadParameters;
    }

    public AndroidKeyDownloadParameters getAndroidKeyDownloadParameters() {
      return androidKeyDownloadParameters;
    }

    public void setAndroidKeyDownloadParameters(AndroidKeyDownloadParameters androidKeyDownloadParameters) {
      this.androidKeyDownloadParameters = androidKeyDownloadParameters;
    }

    public IosExposureDetectionParameters getIosExposureDetectionParameters() {
      return iosExposureDetectionParameters;
    }

    public void setIosExposureDetectionParameters(IosExposureDetectionParameters iosExposureDetectionParameters) {
      this.iosExposureDetectionParameters = iosExposureDetectionParameters;
    }

    public AndroidExposureDetectionParameters getAndroidExposureDetectionParameters() {
      return androidExposureDetectionParameters;
    }

    public void setAndroidExposureDetectionParameters(
        AndroidExposureDetectionParameters androidExposureDetectionParameters) {
      this.androidExposureDetectionParameters = androidExposureDetectionParameters;
    }

    public static class AndroidKeyDownloadParameters extends CommonKeyDownloadParameters {

      private static final int LOWER_BOUNDARY_DOWNLOAD_TIMEOUT = 0;
      public static final String MIN_VALUE_ERROR_MESSAGE_DOWNLOAD_TIMEOUT =
          "Download timeout in seconds must be greater than or equal to " + LOWER_BOUNDARY_DOWNLOAD_TIMEOUT;
      private static final int UPPER_BOUNDARY_DOWNLOAD_TIMEOUT = 1800;
      public static final String MAX_VALUE_ERROR_MESSAGE_DOWNLOAD_TIMEOUT =
          "Download timeout in seconds must be lower than or equal to " + UPPER_BOUNDARY_DOWNLOAD_TIMEOUT;
      private static final int LOWER_BOUNDARY_OVERALL_TIMEOUT = 0;
      public static final String MIN_VALUE_ERROR_MESSAGE_OVERALL_TIMEOUT =
          "Android Key Download: overall timeout in seconds must be greater than or equal to "
              + LOWER_BOUNDARY_OVERALL_TIMEOUT;
      private static final int UPPER_BOUNDARY_OVERALL_TIMEOUT = 1800;
      public static final String MAX_VALUE_ERROR_MESSAGE_OVERALL_TIMEOUT =
          "Android Key Download: overall timeout in seconds must be lower than or equal to "
              + UPPER_BOUNDARY_OVERALL_TIMEOUT;

      @Min(value = LOWER_BOUNDARY_DOWNLOAD_TIMEOUT, message = MIN_VALUE_ERROR_MESSAGE_DOWNLOAD_TIMEOUT)
      @Max(value = UPPER_BOUNDARY_DOWNLOAD_TIMEOUT, message = MAX_VALUE_ERROR_MESSAGE_DOWNLOAD_TIMEOUT)
      private Integer downloadTimeoutInSeconds;
      @Min(value = LOWER_BOUNDARY_OVERALL_TIMEOUT, message = MIN_VALUE_ERROR_MESSAGE_OVERALL_TIMEOUT)
      @Max(value = UPPER_BOUNDARY_OVERALL_TIMEOUT, message = MAX_VALUE_ERROR_MESSAGE_OVERALL_TIMEOUT)
      private Integer overallTimeoutInSeconds;

      public Integer getDownloadTimeoutInSeconds() {
        return downloadTimeoutInSeconds;
      }

      public void setDownloadTimeoutInSeconds(Integer downloadTimeoutInSeconds) {
        this.downloadTimeoutInSeconds = downloadTimeoutInSeconds;
      }

      public Integer getOverallTimeoutInSeconds() {
        return overallTimeoutInSeconds;
      }

      public void setOverallTimeoutInSeconds(Integer overallTimeoutInSeconds) {
        this.overallTimeoutInSeconds = overallTimeoutInSeconds;
      }
    }

    public static class DeserializedDayPackageMetadata {

      private String region;
      private String date;
      private String etag;

      public String getRegion() {
        return region;
      }

      public String getDate() {
        return date;
      }

      public String getEtag() {
        return etag;
      }
    }

    public static class DeserializedHourPackageMetadata extends DeserializedDayPackageMetadata {

      private Integer hour;

      public Integer getHour() {
        return hour;
      }
    }

    private abstract static class CommonKeyDownloadParameters {

      private String revokedDayPackages;
      private String revokedHourPackages;

      public List<DeserializedDayPackageMetadata> getRevokedDayPackages() {
        return SerializationUtils.deserializeJson(revokedDayPackages,
            typeFactory -> typeFactory.constructCollectionType(List.class, DeserializedDayPackageMetadata.class));
      }

      public void setRevokedDayPackages(String revokedDayPackages) {
        this.revokedDayPackages = revokedDayPackages;
      }

      public List<DeserializedHourPackageMetadata> getRevokedHourPackages() {
        return SerializationUtils.deserializeJson(revokedHourPackages,
            typeFactory -> typeFactory
                .constructCollectionType(List.class, DeserializedHourPackageMetadata.class));
      }

      public void setRevokedHourPackages(String revokedHourPackages) {
        this.revokedHourPackages = revokedHourPackages;
      }
    }

    public static class IosKeyDownloadParameters extends CommonKeyDownloadParameters {

    }

    public static class IosExposureDetectionParameters {

      private static final int MIN_VALUE_MAX_EXPOSURE_DETECTIONS = 0;
      public static final String MIN_VALUE_ERROR_MESSAGE_MAX_EXPOSURE_DETECTIONS =
          "IOS Exposure Detection: max exposure detections per interval must be greater than or equal to "
              + MIN_VALUE_MAX_EXPOSURE_DETECTIONS;
      private static final int MAX_VALUE_MAX_EXPOSURE_DETECTIONS = 6;
      public static final String MAX_VALUE_ERROR_MESSAGE_MAX_EXPOSURE_DETECTIONS =
          "IOS Exposure Detection: max exposure detections per interval must be lower than or equal to "
              + MAX_VALUE_MAX_EXPOSURE_DETECTIONS;

      @Min(value = MIN_VALUE_MAX_EXPOSURE_DETECTIONS, message = MIN_VALUE_ERROR_MESSAGE_MAX_EXPOSURE_DETECTIONS)
      @Max(value = MAX_VALUE_MAX_EXPOSURE_DETECTIONS, message = MAX_VALUE_ERROR_MESSAGE_MAX_EXPOSURE_DETECTIONS)
      private Integer maxExposureDetectionsPerInterval;

      public Integer getMaxExposureDetectionsPerInterval() {
        return maxExposureDetectionsPerInterval;
      }

      public void setMaxExposureDetectionsPerInterval(Integer maxExposureDetectionsPerInterval) {
        this.maxExposureDetectionsPerInterval = maxExposureDetectionsPerInterval;
      }

    }

    public static class AndroidExposureDetectionParameters {

      private static final int LOWER_BOUNDARY_OVERALL_TIMEOUT = 0;
      public static final String MIN_VALUE_ERROR_MESSAGE_OVERALL_TIMEOUT =
          "Android Exposure Detection: overall timeout in seconds must be greater than or equal to "
              + LOWER_BOUNDARY_OVERALL_TIMEOUT;
      private static final int UPPER_BOUNDARY_OVERALL_TIMEOUT = 3600;
      public static final String MAX_VALUE_ERROR_MESSAGE_OVERALL_TIMEOUT =
          "Android Exposure Detection: overall timeout in seconds must be lower than or equal to "
              + UPPER_BOUNDARY_OVERALL_TIMEOUT;
      private static final int LOWER_BOUNDARY_MAX_EXPOSURE_DETECTIONS = 0;
      public static final String MIN_VALUE_ERROR_MESSAGE_MAX_EXPOSURE_DETECTIONS =
          "Android Exposure Detection: max exposure detections per interval must be greater than or equal to "
              + LOWER_BOUNDARY_MAX_EXPOSURE_DETECTIONS;
      private static final int UPPER_BOUNDARY_MAX_EXPOSURE_DETECTIONS = 6;
      public static final String MAX_VALUE_ERROR_MESSAGE_MAX_EXPOSURE_DETECTIONS =
          "Android Exposure Detection: max exposure detections per interval must be lower than or equal to "
              + UPPER_BOUNDARY_MAX_EXPOSURE_DETECTIONS;
      @Min(value = LOWER_BOUNDARY_MAX_EXPOSURE_DETECTIONS, message = MIN_VALUE_ERROR_MESSAGE_MAX_EXPOSURE_DETECTIONS)
      @Max(value = UPPER_BOUNDARY_MAX_EXPOSURE_DETECTIONS, message = MAX_VALUE_ERROR_MESSAGE_MAX_EXPOSURE_DETECTIONS)
      private Integer maxExposureDetectionsPerInterval;
      @Min(value = LOWER_BOUNDARY_OVERALL_TIMEOUT, message = MIN_VALUE_ERROR_MESSAGE_OVERALL_TIMEOUT)
      @Max(value = UPPER_BOUNDARY_OVERALL_TIMEOUT, message = MAX_VALUE_ERROR_MESSAGE_OVERALL_TIMEOUT)
      private Integer overallTimeoutInSeconds;

      public Integer getMaxExposureDetectionsPerInterval() {
        return maxExposureDetectionsPerInterval;
      }

      public void setMaxExposureDetectionsPerInterval(Integer maxExposureDetectionsPerInterval) {
        this.maxExposureDetectionsPerInterval = maxExposureDetectionsPerInterval;
      }

      public Integer getOverallTimeoutInSeconds() {
        return overallTimeoutInSeconds;
      }

      public void setOverallTimeoutInSeconds(Integer overallTimeoutInSeconds) {
        this.overallTimeoutInSeconds = overallTimeoutInSeconds;
      }
    }

    public static class IosEventDrivenUserSurveyParameters extends CommonEdusParameters {

    }

    private static class CommonEdusParameters {

      @Size(min = 1, max = 30)
      private String otpQueryParameterName;
      @NotNull
      private Boolean surveyOnHighRiskEnabled;
      @Pattern(regexp = URL_REGEX)
      private String surveyOnHighRiskUrl;

      public String getOtpQueryParameterName() {
        return otpQueryParameterName;
      }

      public void setOtpQueryParameterName(String otpQueryParameterName) {
        this.otpQueryParameterName = otpQueryParameterName;
      }

      public Boolean getSurveyOnHighRiskEnabled() {
        return surveyOnHighRiskEnabled;
      }

      public void setSurveyOnHighRiskEnabled(Boolean surveyOnHighRiskEnabled) {
        this.surveyOnHighRiskEnabled = surveyOnHighRiskEnabled;
      }

      public String getSurveyOnHighRiskUrl() {
        return surveyOnHighRiskUrl;
      }

      public void setSurveyOnHighRiskUrl(String surveyOnHighRiskUrl) {
        this.surveyOnHighRiskUrl = surveyOnHighRiskUrl;
      }
    }

    public static class AndroidEventDrivenUserSurveyParameters extends CommonEdusParameters {

      @NotNull
      private Boolean requireBasicIntegrity;
      @NotNull
      private Boolean requireCtsProfileMatch;
      @NotNull
      private Boolean requireEvaluationTypeBasic;
      @NotNull
      private Boolean requireEvaluationTypeHardwareBacked;

      public Boolean getRequireBasicIntegrity() {
        return requireBasicIntegrity;
      }

      public void setRequireBasicIntegrity(Boolean requireBasicIntegrity) {
        this.requireBasicIntegrity = requireBasicIntegrity;
      }

      public Boolean getRequireCtsProfileMatch() {
        return requireCtsProfileMatch;
      }

      public void setRequireCtsProfileMatch(Boolean requireCtsProfileMatch) {
        this.requireCtsProfileMatch = requireCtsProfileMatch;
      }

      public Boolean getRequireEvaluationTypeBasic() {
        return requireEvaluationTypeBasic;
      }

      public void setRequireEvaluationTypeBasic(Boolean requireEvaluationTypeBasic) {
        this.requireEvaluationTypeBasic = requireEvaluationTypeBasic;
      }

      public Boolean getRequireEvaluationTypeHardwareBacked() {
        return requireEvaluationTypeHardwareBacked;
      }

      public void setRequireEvaluationTypeHardwareBacked(Boolean requireEvaluationTypeHardwareBacked) {
        this.requireEvaluationTypeHardwareBacked = requireEvaluationTypeHardwareBacked;
      }
    }

    public static class IosPrivacyPreservingAnalyticsParameters extends CommonPpaParameters {

    }

    private static class CommonPpaParameters {

      private Double probabilityToSubmit;
      private Double probabilityToSubmitExposureWindows;
      @PositiveOrZero
      private Integer hoursSinceTestRegistrationToSubmitTestResultMetadata;
      @PositiveOrZero
      private Integer hoursSinceTestToSubmitKeySubmissionMetadata;

      public Double getProbabilityToSubmit() {
        return probabilityToSubmit;
      }

      public void setProbabilityToSubmit(Double probabilityToSubmit) {
        this.probabilityToSubmit = probabilityToSubmit;
      }

      public Double getProbabilityToSubmitExposureWindows() {
        return probabilityToSubmitExposureWindows;
      }

      public void setProbabilityToSubmitExposureWindows(Double probabilityToSubmitExposureWindows) {
        this.probabilityToSubmitExposureWindows = probabilityToSubmitExposureWindows;
      }

      public Integer getHoursSinceTestRegistrationToSubmitTestResultMetadata() {
        return hoursSinceTestRegistrationToSubmitTestResultMetadata;
      }

      public void setHoursSinceTestRegistrationToSubmitTestResultMetadata(
          Integer hoursSinceTestRegistrationToSubmitTestResultMetadata) {
        this.hoursSinceTestRegistrationToSubmitTestResultMetadata =
            hoursSinceTestRegistrationToSubmitTestResultMetadata;
      }

      public Integer getHoursSinceTestToSubmitKeySubmissionMetadata() {
        return hoursSinceTestToSubmitKeySubmissionMetadata;
      }

      public void setHoursSinceTestToSubmitKeySubmissionMetadata(Integer hoursSinceTestToSubmitKeySubmissionMetadata) {
        this.hoursSinceTestToSubmitKeySubmissionMetadata = hoursSinceTestToSubmitKeySubmissionMetadata;
      }
    }

    public static class AndroidPrivacyPreservingAnalyticsParameters extends CommonPpaParameters {

      @NotNull
      private Boolean requireBasicIntegrity;
      @NotNull
      private Boolean requireCtsProfileMatch;
      @NotNull
      private Boolean requireEvaluationTypeBasic;
      @NotNull
      private Boolean requireEvaluationTypeHardwareBacked;

      public Boolean getRequireBasicIntegrity() {
        return requireBasicIntegrity;
      }

      public void setRequireBasicIntegrity(Boolean requireBasicIntegrity) {
        this.requireBasicIntegrity = requireBasicIntegrity;
      }

      public Boolean getRequireCtsProfileMatch() {
        return requireCtsProfileMatch;
      }

      public void setRequireCtsProfileMatch(Boolean requireCtsProfileMatch) {
        this.requireCtsProfileMatch = requireCtsProfileMatch;
      }

      public Boolean getRequireEvaluationTypeBasic() {
        return requireEvaluationTypeBasic;
      }

      public void setRequireEvaluationTypeBasic(Boolean requireEvaluationTypeBasic) {
        this.requireEvaluationTypeBasic = requireEvaluationTypeBasic;
      }

      public Boolean getRequireEvaluationTypeHardwareBacked() {
        return requireEvaluationTypeHardwareBacked;
      }

      public void setRequireEvaluationTypeHardwareBacked(Boolean requireEvaluationTypeHardwareBacked) {
        this.requireEvaluationTypeHardwareBacked = requireEvaluationTypeHardwareBacked;
      }
    }

  }
}
