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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "services.federation-download")
@Validated
public class FederationDownloadServiceConfig {

  private Integer connectionPoolSize;
  private FederationGateway federationGateway;

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

  public FederationGateway getFederationGateway() {
    return federationGateway;
  }

  public static class Ssl {

    private String keyStorePath;
    private String keyStorePass;
    private String certificateType;

    public String getKeyStorePath() {
      return keyStorePath;
    }

    public void setKeyStorePath(String keyStorePath) {
      this.keyStorePath = keyStorePath;
    }

    public String getKeyStorePass() {
      return keyStorePass;
    }

    public void setKeyStorePass(String keyStorePass) {
      this.keyStorePass = keyStorePass;
    }

    public String getCertificateType() {
      return certificateType;
    }

    public void setCertificateType(String certificateType) {
      this.certificateType = certificateType;
    }
  }

  public static class FederationGateway {
    private String baseUrl;

    private String path;

    private Ssl ssl;

    public Ssl getSsl() {
      return ssl;
    }

    public void setSsl(Ssl ssl) {
      this.ssl = ssl;
    }

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
}
