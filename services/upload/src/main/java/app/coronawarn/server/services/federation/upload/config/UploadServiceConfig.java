package app.coronawarn.server.services.federation.upload.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "services.upload")
public class UploadServiceConfig {

  private Integer minBatchKeyCount;
  private Integer maxBatchKeyCount;
  private String privateKey;
  private String privateKeyPassword;
  private String certificate;
  private Signature signature;
  private TestData testData;


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

  public static class TestData {
    private int keys;

    public int getKeys() {
      return keys;
    }

    public void setKeys(int keys) {
      this.keys = keys;
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
