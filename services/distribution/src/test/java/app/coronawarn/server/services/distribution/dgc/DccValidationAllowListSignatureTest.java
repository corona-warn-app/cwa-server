package app.coronawarn.server.services.distribution.dgc;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.AllowList;
import app.coronawarn.server.services.distribution.dgc.client.TestDigitalCovidCertificateClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

import static app.coronawarn.server.common.shared.util.SecurityUtils.getPublicKeyFromString;
import static org.assertj.core.api.Assertions.assertThat;


@EnableConfigurationProperties(value = DistributionServiceConfig.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DistributionServiceConfig.class},
    initializers = ConfigDataApplicationContextInitializer.class)
public class DccValidationAllowListSignatureTest {

  @Autowired
  private DistributionServiceConfig distributionServiceConfig;

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
}
