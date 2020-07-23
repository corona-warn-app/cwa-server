/*-
 * ---license-start
 * Corona-Warn-App
 * ---
 * Copyright (C) 2020 SAP SE and all other contributors
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ---license-end
 */

package app.coronawarn.server.services.distribution.config;

import app.coronawarn.server.common.protocols.external.exposurenotification.SignatureInfo;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "services.distribution")
@Validated
public class DistributionServiceConfig {

  private static final String PATH_REGEX = "^[/]?[a-zA-Z0-9_]+(/[a-zA-Z0-9_]+)*[/]?$";
  private static final String FILE_NAME_REGEX = "^[a-zA-Z0-9_-]+$";
  private static final String FILE_NAME_WITH_TYPE_REGEX = "^[a-zA-Z0-9_-]+\\.[a-z]+$";
  private static final String CHAR_AND_NUMBER_REGEX = "^[a-zA-Z0-9_-]+$";
  private static final String CHAR_NUMBER_AND_SPACE_REGEX = "^[a-zA-Z0-9_\\s]+$";
  private static final String NO_WHITESPACE_REGEX = "^[\\S]+$";
  private static final String URL_REGEX = "^http[s]?://[a-z0-9-]+([\\./][a-z0-9-]+)*[/]?$";
  private static final String NUMBER_REGEX = "^[0-9]+$";
  private static final String VERSION_REGEX = "^v[0-9]+$";
  private static final String ALGORITHM_OID_REGEX = "^[0-9]+[\\.[0-9]+]*$";
  private static final String BUNDLE_REGEX = "^[a-z-]+[\\.[a-z-]+]*$";
  private static final String PRIVATE_KEY_REGEX = "^(classpath:|file://)[/]?[a-zA-Z0-9_]+[/[a-zA-Z0-9_]+]*(.pem)?$";

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
  private TekExport tekExport;
  private Signature signature;
  private Api api;
  private ObjectStore objectStore;

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
    private String countryGermany;
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

    public String getCountryGermany() {
      return countryGermany;
    }

    public void setCountryGermany(String countryGermany) {
      this.countryGermany = countryGermany;
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
  }
}
