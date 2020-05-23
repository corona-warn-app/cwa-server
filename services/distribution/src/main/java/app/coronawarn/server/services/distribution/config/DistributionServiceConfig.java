package app.coronawarn.server.services.distribution.config;

import app.coronawarn.server.common.protocols.external.exposurenotification.SignatureInfo;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "services.distribution")
public class DistributionServiceConfig {

  private Paths paths;
  private TestData testData;
  private Integer retentionDays;
  private String outputFileName;
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

  public String getOutputFileName() {
    return outputFileName;
  }

  public void setOutputFileName(String outputFileName) {
    this.outputFileName = outputFileName;
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

    private String fileName;
    private String fileHeader;
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

    private String privateKey;
    private String certificate;
    private String output;

    public String getPrivateKey() {
      return privateKey;
    }

    public void setPrivateKey(String privateKey) {
      this.privateKey = privateKey;
    }

    public String getCertificate() {
      return certificate;
    }

    public void setCertificate(String certificate) {
      this.certificate = certificate;
    }

    public String getOutput() {
      return output;
    }

    public void setOutput(String output) {
      this.output = output;
    }
  }

  public static class Api {

    private String versionPath;
    private String versionV1;
    private String countryPath;
    private String countryGermany;
    private String datePath;
    private String hourPath;
    private String diagnosisKeysPath;
    private String parametersPath;
    private String parametersExposureConfigurationFileName;

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

    public String getParametersExposureConfigurationFileName() {
      return parametersExposureConfigurationFileName;
    }

    public void setParametersExposureConfigurationFileName(String parametersExposureConfigurationFileName) {
      this.parametersExposureConfigurationFileName = parametersExposureConfigurationFileName;
    }

    public String getParametersRiskScoreClassificationFileName() {
      return parametersRiskScoreClassificationFileName;
    }

    public void setParametersRiskScoreClassificationFileName(String parametersRiskScoreClassificationFileName) {
      this.parametersRiskScoreClassificationFileName = parametersRiskScoreClassificationFileName;
    }

    private String parametersRiskScoreClassificationFileName;
  }

  public static class Signature {

    private String appBundleId;
    private String androidPackage;
    private String verificationKeyId;
    private String verificationKeyVersion;
    private String algorithmOid;
    private String algorithmName;
    private String fileName;
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
     * Returns the static {@link SignatureInfo} configured in the application properties. TODO Enter correct values.
     */
    public SignatureInfo getSignatureInfo() {
      // TODO cwa-server#183 cwa-server#207 cwa-server#238
      return SignatureInfo.newBuilder()
        .setAppBundleId(this.getAppBundleId())
        .setAndroidPackage(this.getAndroidPackage())
        .setVerificationKeyVersion(this.getVerificationKeyVersion())
        .setVerificationKeyId(this.getVerificationKeyId())
        .setSignatureAlgorithm(this.getAlgorithmOid())
        .build();
    }
  }

  public static class ObjectStore {

    private String accessKey;
    private String secretKey;
    private String endpoint;
    private Integer port;
    private String bucket;
    private Boolean setPublicReadAclOnPutObject;

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

  }
}
