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

package app.coronawarn.server.services.submission.config;

import java.io.File;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "services.submission")
public class SubmissionServiceConfig {

  // Exponential moving average of the last N real request durations (in ms), where
  // N = fakeDelayMovingAverageSamples.
  private Long initialFakeDelayMilliseconds;
  private Long fakeDelayMovingAverageSamples;
  private Integer retentionDays;
  private Integer connectionPoolSize;
  private Payload payload;
  private Verification verification;
  private Monitoring monitoring;
  private Client client;

  public Long getInitialFakeDelayMilliseconds() {
    return initialFakeDelayMilliseconds;
  }

  public void setInitialFakeDelayMilliseconds(Long initialFakeDelayMilliseconds) {
    this.initialFakeDelayMilliseconds = initialFakeDelayMilliseconds;
  }

  public Long getFakeDelayMovingAverageSamples() {
    return fakeDelayMovingAverageSamples;
  }

  public void setFakeDelayMovingAverageSamples(Long fakeDelayMovingAverageSamples) {
    this.fakeDelayMovingAverageSamples = fakeDelayMovingAverageSamples;
  }

  public Integer getRetentionDays() {
    return retentionDays;
  }

  public void setRetentionDays(Integer retentionDays) {
    this.retentionDays = retentionDays;
  }

  public Integer getConnectionPoolSize() {
    return connectionPoolSize;
  }

  public void setConnectionPoolSize(Integer connectionPoolSize) {
    this.connectionPoolSize = connectionPoolSize;
  }

  public Integer getMaxNumberOfKeys() {
    return payload.getMaxNumberOfKeys();
  }

  public void setPayload(Payload payload) {
    this.payload = payload;
  }

  private static class Payload {

    private Integer maxNumberOfKeys;

    public Integer getMaxNumberOfKeys() {
      return maxNumberOfKeys;
    }

    public void setMaxNumberOfKeys(Integer maxNumberOfKeys) {
      this.maxNumberOfKeys = maxNumberOfKeys;
    }
  }

  public String getVerificationBaseUrl() {
    return verification.getBaseUrl();
  }

  public void setVerification(Verification verification) {
    this.verification = verification;
  }

  public String getVerificationPath() {
    return verification.getPath();
  }

  private static class Verification {
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

  private static class Monitoring {
    private Long batchSize;

    public Long getBatchSize() {
      return batchSize;
    }

    public void setBatchSize(Long batchSize) {
      this.batchSize = batchSize;
    }
  }

  public Monitoring getMonitoring() {
    return monitoring;
  }

  public void setMonitoring(Monitoring monitoring) {
    this.monitoring = monitoring;
  }

  public Long getMonitoringBatchSize() {
    return this.monitoring.getBatchSize();
  }

  public void setMonitoringBatchSize(Long batchSize) {
    this.monitoring.setBatchSize(batchSize);
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
