package app.coronawarn.server.services.distribution.dgc.dsc;

import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.dgc.ApacheHttpTestConfiguration;
import app.coronawarn.server.services.distribution.dgc.Certificates;
import app.coronawarn.server.services.distribution.dgc.dsc.decode.DscListDecoder;
import app.coronawarn.server.services.distribution.dgc.exception.FetchDscTrustListException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DistributionServiceConfig.class, ProdDigitalSigningCertificatesClient.class,
    CloudDscFeignClientConfiguration.class, CloudDscFeignHttpClientProvider.class, DscListDecoder.class,
    ApacheHttpTestConfiguration.class},
    initializers = ConfigDataApplicationContextInitializer.class)
@ImportAutoConfiguration({FeignAutoConfiguration.class, HttpMessageConvertersAutoConfiguration.class})
@ActiveProfiles("dsc-client-factory")
class DigitalSigningCertificatesIT {

  @Autowired
  private DigitalSigningCertificatesClient digitalSigningCertificatesClient;

  @Test
  void should_fetch_certificates() throws FetchDscTrustListException {
    Optional<Certificates> certificates = digitalSigningCertificatesClient.getDscTrustList();
    assertThat(certificates).isPresent();
  }
}
