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
class CloudFederationFeignHttpClientProviderSmokeTest {

  @Test
  void testCanLoadKeystore() {
    Ssl ssl = new Ssl();
    HostnameVerifierProvider hostnameVerifierProvider = new DefaultHostnameVerifierProvider();
    ssl.setKeyStore(new File("../../docker-compose-test-secrets/ssl.p12"));
    ssl.setKeyStorePass("");
    ssl.setTrustStore(new File("../../docker-compose-test-secrets/ssl.p12"));
    ssl.setTrustStorePassword("");
    FederationGatewayConfig config = new FederationGatewayConfig();
    config.setConnectionPoolSize(1);
    config.setSsl(ssl);
    CloudFederationFeignHttpClientProvider cut = new CloudFederationFeignHttpClientProvider(config, hostnameVerifierProvider);
    assertThat(cut.createFeignClient()).isNotNull();
  }

}
