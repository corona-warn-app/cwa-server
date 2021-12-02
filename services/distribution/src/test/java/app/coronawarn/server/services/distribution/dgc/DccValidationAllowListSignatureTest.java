package app.coronawarn.server.services.distribution.dgc;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.AllowList;
import app.coronawarn.server.services.distribution.dgc.dsc.DigitalCovidValidationCertificateToProtobufMapping;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

import static app.coronawarn.server.common.shared.util.SecurityUtils.ecdsaSignatureVerification;
import static app.coronawarn.server.common.shared.util.SecurityUtils.getPublicKeyFromString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@EnableConfigurationProperties(value = DistributionServiceConfig.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    DistributionServiceConfig.class,
    DigitalCovidValidationCertificateToProtobufMapping.class
},
    initializers = ConfigDataApplicationContextInitializer.class)
class DccValidationAllowListSignatureTest {

  @Autowired
  private DistributionServiceConfig distributionServiceConfig;

  @Autowired
  private DigitalCovidValidationCertificateToProtobufMapping digitalCovidValidationCertificateToProtobufMapping;

  @Test
  void testLoadValuesForValidationServiceAllowList() throws NoSuchAlgorithmException, InvalidKeySpecException {
    byte[] signature = distributionServiceConfig.getDigitalGreenCertificate().getAllowListSignature();
    AllowList content = distributionServiceConfig.getDigitalGreenCertificate().getAllowList();
    PublicKey publicKey = getPublicKeyFromString(
        distributionServiceConfig.getDigitalGreenCertificate().getAllowListCertificate());

    assertThat(signature).isNotEmpty();
    assertThat(content).isNotNull();
    assertThat(publicKey).isNotNull();
  }

  @Test
  void testVerifySignature() throws NoSuchAlgorithmException, InvalidKeySpecException {
    String content = distributionServiceConfig.getDigitalGreenCertificate().getAllowListAsString();
    byte[] signature = distributionServiceConfig.getDigitalGreenCertificate().getAllowListSignature();
    PublicKey publicKey = getPublicKeyFromString(
        distributionServiceConfig.getDigitalGreenCertificate().getAllowListCertificate());

    //noinspection CatchMayIgnoreException
    try {
      ecdsaSignatureVerification(
          signature,
          publicKey,
          content.getBytes(StandardCharsets.UTF_8));
    } catch (Throwable t) {
      fail(t.getMessage());
    }
  }

  @Test
  void testValidateSchema() {
    AllowList allowList = distributionServiceConfig.getDigitalGreenCertificate().getAllowList();
    assertThat(digitalCovidValidationCertificateToProtobufMapping.validateSchema(allowList))
        .isTrue();
  }

  @Test
  void testConstructProtobufMapping() throws Exception {
    assertThat(digitalCovidValidationCertificateToProtobufMapping.constructProtobufMapping()).isPresent();
  }
}
