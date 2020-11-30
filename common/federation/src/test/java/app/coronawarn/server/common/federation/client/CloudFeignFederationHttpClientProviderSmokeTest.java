package app.coronawarn.server.common.federation.client;

import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.common.federation.client.config.FederationGatewayConfig;
import app.coronawarn.server.common.federation.client.config.FederationGatewayConfig.Ssl;
import java.io.File;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(properties = "spring.main.lazy-initialization=true", webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext
class CloudFeignFederationHttpClientProviderSmokeTest {

  @Test
  void testCanLoadKeystore() {
    Ssl ssl = new Ssl();
    ssl.setKeyStore(new File("../../docker-compose-test-secrets/ssl.p12"));
    ssl.setKeyStorePass("");
    FederationGatewayConfig config = new FederationGatewayConfig();
    config.setConnectionPoolSize(1);
    config.setSsl(ssl);
    CloudFeignFederationHttpClientProvider cut = new CloudFeignFederationHttpClientProvider(config);
    assertThat(cut.createFeignClient()).isNotNull();
  }

}
