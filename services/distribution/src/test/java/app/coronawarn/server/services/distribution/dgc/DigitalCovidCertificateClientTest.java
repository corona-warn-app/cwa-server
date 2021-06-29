package app.coronawarn.server.services.distribution.dgc;

import app.coronawarn.server.services.distribution.dgc.client.DigitalCovidCertificateClient;
import app.coronawarn.server.services.distribution.dgc.client.TestDigitalCovidCertificateClient;
import app.coronawarn.server.services.distribution.dgc.exception.DigitalCovidCertificateException;
import org.junit.jupiter.api.Test;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@EnableConfigurationProperties(value = DistributionServiceConfig.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DistributionServiceConfig.class, TestDigitalCovidCertificateClient.class},
    initializers = ConfigDataApplicationContextInitializer.class)
@ActiveProfiles("fake-dcc-client")
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
