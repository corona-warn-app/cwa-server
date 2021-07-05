package app.coronawarn.server.services.distribution.dgc;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.dgc.client.DigitalCovidCertificateClient;
import app.coronawarn.server.services.distribution.dgc.client.ProdDigitalCovidCertificateClient;
import app.coronawarn.server.services.distribution.dgc.client.TestDigitalCovidCertificateClient;
import app.coronawarn.server.services.distribution.dgc.exception.DigitalCovidCertificateException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static app.coronawarn.server.services.distribution.dgc.client.TestDigitalCovidCertificateClient.*;

@EnableConfigurationProperties(value = DistributionServiceConfig.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DistributionServiceConfig.class, TestDigitalCovidCertificateClient.class},
    initializers = ConfigDataApplicationContextInitializer.class)
@ActiveProfiles("fake-dcc-client")
class DigitalCovidCertificateClientTest {

  public static final String DE_HASH = "6821d518570fe9f4417c482ff0d2582a7b6440f243a9034f812e0d71611b611f";
  public static final String DE = "DE";

  @Autowired
  private DigitalCovidCertificateClient digitalCovidCertificateClient;

  @Test
  public void testCountryList() throws DigitalCovidCertificateException {
    System.out.println(digitalCovidCertificateClient.getCountryList());
  }

  @Test
  public void testValueSet() {
    System.out.println(digitalCovidCertificateClient.getValueSet(TEST_TYPE_HASH));
  }

  @Test
  public void testValueSets() {
    digitalCovidCertificateClient.getValueSets().forEach(valueSetV2 -> System.out.println(valueSetV2.getId()));
  }

  @Test
  public void testCountryRule() throws DigitalCovidCertificateException {
    System.out.println(digitalCovidCertificateClient.getCountryRuleByHash(DE, DE_HASH));
  }
}
