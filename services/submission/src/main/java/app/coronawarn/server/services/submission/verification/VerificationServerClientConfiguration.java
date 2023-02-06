package app.coronawarn.server.services.submission.verification;

import feign.Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VerificationServerClientConfiguration {

  private final CloudFeignClientProvider feignClientProvider;

  public VerificationServerClientConfiguration(CloudFeignClientProvider feignClientProvider) {
    this.feignClientProvider = feignClientProvider;
  }

  @Bean
  public Client feignClient() {
    return feignClientProvider.createFeignClient();
  }
}
