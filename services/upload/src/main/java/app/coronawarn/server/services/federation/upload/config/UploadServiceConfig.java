package app.coronawarn.server.services.federation.upload.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "services.upload")
public class UploadServiceConfig {

  private String privateKey;
  private Signature signature;

  public String getPrivateKey() {
    return privateKey;
  }

  public void setPrivateKey(String privateKey) {
    this.privateKey = privateKey;
  }

  public Signature getSignature() {
    return signature;
  }

  public void setSignature(Signature signature) {
    this.signature = signature;
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
