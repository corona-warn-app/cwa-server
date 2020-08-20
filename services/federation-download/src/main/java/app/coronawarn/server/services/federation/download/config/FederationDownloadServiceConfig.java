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

package app.coronawarn.server.services.federation.download.config;

import java.io.File;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "services.federation-download")
@Validated
public class FederationDownloadServiceConfig {

  private Integer connectionPoolSize;
  private FederationGateway federationGateway;
  private Client client;

  public Integer getConnectionPoolSize() {
    return connectionPoolSize;
  }

  public void setConnectionPoolSize(Integer connectionPoolSize) {
    this.connectionPoolSize = connectionPoolSize;
  }

  public String getFederationDownloadBaseUrl() {
    return federationGateway.getBaseUrl();
  }

  public void setFederationGateway(FederationGateway federationGateway) {
    this.federationGateway = federationGateway;
  }

  public String getFederationDownloadPath() {
    return federationGateway.getPath();
  }

  private static class FederationGateway {
    private String baseUrl;

    private String path;

    public String getBaseUrl() {
      return baseUrl;
    }

    public String getPath() {
      return path;
    }

    public void setBaseUrl(String baseUrl) {
      this.baseUrl = baseUrl;
    }

    public void setPath(String path) {
      this.path = path;
    }
  }

  public Client getClient() {
    return client;
  }

  public void setClient(Client client) {
    this.client = client;
  }

  public static class Client {

    private Ssl ssl;

    public Ssl getSsl() {
      return ssl;
    }

    public void setSsl(Ssl ssl) {
      this.ssl = ssl;
    }

    public static class Ssl {

      private File keyStore;
      private String keyStorePassword;
      private String keyPassword;
      private File trustStore;
      private String trustStorePassword;

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

      public String getKeyPassword() {
        return keyPassword;
      }

      public void setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
      }

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
    }
  }
}
