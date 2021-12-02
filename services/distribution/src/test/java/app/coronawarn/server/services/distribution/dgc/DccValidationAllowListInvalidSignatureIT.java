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
class DccValidationAllowListInvalidSignatureIT {

  @Autowired
  private DigitalCovidValidationCertificateToProtobufMapping digitalCovidValidationCertificateToProtobufMapping;

  @Test
  void testInvalidFingerprint() {
    assertThatThrownBy(() -> {
      digitalCovidValidationCertificateToProtobufMapping
          .constructProtobufMapping();
    }).isExactlyInstanceOf(InvalidFingerprintException.class);
  }
}
