package app.coronawarn.server.services.federation.download;

import app.coronawarn.server.common.federation.client.FederationFeignClientProvider;
import app.coronawarn.server.services.federation.download.config.FederationDownloadServiceConfig;
import app.coronawarn.server.services.federation.download.config.FederationDownloadServiceConfig.Ssl;
import feign.Client;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients
public class WebConfig {

  @Bean
  Client federationFeignClient(FederationDownloadServiceConfig config) {
    Ssl sslConfig = config.getFederationGateway().getSsl();
    return new FederationFeignClientProvider().createFeignClient(sslConfig.getKeyStorePath(),
        sslConfig.getKeyStorePass(), sslConfig.getCertificateType());
  }

}
