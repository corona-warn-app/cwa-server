package app.coronawarn.server.services.distribution.assembly.configuration;

import app.coronawarn.server.common.protocols.external.exposurenotification.SignatureInfo;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "services.distribution.signature")
public class SignatureConfigurationProperties {

  private String androidPackageName;
  private String appBundleId;
  private String algorithm;
  private String verificationKeyId;
  private String verificationKeyVersion;

  public String getAndroidPackageName() {
    return androidPackageName;
  }

  public void setAndroidPackageName(String androidPackageName) {
    this.androidPackageName = androidPackageName;
  }

  public String getAppBundleId() {
    return appBundleId;
  }

  public void setAppBundleId(String appBundleId) {
    this.appBundleId = appBundleId;
  }

  public String getAlgorithm() {
    return algorithm;
  }

  public void setAlgorithm(String algorithm) {
    this.algorithm = algorithm;
  }

  public String getVerificationKeyId() {
    return verificationKeyId;
  }

  public void setVerificationKeyId(String verificationKeyId) {
    this.verificationKeyId = verificationKeyId;
  }

  public String getVerificationKeyVersion() {
    return verificationKeyVersion;
  }

  public void setVerificationKeyVersion(String verificationKeyVersion) {
    this.verificationKeyVersion = verificationKeyVersion;
  }

  /**
   * Returns the static {@link SignatureInfo} configured in the application properties.
   */
  public SignatureInfo getSignatureInfo() {
    return SignatureInfo.newBuilder()
        .setAppBundleId(this.appBundleId)
        .setAndroidPackage(this.androidPackageName)
        .setVerificationKeyVersion(this.verificationKeyVersion)
        .setVerificationKeyId(this.verificationKeyId)
        .setSignatureAlgorithm(this.algorithm)
        .build();
  }
}
