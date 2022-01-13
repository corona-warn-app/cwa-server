package app.coronawarn.server.services.distribution.dgc.dsc;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.Client.Ssl;
import app.coronawarn.server.services.distribution.dgc.dsc.errors.CloudDscFeignHttpClientProviderException;
import java.io.File;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@EnableConfigurationProperties(value = DistributionServiceConfig.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CloudDscFeignHttpClientProvider.class},
    initializers = ConfigDataApplicationContextInitializer.class)
public class CloudDscFeignHttpClientProviderSmokeTest {

  @Autowired
  DistributionServiceConfig distributionServiceConfig;

  @Test
  void testThrowException() {
    Ssl ssl = new Ssl();
    ssl.setTrustStore(new File("Incorrect"));
    ssl.setTrustStorePassword("Incorrect");
    distributionServiceConfig.setConnectionPoolSize(1);
    distributionServiceConfig.getDigitalGreenCertificate().getDscClient().setSsl(ssl);

    assertThatExceptionOfType(CloudDscFeignHttpClientProviderException.class)
        .isThrownBy(() -> new CloudDscFeignHttpClientProvider(distributionServiceConfig).createDscFeignClient());
  }

}

