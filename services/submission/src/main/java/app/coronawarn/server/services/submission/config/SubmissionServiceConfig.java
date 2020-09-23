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
import java.util.Map;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "services.submission")
@Validated
public class SubmissionServiceConfig {

  private static final String PATH_REGEX = "^[/]?[a-zA-Z0-9_]+[/[a-zA-Z0-9_]+]*$";
  private static final String URL_WITH_PORT_REGEX = "^http[s]?://[a-z0-9-]+(\\.[a-z0-9-]+)*(:[0-9]{2,6})?$";

  // Exponential moving average of the last N real request durations (in ms), where
  // N = fakeDelayMovingAverageSamples.
  @Min(1)
  @Max(3000)
  private Long initialFakeDelayMilliseconds;
  @Min(1)
  @Max(100)
  private Long fakeDelayMovingAverageSamples;
  @Min(7)
  @Max(28)
  private Integer retentionDays;
  @Min(1)
  @Max(25)
  private Integer randomKeyPaddingMultiplier;
  @Min(1)
  @Max(10000)
  private Integer connectionPoolSize;
  private DataSize maximumRequestSize;
  private Payload payload;
  private Verification verification;
  private Monitoring monitoring;
  private Client client;
  @Min(0)
  @Max(144)
  private Integer maxRollingPeriod;


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

  public Integer getRandomKeyPaddingMultiplier() {
    return randomKeyPaddingMultiplier;
  }

  public void setRandomKeyPaddingMultiplier(Integer randomKeyPaddingMultiplier) {
    this.randomKeyPaddingMultiplier = randomKeyPaddingMultiplier;
  }

  public Integer getConnectionPoolSize() {
    return connectionPoolSize;
  }

  public void setConnectionPoolSize(Integer connectionPoolSize) {
    this.connectionPoolSize = connectionPoolSize;
  }

  public DataSize getMaximumRequestSize() {
    return maximumRequestSize;
  }

  public void setMaximumRequestSize(DataSize maximumRequestSize) {
    this.maximumRequestSize = maximumRequestSize;
  }

  public Integer getMaxRollingPeriod() {
    return maxRollingPeriod;
  }

  public void setMaxRollingPeriod(Integer maxRollingPeriod) {
    this.maxRollingPeriod = maxRollingPeriod;
  }

  public Integer getMaxNumberOfKeys() {
    return payload.getMaxNumberOfKeys();
  }

  public String getDefaultOriginCountry() {
    return payload.defaultOriginCountry;
  }

  public String[] getSupportedCountries() {
    return payload.getSupportedCountries();
  }

  public void setSupportedCountries(String[] supportedCountries) {
    payload.setSupportedCountries(supportedCountries);
  }

  public void setPayload(Payload payload) {
    this.payload = payload;
  }

  public TekFieldDerivations getTekFieldDerivations() {
    return payload.getTekFieldDerivations();
  }

  public static class Payload {

    @Min(7)
    @Max(100)
    private Integer maxNumberOfKeys;
    @NotEmpty
    private String[] supportedCountries;
    private String defaultOriginCountry;
    private TekFieldDerivations tekFieldDerivations;

    public Integer getMaxNumberOfKeys() {
      return maxNumberOfKeys;
    }

    public void setMaxNumberOfKeys(Integer maxNumberOfKeys) {
      this.maxNumberOfKeys = maxNumberOfKeys;
    }

    public String[] getSupportedCountries() {
      return supportedCountries;
    }

    public void setSupportedCountries(String[] supportedCountries) {
      this.supportedCountries = supportedCountries;
    }

    public String getDefaultOriginCountry() {
      return defaultOriginCountry;
    }

    public void setDefaultOriginCountry(String defaultOriginCountry) {
      this.defaultOriginCountry = defaultOriginCountry;
    }

    public TekFieldDerivations getTekFieldDerivations() {
      return tekFieldDerivations;
    }

    public void setTekFieldDerivations(TekFieldDerivations tekFieldDerivations) {
      this.tekFieldDerivations = tekFieldDerivations;
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

    @Pattern(regexp = URL_WITH_PORT_REGEX)
    private String baseUrl;

    @Pattern(regexp = PATH_REGEX)
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

    @Min(1)
    @Max(1000)
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

  /**
   * Wrapper over properties defined in the application.yaml which map DSOS to TRL
   * and vice-versa. These maps are used to derive each property from the other.
   */
  public static class TekFieldDerivations {
    @NotNull
    @NotEmpty
    private Map<Integer, Integer> dsosFromTrl;

    public Map<Integer, Integer> getDsosFromTrl() {
      return dsosFromTrl;
    }

    public void setDsosFromTrl(Map<Integer, Integer> dsosFromTrl) {
      this.dsosFromTrl = dsosFromTrl;
    }

    public Integer deriveDsosFromTrl(Integer trlValue) {
      // the derivation logic must be refined to take into account missing trl values.
      return dsosFromTrl.getOrDefault(trlValue, 0);
    }
  }
}
