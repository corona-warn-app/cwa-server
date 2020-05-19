package app.coronawarn.server.services.distribution.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "services.distribution")
public class DistributionServiceConfig {

  private Paths paths;
  private TestData testData;
  private Integer retentionDays;

  public Paths getPaths() {
    return paths;
  }

  public void setPaths(Paths paths) {
    this.paths = paths;
  }

  public TestData getTestData() {
    return testData;
  }

  public void setTestData(TestData testData) {
    this.testData = testData;
  }

  public Integer getRetentionDays() {
    return retentionDays;
  }

  public void setRetentionDays(Integer retentionDays) {
    this.retentionDays = retentionDays;
  }

  public String getPrivatekeyPath() {
    return paths.getPrivatekey();
  }

  public String getCertificatePath() {
    return paths.getCertificate();
  }

  public String getOutputPath() {
    return paths.getOutput();
  }

  public Integer getSeed() {
    return testData.getSeed();
  }

  public Integer getExposuresPerHour() {
    return testData.getExposuresPerHour();
  }

  private static class TestData {

    private Integer seed;
    private Integer exposuresPerHour;

    public Integer getSeed() {
      return seed;
    }

    public void setSeed(Integer seed) {
      this.seed = seed;
    }

    public Integer getExposuresPerHour() {
      return exposuresPerHour;
    }

    public void setExposuresPerHour(Integer exposuresPerHour) {
      this.exposuresPerHour = exposuresPerHour;
    }
  }

  private static class Paths {

    private String privatekey;
    private String certificate;
    private String output;

    public String getPrivatekey() {
      return privatekey;
    }

    public void setPrivatekey(String privatekey) {
      this.privatekey = privatekey;
    }

    public String getCertificate() {
      return certificate;
    }

    public void setCertificate(String certificate) {
      this.certificate = certificate;
    }

    public String getOutput() {
      return output;
    }

    public void setOutput(String output) {
      this.output = output;
    }
  }
}
