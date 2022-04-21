package app.coronawarn.server.services.distribution.dgc.dsc;

import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.dgc.Certificates;
import app.coronawarn.server.services.distribution.dgc.exception.FetchDscTrustListException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DistributionServiceConfig.class, TestDigitalSigningCertificateClient.class},
    initializers = ConfigDataApplicationContextInitializer.class)
@ActiveProfiles("fake-dsc-client")
class DigitalSigningCertificatesClientSpringTest {

  @Autowired
  private DigitalSigningCertificatesClient digitalSigningCertificatesClient;

  @Test
  void should_retrieve_dsc_trust_list() throws FetchDscTrustListException {
    Optional<Certificates> certificates = digitalSigningCertificatesClient.getDscTrustList();
    assertThat(certificates).isNotEmpty();
  }
}
