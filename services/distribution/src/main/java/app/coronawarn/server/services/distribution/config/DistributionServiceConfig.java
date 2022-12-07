package app.coronawarn.server.services.distribution.config;

import app.coronawarn.server.common.protocols.external.exposurenotification.SignatureInfo;
import app.coronawarn.server.common.shared.util.SerializationUtils;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "services.distribution")
@Validated
public class DistributionServiceConfig {

  public static class AllowList {

    @JsonInclude(Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CertificateAllowList {

      private String serviceProvider;
      private String hostname;
      private String fingerprint256;

      @JsonProperty("serviceProviderAllowlistEndpoint")
      private String serviceProviderAllowlistEndpoint;

      public String getFingerprint256() {
        return fingerprint256;
      }

      public String getHostname() {
        return hostname;
      }

      public String getServiceProvider() {
        return serviceProvider;
      }

      public String getServiceProviderAllowlistEndpoint() {
        return serviceProviderAllowlistEndpoint;
      }

      @JsonProperty("fingerprint256")
      public void setFingerprint256(final String fingerprint256) {
        this.fingerprint256 = fingerprint256;
      }

      @JsonProperty("hostname")
      public void setHostname(final String hostname) {
        this.hostname = hostname;
      }

      @JsonProperty("serviceProvider")
      public void setServiceProvider(final String serviceProvider) {
        this.serviceProvider = serviceProvider;
      }

      public void setServiceProviderAllowlistEndpoint(final String serviceProviderAllowlistEndpoint) {
        this.serviceProviderAllowlistEndpoint = serviceProviderAllowlistEndpoint;
      }
    }

    @JsonInclude(Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ServiceProvider {

      @JsonProperty("serviceProviderAllowlistEndpoint")
      private String serviceProviderAllowlistEndpoint;
      @JsonProperty("fingerprint256")
      private String fingerprint256;

      public String getFingerprint256() {
        return fingerprint256;
      }

      public String getServiceProviderAllowlistEndpoint() {
        return serviceProviderAllowlistEndpoint;
      }

      public void setFingerprint256(final String fingerprint256) {
        this.fingerprint256 = fingerprint256;
      }

      public void setServiceProviderAllowlistEndpoint(final String serviceProviderAllowlistEndpoint) {
        this.serviceProviderAllowlistEndpoint = serviceProviderAllowlistEndpoint;
      }
    }

    private List<CertificateAllowList> certificates;

    private List<ServiceProvider> serviceProviders;

    public List<CertificateAllowList> getCertificates() {
      return certificates;
    }

    public List<ServiceProvider> getServiceProviders() {
      return serviceProviders;
    }

    @JsonProperty("certificates")
    public void setCertificates(final List<CertificateAllowList> certificates) {
      this.certificates = certificates;
    }

    @JsonProperty("serviceProviders")
    public void setServiceProviders(final List<ServiceProvider> serviceProviders) {
      this.serviceProviders = serviceProviders;
    }
  }

  public static class Api {

    @Pattern(regexp = CHAR_AND_NUMBER_REGEX)
    private String versionPath;
    @Pattern(regexp = VERSION_REGEX)
    private String versionV1;
    @Pattern(regexp = VERSION_REGEX)
    private String versionV2;
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
    @Pattern(regexp = CHAR_AND_NUMBER_REGEX)
    private String localStatisticsFileName;
    @Pattern(regexp = CHAR_AND_NUMBER_REGEX)
    private String traceWarningsPath;

    public String getAppConfigFileName() {
      return appConfigFileName;
    }

    public String getAppConfigV2AndroidFileName() {
      return appConfigV2AndroidFileName;
    }

    public String getAppConfigV2IosFileName() {
      return appConfigV2IosFileName;
    }

    public String getCountryPath() {
      return countryPath;
    }

    public String getDatePath() {
      return datePath;
    }

    public String getDiagnosisKeysPath() {
      return diagnosisKeysPath;
    }

    public String getHourPath() {
      return hourPath;
    }

    public String getLocalStatisticsFileName() {
      return localStatisticsFileName;
    }

    public String getOriginCountry() {
      return originCountry;
    }

    public String getParametersPath() {
      return parametersPath;
    }

    public String getStatisticsFileName() {
      return statisticsFileName;
    }

    public String getTraceWarningsPath() {
      return traceWarningsPath;
    }

    public String getVersionPath() {
      return versionPath;
    }

    public String getVersionV1() {
      return versionV1;
    }

    public String getVersionV2() {
      return versionV2;
    }

    public void setAppConfigFileName(final String appConfigFileName) {
      this.appConfigFileName = appConfigFileName;
    }

    public void setAppConfigV2AndroidFileName(final String appConfigV2AndroidFileName) {
      this.appConfigV2AndroidFileName = appConfigV2AndroidFileName;
    }

    public void setAppConfigV2IosFileName(final String appConfigV2IosFileName) {
      this.appConfigV2IosFileName = appConfigV2IosFileName;
    }

    public void setCountryPath(final String countryPath) {
      this.countryPath = countryPath;
    }

    public void setDatePath(final String datePath) {
      this.datePath = datePath;
    }

    public void setDiagnosisKeysPath(final String diagnosisKeysPath) {
      this.diagnosisKeysPath = diagnosisKeysPath;
    }

    public void setHourPath(final String hourPath) {
      this.hourPath = hourPath;
    }

    public void setLocalStatisticsFileName(final String localStatisticsFileName) {
      this.localStatisticsFileName = localStatisticsFileName;
    }

    public void setOriginCountry(final String originCountry) {
      this.originCountry = originCountry;
    }

    public void setParametersPath(final String parametersPath) {
      this.parametersPath = parametersPath;
    }

    public void setStatisticsFileName(final String statisticsFileName) {
      this.statisticsFileName = statisticsFileName;
    }

    public void setTraceWarningsPath(final String traceWarningsPath) {
      this.traceWarningsPath = traceWarningsPath;
    }

    public void setVersionPath(final String versionPath) {
      this.versionPath = versionPath;
    }

    public void setVersionV1(final String versionV1) {
      this.versionV1 = versionV1;
    }

    public void setVersionV2(final String versionV2) {
      this.versionV2 = versionV2;
    }
  }

  public static class AppConfigParameters {

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

      public Boolean getRequireCtsProfileMatch() {
        return requireCtsProfileMatch;
      }

      public Boolean getRequireEvaluationTypeBasic() {
        return requireEvaluationTypeBasic;
      }

      public Boolean getRequireEvaluationTypeHardwareBacked() {
        return requireEvaluationTypeHardwareBacked;
      }

      public void setRequireBasicIntegrity(final Boolean requireBasicIntegrity) {
        this.requireBasicIntegrity = requireBasicIntegrity;
      }

      public void setRequireCtsProfileMatch(final Boolean requireCtsProfileMatch) {
        this.requireCtsProfileMatch = requireCtsProfileMatch;
      }

      public void setRequireEvaluationTypeBasic(final Boolean requireEvaluationTypeBasic) {
        this.requireEvaluationTypeBasic = requireEvaluationTypeBasic;
      }

      public void setRequireEvaluationTypeHardwareBacked(final Boolean requireEvaluationTypeHardwareBacked) {
        this.requireEvaluationTypeHardwareBacked = requireEvaluationTypeHardwareBacked;
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

      public Integer getOverallTimeoutInSeconds() {
        return overallTimeoutInSeconds;
      }

      public void setMaxExposureDetectionsPerInterval(final Integer maxExposureDetectionsPerInterval) {
        this.maxExposureDetectionsPerInterval = maxExposureDetectionsPerInterval;
      }

      public void setOverallTimeoutInSeconds(final Integer overallTimeoutInSeconds) {
        this.overallTimeoutInSeconds = overallTimeoutInSeconds;
      }
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

      public Integer getOverallTimeoutInSeconds() {
        return overallTimeoutInSeconds;
      }

      public void setDownloadTimeoutInSeconds(final Integer downloadTimeoutInSeconds) {
        this.downloadTimeoutInSeconds = downloadTimeoutInSeconds;
      }

      public void setOverallTimeoutInSeconds(final Integer overallTimeoutInSeconds) {
        this.overallTimeoutInSeconds = overallTimeoutInSeconds;
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

      public Boolean getRequireCtsProfileMatch() {
        return requireCtsProfileMatch;
      }

      public Boolean getRequireEvaluationTypeBasic() {
        return requireEvaluationTypeBasic;
      }

      public Boolean getRequireEvaluationTypeHardwareBacked() {
        return requireEvaluationTypeHardwareBacked;
      }

      public void setRequireBasicIntegrity(final Boolean requireBasicIntegrity) {
        this.requireBasicIntegrity = requireBasicIntegrity;
      }

      public void setRequireCtsProfileMatch(final Boolean requireCtsProfileMatch) {
        this.requireCtsProfileMatch = requireCtsProfileMatch;
      }

      public void setRequireEvaluationTypeBasic(final Boolean requireEvaluationTypeBasic) {
        this.requireEvaluationTypeBasic = requireEvaluationTypeBasic;
      }

      public void setRequireEvaluationTypeHardwareBacked(final Boolean requireEvaluationTypeHardwareBacked) {
        this.requireEvaluationTypeHardwareBacked = requireEvaluationTypeHardwareBacked;
      }
    }

    public static class AndroidSrsPpacParameters extends AndroidPrivacyPreservingAnalyticsParameters {
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

      public Boolean getSurveyOnHighRiskEnabled() {
        return surveyOnHighRiskEnabled;
      }

      public String getSurveyOnHighRiskUrl() {
        return surveyOnHighRiskUrl;
      }

      public void setOtpQueryParameterName(final String otpQueryParameterName) {
        this.otpQueryParameterName = otpQueryParameterName;
      }

      public void setSurveyOnHighRiskEnabled(final Boolean surveyOnHighRiskEnabled) {
        this.surveyOnHighRiskEnabled = surveyOnHighRiskEnabled;
      }

      public void setSurveyOnHighRiskUrl(final String surveyOnHighRiskUrl) {
        this.surveyOnHighRiskUrl = surveyOnHighRiskUrl;
      }
    }

    private abstract static class CommonKeyDownloadParameters {

      private String revokedDayPackages;
      private String revokedHourPackages;

      public List<DeserializedDayPackageMetadata> getRevokedDayPackages() {
        return SerializationUtils.deserializeJson(revokedDayPackages,
            typeFactory -> typeFactory.constructCollectionType(List.class, DeserializedDayPackageMetadata.class));
      }

      public List<DeserializedHourPackageMetadata> getRevokedHourPackages() {
        return SerializationUtils.deserializeJson(revokedHourPackages,
            typeFactory -> typeFactory
                .constructCollectionType(List.class, DeserializedHourPackageMetadata.class));
      }

      public void setRevokedDayPackages(final String revokedDayPackages) {
        this.revokedDayPackages = revokedDayPackages;
      }

      public void setRevokedHourPackages(final String revokedHourPackages) {
        this.revokedHourPackages = revokedHourPackages;
      }
    }

    private static class CommonPpaParameters {

      private Double probabilityToSubmit;
      private Double probabilityToSubmitExposureWindows;
      @PositiveOrZero
      private Integer hoursSinceTestRegistrationToSubmitTestResultMetadata;
      @PositiveOrZero
      private Integer hoursSinceTestToSubmitKeySubmissionMetadata;

      public Integer getHoursSinceTestRegistrationToSubmitTestResultMetadata() {
        return hoursSinceTestRegistrationToSubmitTestResultMetadata;
      }

      public Integer getHoursSinceTestToSubmitKeySubmissionMetadata() {
        return hoursSinceTestToSubmitKeySubmissionMetadata;
      }

      public Double getProbabilityToSubmit() {
        return probabilityToSubmit;
      }

      public Double getProbabilityToSubmitExposureWindows() {
        return probabilityToSubmitExposureWindows;
      }

      public void setHoursSinceTestRegistrationToSubmitTestResultMetadata(final Integer integer) {
        this.hoursSinceTestRegistrationToSubmitTestResultMetadata = integer;
      }

      public void setHoursSinceTestToSubmitKeySubmissionMetadata(
          final Integer hoursSinceTestToSubmitKeySubmissionMetadata) {
        this.hoursSinceTestToSubmitKeySubmissionMetadata = hoursSinceTestToSubmitKeySubmissionMetadata;
      }

      public void setProbabilityToSubmit(final Double probabilityToSubmit) {
        this.probabilityToSubmit = probabilityToSubmit;
      }

      public void setProbabilityToSubmitExposureWindows(final Double probabilityToSubmitExposureWindows) {
        this.probabilityToSubmitExposureWindows = probabilityToSubmitExposureWindows;
      }
    }

    public static class DeserializedDayPackageMetadata {

      private String region;
      private String date;
      private String etag;

      public String getDate() {
        return date;
      }

      public String getEtag() {
        return etag;
      }

      public String getRegion() {
        return region;
      }
    }

    public static class DeserializedHourPackageMetadata extends DeserializedDayPackageMetadata {

      private Integer hour;

      public Integer getHour() {
        return hour;
      }
    }

    public static class DgcParameters {

      public static class DgcBlocklistParameters {

        public static class DgcBlockedUvciChunk {

          List<Integer> indices;
          String hash;
          Integer validFrom;

          public byte[] getHash() {
            return Hex.decode(hash);
          }

          public List<Integer> getIndices() {
            return indices;
          }

          public Integer getValidFrom() {
            return validFrom;
          }

          public void setHash(final String hash) {
            this.hash = hash;
          }

          public void setIndices(final List<Integer> indices) {
            this.indices = indices;
          }

          public void setValidFrom(final Integer validFrom) {
            this.validFrom = validFrom;
          }
        }

        private String blockedUvciChunks;

        /**
         * Parse String from application.yaml parameter.
         *
         * @return parsed string in a list of DgcBlockedUvciChunk.
         */
        public List<DgcBlockedUvciChunk> getBlockedUvciChunks() {
          return SerializationUtils.deserializeJson(blockedUvciChunks,
              typeFactory -> typeFactory
                  .constructCollectionType(List.class, DgcBlockedUvciChunk.class));
        }

        public void setBlockedUvciChunks(final String blockedUvciChunks) {
          this.blockedUvciChunks = blockedUvciChunks;
        }
      }

      public static class DgcTestCertificateParameters {

        @Min(0)
        @Max(60)
        private Integer waitAfterPublicKeyRegistrationInSeconds;

        @Min(0)
        @Max(60)
        private Integer waitForRetryInSeconds;

        public Integer getWaitAfterPublicKeyRegistrationInSeconds() {
          return waitAfterPublicKeyRegistrationInSeconds;
        }

        public Integer getWaitForRetryInSeconds() {
          return waitForRetryInSeconds;
        }

        public void setWaitAfterPublicKeyRegistrationInSeconds(final Integer waitAfterPublicKeyRegistrationInSeconds) {
          this.waitAfterPublicKeyRegistrationInSeconds = waitAfterPublicKeyRegistrationInSeconds;
        }

        public void setWaitForRetryInSeconds(final Integer waitForRetryInSeconds) {
          this.waitForRetryInSeconds = waitForRetryInSeconds;
        }
      }

      private DgcTestCertificateParameters dgcTestCertificateParameters;

      @Min(0)
      @Max(100)
      private Integer expirationThresholdInDays;

      private DgcBlocklistParameters blockListParameters;

      private String iosDgcReissueServicePublicKeyDigest;

      private String androidDgcReissueServicePublicKeyDigest;

      public byte[] getAndroidDgcReissueServicePublicKeyDigest() {
        return Hex.decode(androidDgcReissueServicePublicKeyDigest);
      }

      public DgcBlocklistParameters getBlockListParameters() {
        return blockListParameters;
      }

      public Integer getExpirationThresholdInDays() {
        return expirationThresholdInDays;
      }

      public byte[] getIosDgcReissueServicePublicKeyDigest() {
        return Hex.decode(iosDgcReissueServicePublicKeyDigest);
      }

      public DgcTestCertificateParameters getTestCertificateParameters() {
        return dgcTestCertificateParameters;
      }

      public void setAndroidDgcReissueServicePublicKeyDigest(final String androidDgcReissueServicePublicKeyDigest) {
        this.androidDgcReissueServicePublicKeyDigest = androidDgcReissueServicePublicKeyDigest;
      }

      public void setBlockListParameters(final DgcBlocklistParameters blockListParameters) {
        this.blockListParameters = blockListParameters;
      }

      public void setExpirationThresholdInDays(final Integer expirationThresholdInDays) {
        this.expirationThresholdInDays = expirationThresholdInDays;
      }

      public void setIosDgcReissueServicePublicKeyDigest(final String iosDgcReissueServicePublicKeyDigest) {
        this.iosDgcReissueServicePublicKeyDigest = iosDgcReissueServicePublicKeyDigest;
      }

      public void setTestCertificateParameters(final DgcTestCertificateParameters dgcTestCertificateParameters) {
        this.dgcTestCertificateParameters = dgcTestCertificateParameters;
      }
    }

    public static class IosEventDrivenUserSurveyParameters extends CommonEdusParameters {

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

      public void setMaxExposureDetectionsPerInterval(final Integer maxExposureDetectionsPerInterval) {
        this.maxExposureDetectionsPerInterval = maxExposureDetectionsPerInterval;
      }

    }

    public static class IosKeyDownloadParameters extends CommonKeyDownloadParameters {

    }

    public static class IosPrivacyPreservingAnalyticsParameters extends CommonPpaParameters {

    }

    private IosKeyDownloadParameters iosKeyDownloadParameters;

    private AndroidKeyDownloadParameters androidKeyDownloadParameters;

    private IosExposureDetectionParameters iosExposureDetectionParameters;

    private AndroidExposureDetectionParameters androidExposureDetectionParameters;

    private IosEventDrivenUserSurveyParameters iosEventDrivenUserSurveyParameters;

    private AndroidEventDrivenUserSurveyParameters androidEventDrivenUserSurveyParameters;

    private IosPrivacyPreservingAnalyticsParameters iosPrivacyPreservingAnalyticsParameters;

    private AndroidPrivacyPreservingAnalyticsParameters androidPrivacyPreservingAnalyticsParameters;

    private AndroidSrsPpacParameters androidSrsPpacParameters;

    private DgcParameters dgcParameters;

    @Min(1)
    @Max(1000)
    private int srsTimeSinceOnboardingInHours;

    @Min(1)
    @Max(1000)
    private int srsTimeBetweenSubmissionsInDays;

    public AndroidEventDrivenUserSurveyParameters getAndroidEventDrivenUserSurveyParameters() {
      return androidEventDrivenUserSurveyParameters;
    }

    public AndroidExposureDetectionParameters getAndroidExposureDetectionParameters() {
      return androidExposureDetectionParameters;
    }

    public AndroidKeyDownloadParameters getAndroidKeyDownloadParameters() {
      return androidKeyDownloadParameters;
    }

    public AndroidPrivacyPreservingAnalyticsParameters getAndroidPrivacyPreservingAnalyticsParameters() {
      return androidPrivacyPreservingAnalyticsParameters;
    }

    public AndroidSrsPpacParameters getAndroidSrsPpacParameters() {
      return androidSrsPpacParameters;
    }

    public DgcParameters getDgcParameters() {
      return dgcParameters;
    }

    public IosEventDrivenUserSurveyParameters getIosEventDrivenUserSurveyParameters() {
      return iosEventDrivenUserSurveyParameters;
    }

    public IosExposureDetectionParameters getIosExposureDetectionParameters() {
      return iosExposureDetectionParameters;
    }

    public IosKeyDownloadParameters getIosKeyDownloadParameters() {
      return iosKeyDownloadParameters;
    }

    public IosPrivacyPreservingAnalyticsParameters getIosPrivacyPreservingAnalyticsParameters() {
      return iosPrivacyPreservingAnalyticsParameters;
    }

    public int getSrsTimeBetweenSubmissionsInDays() {
      return srsTimeBetweenSubmissionsInDays;
    }

    public int getSrsTimeSinceOnboardingInHours() {
      return srsTimeSinceOnboardingInHours;
    }

    public void setAndroidEventDrivenUserSurveyParameters(
        final AndroidEventDrivenUserSurveyParameters androidEventDrivenUserSurveyParameters) {
      this.androidEventDrivenUserSurveyParameters = androidEventDrivenUserSurveyParameters;
    }

    public void setAndroidExposureDetectionParameters(
        final AndroidExposureDetectionParameters androidExposureDetectionParameters) {
      this.androidExposureDetectionParameters = androidExposureDetectionParameters;
    }

    public void setAndroidKeyDownloadParameters(final AndroidKeyDownloadParameters androidKeyDownloadParameters) {
      this.androidKeyDownloadParameters = androidKeyDownloadParameters;
    }

    public void setAndroidPrivacyPreservingAnalyticsParameters(
        final AndroidPrivacyPreservingAnalyticsParameters androidPrivacyPreservingAnalyticsParameters) {
      this.androidPrivacyPreservingAnalyticsParameters = androidPrivacyPreservingAnalyticsParameters;
    }

    public void setAndroidSrsPpacParameters(final AndroidSrsPpacParameters androidSrsPpacParameters) {
      this.androidSrsPpacParameters = androidSrsPpacParameters;
    }

    public void setDgcParameters(final DgcParameters dgcParameters) {
      this.dgcParameters = dgcParameters;
    }

    public void setIosEventDrivenUserSurveyParameters(
        final IosEventDrivenUserSurveyParameters iosEventDrivenUserSurveyParameters) {
      this.iosEventDrivenUserSurveyParameters = iosEventDrivenUserSurveyParameters;
    }

    public void setIosExposureDetectionParameters(final IosExposureDetectionParameters iosExposureDetectionParameters) {
      this.iosExposureDetectionParameters = iosExposureDetectionParameters;
    }

    public void setIosKeyDownloadParameters(final IosKeyDownloadParameters iosKeyDownloadParameters) {
      this.iosKeyDownloadParameters = iosKeyDownloadParameters;
    }

    public void setIosPrivacyPreservingAnalyticsParameters(
        final IosPrivacyPreservingAnalyticsParameters iosPrivacyPreservingAnalyticsParameters) {
      this.iosPrivacyPreservingAnalyticsParameters = iosPrivacyPreservingAnalyticsParameters;
    }

    public void setSrsTimeBetweenSubmissionsInDays(final int srsTimeBetweenSubmissionsInDays) {
      this.srsTimeBetweenSubmissionsInDays = srsTimeBetweenSubmissionsInDays;
    }

    public void setSrsTimeSinceOnboardingInHours(final int srsTimeSinceOnboardingInHours) {
      this.srsTimeSinceOnboardingInHours = srsTimeSinceOnboardingInHours;
    }

  }

  public static class AppFeature {

    private String label;
    private Integer value;

    public String getLabel() {
      return label;
    }

    public Integer getValue() {
      return value;
    }

    public void setLabel(final String label) {
      this.label = label;
    }

    public void setValue(final Integer value) {
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

    public String getLatestAndroid() {
      return latestAndroid;
    }

    public Integer getLatestAndroidVersionCode() {
      return latestAndroidVersionCode;
    }

    public String getLatestIos() {
      return latestIos;
    }

    public String getMinAndroid() {
      return minAndroid;
    }

    public Integer getMinAndroidVersionCode() {
      return minAndroidVersionCode;
    }

    public String getMinIos() {
      return minIos;
    }

    public void setLatestAndroid(final String latestAndroid) {
      this.latestAndroid = latestAndroid;
    }

    public void setLatestAndroidVersionCode(final Integer latestAndroidVersionCode) {
      this.latestAndroidVersionCode = latestAndroidVersionCode;
    }

    public void setLatestIos(final String latestIos) {
      this.latestIos = latestIos;
    }

    public void setMinAndroid(final String minAndroid) {
      this.minAndroid = minAndroid;
    }

    public void setMinAndroidVersionCode(final Integer minAndroidVersionCode) {
      this.minAndroidVersionCode = minAndroidVersionCode;
    }

    public void setMinIos(final String minIos) {
      this.minIos = minIos;
    }
  }

  public static class Client {

    public static class Ssl {

      private File trustStore;
      private String trustStorePassword;

      public File getTrustStore() {
        return trustStore;
      }

      public String getTrustStorePassword() {
        return trustStorePassword;
      }

      public void setTrustStore(final File trustStore) {
        this.trustStore = trustStore;
      }

      public void setTrustStorePassword(final String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
      }
    }

    private String publicKey;

    private String baseUrl;

    private Ssl ssl;

    private int retryPeriod;

    private int maxRetryPeriod;

    private int maxRetryAttempts;

    public String getBaseUrl() {
      return baseUrl;
    }

    public int getMaxRetryAttempts() {
      return maxRetryAttempts;
    }

    public int getMaxRetryPeriod() {
      return maxRetryPeriod;
    }

    public String getPublicKey() {
      return publicKey;
    }

    public int getRetryPeriod() {
      return retryPeriod;
    }

    public Ssl getSsl() {
      return ssl;
    }

    public void setBaseUrl(final String baseUrl) {
      this.baseUrl = baseUrl;
    }

    public void setMaxRetryAttempts(final int maxRetryAttempts) {
      this.maxRetryAttempts = maxRetryAttempts;
    }

    public void setMaxRetryPeriod(final int maxRetryPeriod) {
      this.maxRetryPeriod = maxRetryPeriod;
    }

    public void setPublicKey(final String publicKey) {
      this.publicKey = publicKey;
    }

    public void setRetryPeriod(final int retryPeriod) {
      this.retryPeriod = retryPeriod;
    }

    public void setSsl(final Ssl ssl) {
      this.ssl = ssl;
    }
  }

  public static class DccRevocation {

    private Client client;
    private String certificate;
    private String dccListPath;
    private String dccRevocationDirectory;

    public String getCertificate() {
      return certificate;
    }

    public Client getClient() {
      return client;
    }

    public String getDccListPath() {
      return dccListPath;
    }

    public String getDccRevocationDirectory() {
      return dccRevocationDirectory;
    }

    public void setCertificate(final String certificate) {
      this.certificate = certificate;
    }

    public void setClient(final Client client) {
      this.client = client;
    }

    public void setDccListPath(final String dccListPath) {
      this.dccListPath = dccListPath;
    }

    public void setDccRevocationDirectory(final String dccRevocationDirectory) {
      this.dccRevocationDirectory = dccRevocationDirectory;
    }
  }

  public static class DigitalGreenCertificate {

    @Pattern(regexp = RESOURCE_OR_EMPTY_REGEX)
    private String mahJsonPath;

    @Pattern(regexp = RESOURCE_OR_EMPTY_REGEX)
    private String prophylaxisJsonPath;

    @Pattern(regexp = RESOURCE_OR_EMPTY_REGEX)
    private String medicinalProductsJsonPath;

    @Pattern(regexp = RESOURCE_OR_EMPTY_REGEX)
    private String diseaseAgentTargetedJsonPath;

    @Pattern(regexp = RESOURCE_OR_EMPTY_REGEX)
    private String testManfJsonPath;

    @Pattern(regexp = RESOURCE_OR_EMPTY_REGEX)
    private String testResultJsonPath;

    @Pattern(regexp = RESOURCE_OR_EMPTY_REGEX)
    private String testTypeJsonPath;

    @NotNull
    @Pattern(regexp = CHAR_AND_NUMBER_REGEX)
    private String dgcDirectory;

    @NotNull
    @Pattern(regexp = CHAR_AND_NUMBER_REGEX)
    private String boosterNotification;

    @NotNull
    @Pattern(regexp = CHAR_AND_NUMBER_REGEX)
    private String valuesetsFileName;

    private String cclDirectory;

    private String[] cclAllowList;

    private String allowList;

    private String allowListSignature;

    private String allowListCertificate;

    private String[] supportedLanguages;

    private String exportArchiveName;

    private Client client;

    private Client dscClient;

    /**
     * getAllowList.
     *
     * @return AllowList
     */
    public AllowList getAllowList() {
      return SerializationUtils.deserializeJson(allowList,
          typeFactory -> typeFactory
              .constructType(AllowList.class));
    }

    public String getAllowListAsString() {
      return allowList;
    }

    public String getAllowListCertificate() {
      return allowListCertificate;
    }

    public byte[] getAllowListSignature() {
      return Hex.decode(allowListSignature);
    }

    public String getBoosterNotification() {
      return boosterNotification;
    }

    public String[] getCclAllowList() {
      return cclAllowList;
    }

    public String getCclDirectory() {
      return cclDirectory;
    }

    public Client getClient() {
      return client;
    }

    public String getDgcDirectory() {
      return dgcDirectory;
    }

    public String getDiseaseAgentTargetedJsonPath() {
      return diseaseAgentTargetedJsonPath;
    }

    public Client getDscClient() {
      return dscClient;
    }

    public String getExportArchiveName() {
      return exportArchiveName;
    }

    public String getMahJsonPath() {
      return mahJsonPath;
    }

    public String getMedicinalProductsJsonPath() {
      return medicinalProductsJsonPath;
    }

    public String getProphylaxisJsonPath() {
      return prophylaxisJsonPath;
    }

    public String[] getSupportedLanguages() {
      return supportedLanguages;
    }

    public String getTestManfJsonPath() {
      return testManfJsonPath;
    }

    public String getTestResultJsonPath() {
      return testResultJsonPath;
    }

    public String getTestTypeJsonPath() {
      return testTypeJsonPath;
    }

    public String getValuesetsFileName() {
      return valuesetsFileName;
    }

    public void setAllowList(final String allowList) {
      this.allowList = allowList;
    }

    public void setAllowListCertificate(final String allowListCertificate) {
      this.allowListCertificate = allowListCertificate;
    }

    public void setAllowListSignature(final String allowListSignature) {
      this.allowListSignature = allowListSignature;
    }

    public void setBoosterNotification(final String boosterNotification) {
      this.boosterNotification = boosterNotification;
    }

    public void setCclAllowList(final String[] cclAllowList) {
      this.cclAllowList = cclAllowList;
    }

    public void setCclDirectory(final String cclDirectory) {
      this.cclDirectory = cclDirectory;
    }

    public void setClient(final Client client) {
      this.client = client;
    }

    public void setDgcDirectory(final String dgcDirectory) {
      this.dgcDirectory = dgcDirectory;
    }

    public void setDiseaseAgentTargetedJsonPath(final String diseaseAgentTargetedJsonPath) {
      this.diseaseAgentTargetedJsonPath = diseaseAgentTargetedJsonPath;
    }

    public void setDscClient(final Client dscClient) {
      this.dscClient = dscClient;
    }

    public void setExportArchiveName(final String exportArchiveName) {
      this.exportArchiveName = exportArchiveName;
    }

    public void setMahJsonPath(final String mahJsonPath) {
      this.mahJsonPath = mahJsonPath;
    }

    public void setMedicinalProductsJsonPath(final String medicinalProductsJsonPath) {
      this.medicinalProductsJsonPath = medicinalProductsJsonPath;
    }

    public void setProphylaxisJsonPath(final String prophylaxisJsonPath) {
      this.prophylaxisJsonPath = prophylaxisJsonPath;
    }

    public void setSupportedLanguages(final String[] supportedLanguages) {
      this.supportedLanguages = supportedLanguages;
    }

    public void setTestManfJsonPath(final String testManfJsonPath) {
      this.testManfJsonPath = testManfJsonPath;
    }

    public void setTestResultJsonPath(final String testResultJsonPath) {
      this.testResultJsonPath = testResultJsonPath;
    }

    public void setTestTypeJsonPath(final String testTypeJsonPath) {
      this.testTypeJsonPath = testTypeJsonPath;
    }

    public void setValuesetsFileName(final String valuesetsFileName) {
      this.valuesetsFileName = valuesetsFileName;
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

    public String getBucket() {
      return bucket;
    }

    public String getEndpoint() {
      return endpoint;
    }

    public Boolean getForceUpdateKeyfiles() {
      return forceUpdateKeyfiles;
    }

    public Integer getHourFileRetentionDays() {
      return hourFileRetentionDays;
    }

    public Integer getMaxNumberOfFailedOperations() {
      return maxNumberOfFailedOperations;
    }

    public Integer getMaxNumberOfS3Threads() {
      return maxNumberOfS3Threads;
    }

    public Integer getPort() {
      return port;
    }

    public String getSecretKey() {
      return secretKey;
    }

    public Boolean isSetPublicReadAclOnPutObject() {
      return setPublicReadAclOnPutObject;
    }

    public void setAccessKey(final String accessKey) {
      this.accessKey = accessKey;
    }

    public void setBucket(final String bucket) {
      this.bucket = bucket;
    }

    public void setEndpoint(final String endpoint) {
      this.endpoint = endpoint;
    }

    public void setForceUpdateKeyfiles(final Boolean forceUpdateKeyfiles) {
      this.forceUpdateKeyfiles = forceUpdateKeyfiles;
    }

    public void setHourFileRetentionDays(final Integer hourFileRetentionDays) {
      this.hourFileRetentionDays = hourFileRetentionDays;
    }

    public void setMaxNumberOfFailedOperations(final Integer maxNumberOfFailedOperations) {
      this.maxNumberOfFailedOperations = maxNumberOfFailedOperations;
    }

    public void setMaxNumberOfS3Threads(final Integer maxNumberOfS3Threads) {
      this.maxNumberOfS3Threads = maxNumberOfS3Threads;
    }

    public void setPort(final Integer port) {
      this.port = port;
    }

    public void setSecretKey(final String secretKey) {
      this.secretKey = secretKey;
    }

    public void setSetPublicReadAclOnPutObject(final Boolean setPublicReadAclOnPutObject) {
      this.setPublicReadAclOnPutObject = setPublicReadAclOnPutObject;
    }
  }

  public static class Paths {

    @Pattern(regexp = PRIVATE_KEY_REGEX)
    private String privateKey;
    @Pattern(regexp = PATH_REGEX)
    private String output;

    public String getOutput() {
      return output;
    }

    public String getPrivateKey() {
      return privateKey;
    }

    public void setOutput(final String output) {
      this.output = output;
    }

    public void setPrivateKey(final String privateKey) {
      this.privateKey = privateKey;
    }
  }

  public static class PresenceTracingParameters {

    public static class PlausibleDeniabilityParameters {

      private double probabilityToFakeCheckInsIfNoCheckIns;
      private double probabilityToFakeCheckInsIfSomeCheckIns;

      public double getProbabilityToFakeCheckInsIfNoCheckIns() {
        return probabilityToFakeCheckInsIfNoCheckIns;
      }

      public double getProbabilityToFakeCheckInsIfSomeCheckIns() {
        return probabilityToFakeCheckInsIfSomeCheckIns;
      }

      public void setProbabilityToFakeCheckInsIfNoCheckIns(final double probabilityToFakeCheckInsIfNoCheckIns) {
        this.probabilityToFakeCheckInsIfNoCheckIns = probabilityToFakeCheckInsIfNoCheckIns;
      }

      public void setProbabilityToFakeCheckInsIfSomeCheckIns(final double probabilityToFakeCheckInsIfSomeCheckIns) {
        this.probabilityToFakeCheckInsIfSomeCheckIns = probabilityToFakeCheckInsIfSomeCheckIns;
      }
    }

    private int qrCodeErrorCorrectionLevel;

    private PlausibleDeniabilityParameters plausibleDeniabilityParameters;

    public PlausibleDeniabilityParameters getPlausibleDeniabilityParameters() {
      return plausibleDeniabilityParameters;
    }

    public int getQrCodeErrorCorrectionLevel() {
      return qrCodeErrorCorrectionLevel;
    }

    public void setPlausibleDeniabilityParameters(final PlausibleDeniabilityParameters plausibleDeniabilityParameters) {
      this.plausibleDeniabilityParameters = plausibleDeniabilityParameters;
    }

    public void setQrCodeErrorCorrectionLevel(final int qrCodeErrorCorrectionLevel) {
      this.qrCodeErrorCorrectionLevel = qrCodeErrorCorrectionLevel;
    }
  }

  public static class QrCodePosterTemplate {

    public static class DescriptionTextBox {

      @NotNull
      private Double offsetX;
      @NotNull
      private Double offsetY;
      @NotNull
      private Integer width;
      @NotNull
      private Integer height;
      @NotNull
      private Integer fontSize;
      @NotNull
      private String fontColor;

      public String getFontColor() {
        return fontColor;
      }

      public Integer getFontSize() {
        return fontSize;
      }

      public Integer getHeight() {
        return height;
      }

      public Double getOffsetX() {
        return offsetX;
      }

      public Double getOffsetY() {
        return offsetY;
      }

      public Integer getWidth() {
        return width;
      }

      public void setFontColor(final String fontColor) {
        this.fontColor = fontColor;
      }

      public void setFontSize(final Integer fontSize) {
        this.fontSize = fontSize;
      }

      public void setHeight(final Integer height) {
        this.height = height;
      }

      public void setOffsetX(final Double offsetX) {
        this.offsetX = offsetX;
      }

      public void setOffsetY(final Double offsetY) {
        this.offsetY = offsetY;
      }

      public void setWidth(final Integer width) {
        this.width = width;
      }
    }

    private String template;
    @NotNull
    private Double offsetX;
    @NotNull
    private Double offsetY;
    @NotNull
    private Integer qrCodeSideLength;
    @NotEmpty
    private String publishedArchiveName;
    private DescriptionTextBox descriptionTextBox;

    private DescriptionTextBox addressTextBox;

    public DescriptionTextBox getAddressTextBox() {
      return addressTextBox;
    }

    public DescriptionTextBox getDescriptionTextBox() {
      return descriptionTextBox;
    }

    public Double getOffsetX() {
      return offsetX;
    }

    public Double getOffsetY() {
      return offsetY;
    }

    public String getPublishedArchiveName() {
      return publishedArchiveName;
    }

    public Integer getQrCodeSideLength() {
      return qrCodeSideLength;
    }

    public String getTemplate() {
      return template;
    }

    public void setAddressTextBox(final DescriptionTextBox addressTextBox) {
      this.addressTextBox = addressTextBox;
    }

    public void setDescriptionTextBox(final DescriptionTextBox descriptionTextBox) {
      this.descriptionTextBox = descriptionTextBox;
    }

    public void setOffsetX(final Double offsetX) {
      this.offsetX = offsetX;
    }

    public void setOffsetY(final Double offsetY) {
      this.offsetY = offsetY;
    }

    public void setPublishedArchiveName(final String publishedArchiveName) {
      this.publishedArchiveName = publishedArchiveName;
    }

    public void setQrCodeSideLength(final Integer qrCodeSideLength) {
      this.qrCodeSideLength = qrCodeSideLength;
    }

    public void setTemplate(final String template) {
      this.template = template;
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

    public String getAlgorithmName() {
      return algorithmName;
    }

    public String getAlgorithmOid() {
      return algorithmOid;
    }

    public String getAndroidPackage() {
      return androidPackage;
    }

    public String getAppBundleId() {
      return appBundleId;
    }

    public String getFileName() {
      return fileName;
    }

    public String getSecurityProvider() {
      return securityProvider;
    }

    /**
     * Returns the static {@link SignatureInfo} configured in the application properties.
     *
     * @return SignatureInfo
     */
    public SignatureInfo getSignatureInfo() {
      return SignatureInfo.newBuilder()
          .setAppBundleId(getAppBundleId())
          .setVerificationKeyVersion(getVerificationKeyVersion())
          .setVerificationKeyId(getVerificationKeyId())
          .setSignatureAlgorithm(getAlgorithmOid())
          .build();
    }

    public String getVerificationKeyId() {
      return verificationKeyId;
    }

    public String getVerificationKeyVersion() {
      return verificationKeyVersion;
    }

    public void setAlgorithmName(final String algorithmName) {
      this.algorithmName = algorithmName;
    }

    public void setAlgorithmOid(final String algorithmOid) {
      this.algorithmOid = algorithmOid;
    }

    public void setAndroidPackage(final String androidPackage) {
      this.androidPackage = androidPackage;
    }

    public void setAppBundleId(final String appBundleId) {
      this.appBundleId = appBundleId;
    }

    public void setFileName(final String fileName) {
      this.fileName = fileName;
    }

    public void setSecurityProvider(final String securityProvider) {
      this.securityProvider = securityProvider;
    }

    public void setVerificationKeyId(final String verificationKeyId) {
      this.verificationKeyId = verificationKeyId;
    }

    public void setVerificationKeyVersion(final String verificationKeyVersion) {
      this.verificationKeyVersion = verificationKeyVersion;
    }
  }

  public static class StatisticsConfig {

    private String statisticPath;

    private String localStatisticPath;

    private String accessKey;

    private String secretKey;

    private String endpoint;

    private String bucket;

    private String pandemicRadarUrl;

    private String pandemicRadarBmgUrl;

    public String getAccessKey() {
      return accessKey;
    }

    public String getBucket() {
      return bucket;
    }

    public String getEndpoint() {
      return endpoint;
    }

    public String getLocalStatisticPath() {
      return localStatisticPath;
    }

    public String getPandemicRadarBmgUrl() {
      return pandemicRadarBmgUrl;
    }

    public String getPandemicRadarUrl() {
      return pandemicRadarUrl;
    }

    public String getSecretKey() {
      return secretKey;
    }

    public String getStatisticPath() {
      return statisticPath;
    }

    public void setAccessKey(final String accessKey) {
      this.accessKey = accessKey;
    }

    public void setBucket(final String bucket) {
      this.bucket = bucket;
    }

    public void setEndpoint(final String endpoint) {
      this.endpoint = endpoint;
    }

    public void setLocalStatisticPath(final String localStatisticPath) {
      this.localStatisticPath = localStatisticPath;
    }

    public void setPandemicRadarBmgUrl(final String pandemicRadarBmgUrl) {
      this.pandemicRadarBmgUrl = pandemicRadarBmgUrl;
    }

    public void setPandemicRadarUrl(final String pandemicRadarUrl) {
      this.pandemicRadarUrl = pandemicRadarUrl;
    }

    public void setSecretKey(final String secretKey) {
      this.secretKey = secretKey;
    }

    public void setStatisticPath(final String statisticPath) {
      this.statisticPath = statisticPath;
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

    public String getFileHeader() {
      return fileHeader;
    }

    public Integer getFileHeaderWidth() {
      return fileHeaderWidth;
    }

    public String getFileName() {
      return fileName;
    }

    public void setFileHeader(final String fileHeader) {
      this.fileHeader = fileHeader;
    }

    public void setFileHeaderWidth(final Integer fileHeaderWidth) {
      this.fileHeaderWidth = fileHeaderWidth;
    }

    public void setFileName(final String fileName) {
      this.fileName = fileName;
    }
  }

  public static class TestData {

    private Integer seed;
    private Integer exposuresPerHour;
    private boolean distributionTestdataConsentToFederation;

    public boolean getDistributionTestdataConsentToFederation() {
      return distributionTestdataConsentToFederation;
    }

    public Integer getExposuresPerHour() {
      return exposuresPerHour;
    }

    public Integer getSeed() {
      return seed;
    }

    public void setDistributionTestdataConsentToFederation(final boolean distributionTestdataConsentToFederation) {
      this.distributionTestdataConsentToFederation = distributionTestdataConsentToFederation;
    }

    public void setExposuresPerHour(final Integer exposuresPerHour) {
      this.exposuresPerHour = exposuresPerHour;
    }

    public void setSeed(final Integer seed) {
      this.seed = seed;
    }
  }

  private static final String PATH_REGEX = "^[/]?[a-zA-Z0-9_]{1,1024}(/[a-zA-Z0-9_]{1,1024}){0,256}[/]?$";
  private static final String RESOURCE_REGEX = "^(classpath:|file:[/]{1,3})?([a-zA-Z0-9_/\\.]{1,1010})$";
  private static final String RESOURCE_OR_EMPTY_REGEX = "(" + RESOURCE_REGEX + ")?";
  private static final String FILE_NAME_REGEX = "^[a-zA-Z0-9_-]{1,1024}$";
  private static final String FILE_NAME_WITH_TYPE_REGEX = "^[a-zA-Z0-9_-]{1,1024}\\.[a-z]{1,64}$";
  private static final String CHAR_AND_NUMBER_REGEX = "^[a-zA-Z0-9_-]{1,1024}$";
  private static final String CHAR_NUMBER_AND_SPACE_REGEX = "^[a-zA-Z0-9_\\s]{1,32}$";
  private static final String NO_WHITESPACE_REGEX = "^[\\S]+$";
  private static final String URL_REGEX = "^http[s]?://[a-zA-Z0-9-_]{1,1024}([\\./][a-zA-Z0-9-_]{1,1024}){0,256}[/]?$";
  private static final String NUMBER_REGEX = "^[0-9]{1,256}$";
  private static final String VERSION_REGEX = "^v[0-9]{1,256}$";
  private static final String ALGORITHM_OID_REGEX = "^[0-9]{1,256}[\\.[0-9]{1,256}]{0,256}$";
  private static final String BUNDLE_REGEX = "^[a-z-]{1,256}[\\.[a-z-]{1,256}]{0,256}$";
  private static final String PRIVATE_KEY_REGEX = 
      "^(classpath:|file:\\.?[/]{1,8})[a-zA-Z0-9_-]{1,256}:?[/[a-zA-Z0-9_-]{1,256}]{0,256}(.pem)?$";
  private Paths paths;
  private TestData testData;
  @Min(0)
  @Max(28)
  private Integer retentionDays;
  @Min(0)
  @Max(4000)
  private int srsTypeStatisticsDays;
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
  @Pattern(regexp = FILE_NAME_REGEX)
  private String outputFileNameV2;
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

  private QrCodePosterTemplate iosQrCodePosterTemplate;

  private QrCodePosterTemplate androidQrCodePosterTemplate;

  private PresenceTracingParameters presenceTracingParameters;

  private DigitalGreenCertificate digitalGreenCertificate;

  private Integer connectionPoolSize;

  private String defaultArchiveName;

  private Integer minimumTrlValueAllowed;

  private Integer daysToPublish;

  private DccRevocation dccRevocation;

  @Min(0)
  @Max(100)
  private int infectionThreshold;

  public QrCodePosterTemplate getAndroidQrCodePosterTemplate() {
    return androidQrCodePosterTemplate;
  }

  public Api getApi() {
    return api;
  }

  public AppConfigParameters getAppConfigParameters() {
    return appConfigParameters;
  }

  public List<AppFeature> getAppFeatures() {
    return appFeatures;
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

  public Boolean getApplyPoliciesForAllCountries() {
    return applyPoliciesForAllCountries;
  }

  public AppVersions getAppVersions() {
    return appVersions;
  }

  public String getCardIdSequence() {
    return cardIdSequence;
  }

  public Integer getConnectionPoolSize() {
    return connectionPoolSize;
  }

  public Integer getDaysToPublish() {
    return daysToPublish == null ? retentionDays : daysToPublish;
  }

  public DccRevocation getDccRevocation() {
    return dccRevocation;
  }

  public String getDefaultArchiveName() {
    return defaultArchiveName;
  }

  public DigitalGreenCertificate getDigitalGreenCertificate() {
    return digitalGreenCertificate;
  }

  public String getEuPackageName() {
    return euPackageName;
  }

  public Integer getExpiryPolicyMinutes() {
    return expiryPolicyMinutes;
  }

  public Boolean getIncludeIncompleteDays() {
    return includeIncompleteDays;
  }

  public Boolean getIncludeIncompleteHours() {
    return includeIncompleteHours;
  }

  public int getInfectionThreshold() {
    return infectionThreshold;
  }

  public QrCodePosterTemplate getIosQrCodePosterTemplate() {
    return iosQrCodePosterTemplate;
  }

  public Integer getMaximumNumberOfKeysPerBundle() {
    return maximumNumberOfKeysPerBundle;
  }

  public Integer getMinimumTrlValueAllowed() {
    return minimumTrlValueAllowed;
  }

  public ObjectStore getObjectStore() {
    return objectStore;
  }

  public String getOutputFileName() {
    return outputFileName;
  }

  public String getOutputFileNameV2() {
    return outputFileNameV2;
  }

  public Paths getPaths() {
    return paths;
  }

  public PresenceTracingParameters getPresenceTracingParameters() {
    return presenceTracingParameters;
  }

  public Integer getRetentionDays() {
    return retentionDays;
  }

  public Integer getShiftingPolicyThreshold() {
    return shiftingPolicyThreshold;
  }

  public Signature getSignature() {
    return signature;
  }

  public int getSrsTypeStatisticsDays() {
    return srsTypeStatisticsDays;
  }

  public StatisticsConfig getStatistics() {
    return statistics;
  }

  public String[] getSupportedCountries() {
    return supportedCountries;
  }

  public TekExport getTekExport() {
    return tekExport;
  }

  public TestData getTestData() {
    return testData;
  }

  public void setAndroidQrCodePosterTemplate(final QrCodePosterTemplate androidQrCodePosterTemplate) {
    this.androidQrCodePosterTemplate = androidQrCodePosterTemplate;
  }

  public void setApi(final Api api) {
    this.api = api;
  }

  public void setAppConfigParameters(final AppConfigParameters appConfigParameters) {
    this.appConfigParameters = appConfigParameters;
  }

  public void setAppFeatures(final List<AppFeature> appFeatures) {
    this.appFeatures = appFeatures;
  }

  public void setApplyPoliciesForAllCountries(final Boolean applyPoliciesForAllCountries) {
    this.applyPoliciesForAllCountries = applyPoliciesForAllCountries;
  }

  public void setAppVersions(final AppVersions appVersions) {
    this.appVersions = appVersions;
  }

  public void setCardIdSequence(final String cardIdSequence) {
    this.cardIdSequence = cardIdSequence;
  }

  public void setConnectionPoolSize(final Integer connectionPoolSize) {
    this.connectionPoolSize = connectionPoolSize;
  }

  public void setDaysToPublish(final Integer daysToPublish) {
    this.daysToPublish = daysToPublish;
  }

  public void setDccRevocation(final DccRevocation dccRevocation) {
    this.dccRevocation = dccRevocation;
  }

  public void setDefaultArchiveName(final String defaultArchiveName) {
    this.defaultArchiveName = defaultArchiveName;
  }

  public void setDigitalGreenCertificate(final DigitalGreenCertificate digitalGreenCertificate) {
    this.digitalGreenCertificate = digitalGreenCertificate;
  }

  public void setEuPackageName(final String euPackageName) {
    this.euPackageName = euPackageName;
  }

  public void setExpiryPolicyMinutes(final Integer expiryPolicyMinutes) {
    this.expiryPolicyMinutes = expiryPolicyMinutes;
  }

  public void setIncludeIncompleteDays(final Boolean includeIncompleteDays) {
    this.includeIncompleteDays = includeIncompleteDays;
  }

  public void setIncludeIncompleteHours(final Boolean includeIncompleteHours) {
    this.includeIncompleteHours = includeIncompleteHours;
  }

  public void setInfectionThreshold(final int infectionThreshold) {
    this.infectionThreshold = infectionThreshold;
  }

  public void setIosQrCodePosterTemplate(final QrCodePosterTemplate iosQrCodePosterTemplate) {
    this.iosQrCodePosterTemplate = iosQrCodePosterTemplate;
  }

  public void setMaximumNumberOfKeysPerBundle(final Integer maximumNumberOfKeysPerBundle) {
    this.maximumNumberOfKeysPerBundle = maximumNumberOfKeysPerBundle;
  }

  public void setMinimumTrlValueAllowed(final Integer minimumTrlValueAllowed) {
    this.minimumTrlValueAllowed = minimumTrlValueAllowed;
  }

  public void setObjectStore(
      final ObjectStore objectStore) {
    this.objectStore = objectStore;
  }

  public void setOutputFileName(final String outputFileName) {
    this.outputFileName = outputFileName;
  }

  public void setOutputFileNameV2(final String outputFileNameV2) {
    this.outputFileNameV2 = outputFileNameV2;
  }

  public void setPaths(final Paths paths) {
    this.paths = paths;
  }

  public void setPresenceTracingParameters(
      final PresenceTracingParameters presenceTracingParameters) {
    this.presenceTracingParameters = presenceTracingParameters;
  }

  public void setRetentionDays(final Integer retentionDays) {
    this.retentionDays = retentionDays;
  }

  public void setShiftingPolicyThreshold(final Integer shiftingPolicyThreshold) {
    this.shiftingPolicyThreshold = shiftingPolicyThreshold;
  }

  public void setSignature(final Signature signature) {
    this.signature = signature;
  }

  public void setSrsTypeStatisticsDays(final int srsTypeStatisticsDays) {
    this.srsTypeStatisticsDays = srsTypeStatisticsDays;
  }

  public void setStatistics(final StatisticsConfig statistics) {
    this.statistics = statistics;
  }

  public void setSupportedCountries(final String[] supportedCountries) {
    this.supportedCountries = supportedCountries;
  }

  public void setTekExport(final TekExport tekExport) {
    this.tekExport = tekExport;
  }

  public void setTestData(final TestData testData) {
    this.testData = testData;
  }
}
