package app.coronawarn.server.services.submission.config;

import app.coronawarn.server.common.persistence.domain.config.TekFieldDerivations;
import app.coronawarn.server.common.persistence.domain.config.TrlDerivations;
import java.io.File;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "services.submission")
@Validated
public class SubmissionServiceConfig {

  private static final String PATH_REGEX = "^[/]?[a-zA-Z0-9_]{1,1024}(/[a-zA-Z0-9_]{1,1024}){0,256}[/]?$";
  private static final String URL_WITH_PORT_REGEX =
      "^http[s]?://[a-z0-9-]{1,1024}(\\.[a-z0-9-]{1,1024}){0,256}(:[0-9]{2,6})?$";

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
  @Max(25)
  private Integer randomCheckinsPaddingMultiplier;
  @Min(1)
  @Max(9999)
  private Integer maxAllowedCheckinsPerDay;
  @NotEmpty
  private String randomCheckinsPaddingPepper;
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
  private Integer minRollingPeriod;

  @Autowired
  private TekFieldDerivations tekFieldDerivations;

  @Autowired
  private TrlDerivations trlDerivations;


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

  public Integer getRandomCheckinsPaddingMultiplier() {
    return randomCheckinsPaddingMultiplier;
  }

  public void setRandomCheckinsPaddingMultiplier(Integer randomCheckinsPaddingMultiplier) {
    this.randomCheckinsPaddingMultiplier = randomCheckinsPaddingMultiplier;
  }

  public String getRandomCheckinsPaddingPepper() {
    return randomCheckinsPaddingPepper;
  }

  public byte[] getRandomCheckinsPaddingPepperAsByteArray() {
    return Hex.decode(randomCheckinsPaddingPepper.getBytes());
  }

  public void setRandomCheckinsPaddingPepper(String randomCheckinsPaddingPepper) {
    this.randomCheckinsPaddingPepper = randomCheckinsPaddingPepper;
  }

  public Integer getMaxAllowedCheckinsPerDay() {
    return maxAllowedCheckinsPerDay;
  }

  public void setMaxAllowedCheckinsPerDay(Integer maxAllowedCheckinsPerDay) {
    this.maxAllowedCheckinsPerDay = maxAllowedCheckinsPerDay;
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

  public Integer getMinRollingPeriod() {
    return minRollingPeriod;
  }

  public void setMinRollingPeriod(Integer minRollingPeriod) {
    this.minRollingPeriod = minRollingPeriod;
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

  public Integer getAcceptedEventDateThresholdDays() {
    return payload.checkins.getAcceptedEventDateThresholdDays();
  }

  public void setAcceptedEventDateThresholdDays(Integer acceptedEventDateThresholdDays) {
    this.payload.checkins.setAcceptedEventDateThresholdDays(acceptedEventDateThresholdDays);
  }

  public void setPayload(Payload payload) {
    this.payload = payload;
  }

  public TekFieldDerivations getTekFieldDerivations() {
    return tekFieldDerivations;
  }

  public void setTekFieldDerivations(TekFieldDerivations tekFieldDerivations) {
    this.tekFieldDerivations = tekFieldDerivations;
  }

  public TrlDerivations getTrlDerivations() {
    return trlDerivations;
  }

  public void setTrlDerivations(TrlDerivations trlDerivations) {
    this.trlDerivations = trlDerivations;
  }

  public static class Payload {

    @Min(7)
    @Max(100)
    private Integer maxNumberOfKeys;
    @NotEmpty
    private String[] supportedCountries;
    private String defaultOriginCountry;
    private Checkins checkins;

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

    public Checkins getCheckins() {
      return checkins;
    }

    public void setCheckins(Checkins checkins) {
      this.checkins = checkins;
    }

    public static class Checkins {

      @NotNull
      private Integer acceptedEventDateThresholdDays;

      public Integer getAcceptedEventDateThresholdDays() {
        return acceptedEventDateThresholdDays;
      }

      public void setAcceptedEventDateThresholdDays(Integer acceptedEventDateThresholdDays) {
        this.acceptedEventDateThresholdDays = acceptedEventDateThresholdDays;
      }
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
}
