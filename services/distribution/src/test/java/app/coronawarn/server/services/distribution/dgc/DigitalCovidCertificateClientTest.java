package app.coronawarn.server.services.distribution.dgc;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.dgc.client.CloudDccFeignClientConfiguration;
import app.coronawarn.server.services.distribution.dgc.client.DigitalCovidCertificateClient;
import app.coronawarn.server.services.distribution.dgc.client.ProdDigitalCovidCertificateClient;
import app.coronawarn.server.services.distribution.dgc.exception.DigitalCovidCertificateException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@EnableConfigurationProperties(value = DistributionServiceConfig.class)
@ExtendWith(SpringExtension.class)
@EnableFeignClients(defaultConfiguration = CloudDccFeignClientConfiguration.class)
@ContextConfiguration(classes = {DistributionServiceConfig.class, ProdDigitalCovidCertificateClient.class},
    initializers = ConfigDataApplicationContextInitializer.class)
//@ActiveProfiles("fake-dcc-client")
class DigitalCovidCertificateClientTest {

  @Autowired
  private DigitalCovidCertificateClient digitalCovidCertificateClient;

  @Test
  public void testCountryList() throws DigitalCovidCertificateException {
    System.out.println(digitalCovidCertificateClient.getCountryList());
  }

  @Test
  public void testValueSet() {
    System.out.println(digitalCovidCertificateClient.getValueSet("test"));
  }

  @Test
  public void testValueSets() {
    digitalCovidCertificateClient.getValueSets().forEach(valueSetV2 -> System.out.println(valueSetV2.getId()));
  }

  @Test
  public void testCountryRule() throws DigitalCovidCertificateException {
    System.out.println(digitalCovidCertificateClient.getCountryRuleByHash("test", "test"));
  }
}
