package app.coronawarn.server.services.eventregistration.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "event-registration")
public class EventRegistrationConfiguration {

  private Integer version;

  private String privateKey;
  private Signature signature;

  public static class Signature {


    private String algorithmName;
    private String securityProvider;

    public String getSecurityProvider() {
      return securityProvider;
    }

    public void setSecurityProvider(String securityProvider) {
      this.securityProvider = securityProvider;
    }

    public String getAlgorithmName() {
      return algorithmName;
    }

    public void setAlgorithmName(String algorithmName) {
      this.algorithmName = algorithmName;
    }

  }

  public Integer getVersion() {
    return version;
  }

  public void setVersion(Integer version) {
    this.version = version;
  }

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
}
