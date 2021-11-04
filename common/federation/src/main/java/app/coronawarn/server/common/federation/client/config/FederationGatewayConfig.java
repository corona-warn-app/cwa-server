
package app.coronawarn.server.common.federation.client.config;

import java.io.File;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "federation-gateway")
@Validated
public class FederationGatewayConfig {

  private Ssl ssl;
  private Integer connectionPoolSize;
  private String baseUrl;

  public Ssl getSsl() {
    return ssl;
  }

  public void setSsl(Ssl ssl) {
    this.ssl = ssl;
  }

  public Integer getConnectionPoolSize() {
    return connectionPoolSize;
  }

  public void setConnectionPoolSize(Integer connectionPoolSize) {
    this.connectionPoolSize = connectionPoolSize;
  }

  public String getBaseUrl() {
    return baseUrl;
  }

  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  public static class Ssl {

    private File keyStore;
    private String keyStorePassword;
    private File trustStore;
    private String trustStorePassword;

    public File getTrustStore() {
      return trustStore;
    }

    public void setTrustStore(File trustStore) {
      this.trustStore = trustStore;
    }

    public String getTrustStorePassword() {
      return trustStorePassword;
    }

    public void setTrustStorePassword(String trustStorePassword) {
      this.trustStorePassword = trustStorePassword;
    }

    public File getKeyStore() {
      return keyStore;
    }

    public void setKeyStore(File keyStore) {
      this.keyStore = keyStore;
    }

    public String getKeyStorePassword() {
      return keyStorePassword;
    }

    public void setKeyStorePassword(String keyStorePassword) {
      this.keyStorePassword = keyStorePassword;
    }

  }
}
