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
    private String keyStorePass;
    private String certificateType;
    private String certificateDn;
    private String certificateSha;

    public String getCertificateDn() {
      return certificateDn;
    }

    public void setCertificateDn(String certificateDn) {
      this.certificateDn = certificateDn;
    }

    public String getCertificateSha() {
      return certificateSha;
    }

    public void setCertificateSha(String certificateSha) {
      this.certificateSha = certificateSha;
    }

    public File getKeyStore() {
      return keyStore;
    }

    public void setKeyStore(File keyStore) {
      this.keyStore = keyStore;
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
}
