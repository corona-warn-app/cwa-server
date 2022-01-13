package app.coronawarn.server.common.federation.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import app.coronawarn.server.common.federation.client.config.FederationGatewayConfig;
import app.coronawarn.server.common.federation.client.config.FederationGatewayConfig.Ssl;
import app.coronawarn.server.common.federation.client.hostname.DefaultHostnameVerifierProvider;
import java.io.File;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(properties = "spring.main.lazy-initialization=true", webEnvironment = WebEnvironment.RANDOM_PORT)
class CloudFederationFeignHttpClientProviderSmokeTest {

  @Test
  void testCanLoadKeystore() {
    Ssl ssl = new Ssl();
    ssl.setKeyStore(new File("../../docker-compose-test-secrets/ssl.p12"));
    ssl.setKeyStorePassword("123456");
    ssl.setCertificateType("PKCS12");
    ssl.setTrustStore(new File("../../docker-compose-test-secrets/contains_efgs_truststore.jks"));
    ssl.setTrustStorePassword("123456");
    FederationGatewayConfig config = new FederationGatewayConfig();
    config.setConnectionPoolSize(1);
    config.setSsl(ssl);
    CloudFederationFeignHttpClientProvider cut = new CloudFederationFeignHttpClientProvider(config,
        new DefaultHostnameVerifierProvider());
    assertThat(cut.createFeignClient()).isNotNull();
  }

  @Test
  void testThrowException() {
    Ssl ssl = new Ssl();
    ssl.setKeyStore(new File("Incorrect"));
    ssl.setKeyStorePassword("Incorrect");
    ssl.setCertificateType("Incorrect");
    ssl.setTrustStore(new File("Incorrect"));
    ssl.setTrustStorePassword("Incorrect");
    FederationGatewayConfig config = new FederationGatewayConfig();
    config.setConnectionPoolSize(1);
    config.setSsl(ssl);

    CloudFederationFeignHttpClientProvider provider = new CloudFederationFeignHttpClientProvider(config,
        new DefaultHostnameVerifierProvider());
    assertThatExceptionOfType(CloudFeignHttpClientProviderException.class)
        .isThrownBy(() -> provider.createFeignClient());
  }

}
