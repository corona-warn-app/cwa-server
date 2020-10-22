

package app.coronawarn.server.services.federation.upload.config;

import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "services.upload")
public class UploadServiceConfig {

  private Integer expiryPolicyMinutes;
  private Integer minBatchKeyCount;
  private Integer maxBatchKeyCount;
  private String privateKey;
  private String privateKeyPassword;
  private String certificate;
  private Integer retentionDays;
  private Signature signature;
  private TestData testData;
  private EfgsTransmission efgsTransmission;

  public Integer getRetentionDays() {
    return retentionDays;
  }

  public void setRetentionDays(Integer retentionDays) {
    this.retentionDays = retentionDays;
  }

  public EfgsTransmission getEfgsTransmission() {
    return efgsTransmission;
  }

  public void setEfgsTransmission(EfgsTransmission efgsTransmission) {
    this.efgsTransmission = efgsTransmission;
  }

  public Integer getExpiryPolicyMinutes() {
    return expiryPolicyMinutes;
  }

  public void setExpiryPolicyMinutes(Integer expiryPolicyMinutes) {
    this.expiryPolicyMinutes = expiryPolicyMinutes;
  }

  public Integer getMinBatchKeyCount() {
    return minBatchKeyCount;
  }

  public void setMinBatchKeyCount(Integer minBatchKeyCount) {
    this.minBatchKeyCount = minBatchKeyCount;
  }

  public Integer getMaxBatchKeyCount() {
    return maxBatchKeyCount;
  }

  public void setMaxBatchKeyCount(Integer maxBatchKeyCount) {
    this.maxBatchKeyCount = maxBatchKeyCount;
  }

  public String getCertificate() {
    return certificate;
  }

  public TestData getTestData() {
    return testData;
  }

  public void setTestData(TestData testData) {
    this.testData = testData;
  }

  public void setCertificate(String certificate) {
    this.certificate = certificate;
  }

  public String getPrivateKey() {
    return privateKey;
  }

  public void setPrivateKey(String privateKey) {
    this.privateKey = privateKey;
  }

  public String getPrivateKeyPassword() {
    return privateKeyPassword;
  }

  public void setPrivateKeyPassword(String privateKeyPassword) {
    this.privateKeyPassword = privateKeyPassword;
  }

  public Signature getSignature() {
    return signature;
  }

  public void setSignature(Signature signature) {
    this.signature = signature;
  }

  public static class EfgsTransmission {
    private boolean enableDsos;
    private boolean enableReportType;
    private Integer defaultDsos;
    private ReportType defaultReportType;

    public ReportType getDefaultReportType() {
      return defaultReportType;
    }

    public void setDefaultReportType(ReportType defaultReportType) {
      this.defaultReportType = defaultReportType;
    }

    public boolean isEnableDsos() {
      return enableDsos;
    }

    public void setEnableDsos(boolean enableDsos) {
      this.enableDsos = enableDsos;
    }

    public boolean isEnableReportType() {
      return enableReportType;
    }

    public void setEnableReportType(boolean enableReportType) {
      this.enableReportType = enableReportType;
    }

    public Integer getDefaultDsos() {
      return defaultDsos;
    }

    public void setDefaultDsos(Integer defaultDsos) {
      this.defaultDsos = defaultDsos;
    }
  }

  public static class TestData {
    private int maxPendingKeys;

    public int getMaxPendingKeys() {
      return maxPendingKeys;
    }

    public void setMaxPendingKeys(int maxPendingKeys) {
      this.maxPendingKeys = maxPendingKeys;
    }
  }

  public static class Signature {

    private String algorithmOid;
    private String algorithmName;
    private String securityProvider;

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

    public String getSecurityProvider() {
      return securityProvider;
    }

    public void setSecurityProvider(String securityProvider) {
      this.securityProvider = securityProvider;
    }
  }
}
