package app.coronawarn.server.services.distribution.dgc;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.dgc.dsc.DigitalCovidValidationCertificateToProtobufMapping;
import app.coronawarn.server.services.distribution.dgc.dsc.errors.InvalidFingerprintException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@EnableConfigurationProperties(value = DistributionServiceConfig.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DistributionServiceConfig.class,
    DigitalCovidValidationCertificateToProtobufMapping.class},
    initializers = ConfigDataApplicationContextInitializer.class)
@ActiveProfiles("allow-list-invalid")
public class DccValidationAllowListInvalidSignatureIT {

  @Autowired
  private DistributionServiceConfig distributionServiceConfig;

  @Autowired
  private DigitalCovidValidationCertificateToProtobufMapping digitalCovidValidationCertificateToProtobufMapping;

  @Test
  void testInvalidSignature(){
    assertThatThrownBy(() -> {
      digitalCovidValidationCertificateToProtobufMapping
          .constructProtobufMapping();
    }).isExactlyInstanceOf(InvalidFingerprintException.class);
  }
}
