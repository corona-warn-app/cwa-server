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

  public static class Client {

    public static class Ssl {

      private String keyPassword;

      private File keyStore;

      private String keyStorePassword;

      private File trustStore;

      private String trustStorePassword;

      public String getKeyPassword() {
        return keyPassword;
      }

      public File getKeyStore() {
        return keyStore;
      }

      public String getKeyStorePassword() {
        return keyStorePassword;
      }

      public File getTrustStore() {
        return trustStore;
      }

      public String getTrustStorePassword() {
        return trustStorePassword;
      }

      public void setKeyPassword(final String keyPassword) {
        this.keyPassword = keyPassword;
      }

      public void setKeyStore(final File keyStore) {
        this.keyStore = keyStore;
      }

      public void setKeyStorePassword(final String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
      }

      public void setTrustStore(final File trustStore) {
        this.trustStore = trustStore;
      }

      public void setTrustStorePassword(final String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
      }
    }

    private Ssl ssl;

    public Ssl getSsl() {
      return ssl;
    }

    public void setSsl(final Ssl ssl) {
      this.ssl = ssl;
    }
  }

  public static class FeignRetry {

    @Min(1)
    @Max(100)
    private int maxAttempts;

    @Min(1)
    @Max(120)
    private long maxPeriod;

    @Min(100)
    @Max(10000)
    private long period;

    public int getMaxAttempts() {
      return maxAttempts;
    }

    public long getMaxPeriod() {
      return maxPeriod;
    }

    public long getPeriod() {
      return period;
    }

    public void setMaxAttempts(final int maxAttempts) {
      this.maxAttempts = maxAttempts;
    }

    public void setMaxPeriod(final long maxPeriod) {
      this.maxPeriod = maxPeriod;
    }

    public void setPeriod(final long period) {
      this.period = period;
    }
  }

  private static class Monitoring {

    @Min(1)
    @Max(1000)
    private Long batchSize;

    public Long getBatchSize() {
      return batchSize;
    }

    public void setBatchSize(final Long batchSize) {
      this.batchSize = batchSize;
    }
  }

  public static class Payload {

    public static class Checkins {

      @NotNull
      private Integer acceptedEventDateThresholdDays;

      public Integer getAcceptedEventDateThresholdDays() {
        return acceptedEventDateThresholdDays;
      }

      public void setAcceptedEventDateThresholdDays(final Integer acceptedEventDateThresholdDays) {
        this.acceptedEventDateThresholdDays = acceptedEventDateThresholdDays;
      }
    }

    private Checkins checkins;

    private String defaultOriginCountry;

    @Min(7)
    @Max(100)
    private Integer maxNumberOfKeys;

    @NotEmpty
    private String[] supportedCountries;

    public Checkins getCheckins() {
      return checkins;
    }

    public String getDefaultOriginCountry() {
      return defaultOriginCountry;
    }

    public Integer getMaxNumberOfKeys() {
      return maxNumberOfKeys;
    }

    public String[] getSupportedCountries() {
      return supportedCountries;
    }

    public void setCheckins(final Checkins checkins) {
      this.checkins = checkins;
    }

    public void setDefaultOriginCountry(final String defaultOriginCountry) {
      this.defaultOriginCountry = defaultOriginCountry;
    }

    public void setMaxNumberOfKeys(final Integer maxNumberOfKeys) {
      this.maxNumberOfKeys = maxNumberOfKeys;
    }

    public void setSupportedCountries(final String[] supportedCountries) {
      this.supportedCountries = supportedCountries;
    }
  }

  static class Verification {

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

    public void setBaseUrl(final String baseUrl) {
      this.baseUrl = baseUrl;
    }

    public void setPath(final String path) {
      this.path = path;
    }
  }

  private static final String PATH_REGEX = "^[/]?[a-zA-Z0-9_]{1,1024}(/[a-zA-Z0-9_]{1,1024}){0,256}[/]?$";

  private static final String URL_WITH_PORT_REGEX 
      = "^http[s]?://[a-z0-9-]{1,1024}(\\.[a-z0-9-]{1,1024}){0,256}(:[0-9]{2,6})?$";

  private Client client;

  @Min(1)
  @Max(10000)
  private Integer connectionPoolSize;

  /**
   * Exponential moving average of the last N real request durations (in ms), where N = fakeDelayMovingAverageSamples.
   */
  @Min(1)
  @Max(100)
  private Long fakeDelayMovingAverageSamples;

  private FeignRetry feignRetry;

  @Min(1)
  @Max(3000)
  private Long initialFakeDelayMilliseconds;

  @Min(1)
  @Max(9999)
  private Integer maxAllowedCheckinsPerDay;

  private DataSize maximumRequestSize;

  @Min(1)
  @Max(100000000)
  private int maxKeysPerDay;

  @Min(1)
  @Max(144)
  private Integer maxRollingPeriod;

  @Min(1)
  @Max(144)
  private Integer minRollingPeriod;

  private Monitoring monitoring;

  private Payload payload;

  @Min(1)
  @Max(25)
  private Integer randomCheckinsPaddingMultiplier;

  @NotEmpty
  private String randomCheckinsPaddingPepper;

  @Min(1)
  @Max(25)
  private Integer randomKeyPaddingMultiplier;

  @Min(7)
  @Max(28)
  private Integer retentionDays;

  @Min(0)
  @Max(28)
  private int srsDays;

  private Verification srsVerify;

  @Autowired
  private TekFieldDerivations tekFieldDerivations;

  @Autowired
  private TrlDerivations trlDerivations;

  /**
   * unencryptedCheckinsEnabled.
   *
   * @deprecated should be removed when false
   */
  @Deprecated(since = "2.8", forRemoval = false)
  private Boolean unencryptedCheckinsEnabled;

  private Verification verification;

  public Integer getAcceptedEventDateThresholdDays() {
    return payload.checkins.getAcceptedEventDateThresholdDays();
  }

  public Client getClient() {
    return client;
  }

  public Integer getConnectionPoolSize() {
    return connectionPoolSize;
  }

  public String getDefaultOriginCountry() {
    return payload.defaultOriginCountry;
  }

  public Long getFakeDelayMovingAverageSamples() {
    return fakeDelayMovingAverageSamples;
  }

  public FeignRetry getFeignRetry() {
    return feignRetry;
  }

  public Long getInitialFakeDelayMilliseconds() {
    return initialFakeDelayMilliseconds;
  }

  public Integer getMaxAllowedCheckinsPerDay() {
    return maxAllowedCheckinsPerDay;
  }

  public DataSize getMaximumRequestSize() {
    return maximumRequestSize;
  }

  public int getMaxKeysPerDay() {
    return maxKeysPerDay;
  }

  public Integer getMaxNumberOfKeys() {
    return payload.getMaxNumberOfKeys();
  }

  public Integer getMaxRollingPeriod() {
    return maxRollingPeriod;
  }

  public Integer getMinRollingPeriod() {
    return minRollingPeriod;
  }

  public Monitoring getMonitoring() {
    return monitoring;
  }

  public Long getMonitoringBatchSize() {
    return monitoring.getBatchSize();
  }

  public Integer getRandomCheckinsPaddingMultiplier() {
    return randomCheckinsPaddingMultiplier;
  }

  public String getRandomCheckinsPaddingPepper() {
    return randomCheckinsPaddingPepper;
  }

  public byte[] getRandomCheckinsPaddingPepperAsByteArray() {
    return Hex.decode(randomCheckinsPaddingPepper.getBytes());
  }

  public Integer getRandomKeyPaddingMultiplier() {
    return randomKeyPaddingMultiplier;
  }

  public Integer getRetentionDays() {
    return retentionDays;
  }

  public int getSrsDays() {
    return Math.min(srsDays, retentionDays);
  }

  Verification getSrsVerify() {
    return srsVerify;
  }

  public String getSrsVerifyBaseUrl() {
    return getSrsVerify().getBaseUrl();
  }

  public String getSrsVerifyPath() {
    return getSrsVerify().getPath();
  }

  public String[] getSupportedCountries() {
    return payload.getSupportedCountries();
  }

  public TekFieldDerivations getTekFieldDerivations() {
    return tekFieldDerivations;
  }

  public TrlDerivations getTrlDerivations() {
    return trlDerivations;
  }

  public String getVerificationBaseUrl() {
    return verification.getBaseUrl();
  }

  public String getVerificationPath() {
    return verification.getPath();
  }

  /**
   * unencryptedCheckinsEnabled.
   *
   * @deprecated should be removed when false
   */
  @Deprecated(since = "2.8", forRemoval = false)
  public Boolean isUnencryptedCheckinsEnabled() {
    return unencryptedCheckinsEnabled == null ? false : unencryptedCheckinsEnabled;
  }

  public void setAcceptedEventDateThresholdDays(final Integer acceptedEventDateThresholdDays) {
    payload.checkins.setAcceptedEventDateThresholdDays(acceptedEventDateThresholdDays);
  }

  public void setClient(final Client client) {
    this.client = client;
  }

  public void setConnectionPoolSize(final Integer connectionPoolSize) {
    this.connectionPoolSize = connectionPoolSize;
  }

  public void setFakeDelayMovingAverageSamples(final Long fakeDelayMovingAverageSamples) {
    this.fakeDelayMovingAverageSamples = fakeDelayMovingAverageSamples;
  }

  public void setFeignRetry(final FeignRetry feignRetry) {
    this.feignRetry = feignRetry;
  }

  public void setInitialFakeDelayMilliseconds(final Long initialFakeDelayMilliseconds) {
    this.initialFakeDelayMilliseconds = initialFakeDelayMilliseconds;
  }

  public void setMaxAllowedCheckinsPerDay(final Integer maxAllowedCheckinsPerDay) {
    this.maxAllowedCheckinsPerDay = maxAllowedCheckinsPerDay;
  }

  public void setMaximumRequestSize(final DataSize maximumRequestSize) {
    this.maximumRequestSize = maximumRequestSize;
  }

  public void setMaxKeysPerDay(final int maxKeysPerDay) {
    this.maxKeysPerDay = maxKeysPerDay;
  }

  public void setMaxRollingPeriod(final Integer maxRollingPeriod) {
    this.maxRollingPeriod = maxRollingPeriod;
  }

  public void setMinRollingPeriod(final Integer minRollingPeriod) {
    this.minRollingPeriod = minRollingPeriod;
  }

  public void setMonitoring(final Monitoring monitoring) {
    this.monitoring = monitoring;
  }

  public void setMonitoringBatchSize(final Long batchSize) {
    monitoring.setBatchSize(batchSize);
  }

  public void setPayload(final Payload payload) {
    this.payload = payload;
  }

  public void setRandomCheckinsPaddingMultiplier(final Integer randomCheckinsPaddingMultiplier) {
    this.randomCheckinsPaddingMultiplier = randomCheckinsPaddingMultiplier;
  }

  public void setRandomCheckinsPaddingPepper(final String randomCheckinsPaddingPepper) {
    this.randomCheckinsPaddingPepper = randomCheckinsPaddingPepper;
  }

  public void setRandomKeyPaddingMultiplier(final Integer randomKeyPaddingMultiplier) {
    this.randomKeyPaddingMultiplier = randomKeyPaddingMultiplier;
  }

  public void setRetentionDays(final Integer retentionDays) {
    this.retentionDays = retentionDays;
  }

  public void setSrsDays(final int srsDays) {
    this.srsDays = srsDays;
  }

  void setSrsVerify(final Verification srsVerify) {
    this.srsVerify = srsVerify;
  }

  public void setSupportedCountries(final String[] supportedCountries) {
    payload.setSupportedCountries(supportedCountries);
  }

  public void setTekFieldDerivations(final TekFieldDerivations tekFieldDerivations) {
    this.tekFieldDerivations = tekFieldDerivations;
  }

  public void setTrlDerivations(final TrlDerivations trlDerivations) {
    this.trlDerivations = trlDerivations;
  }

  public void setUnencryptedCheckinsEnabled(final Boolean unencryptedCheckinsEnabled) {
    this.unencryptedCheckinsEnabled = unencryptedCheckinsEnabled;
  }

  public void setVerification(final Verification verification) {
    this.verification = verification;
  }
}
