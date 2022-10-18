package app.coronawarn.server.services.distribution.dgc.client;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import feign.Client;
import feign.Retryer;
import feign.codec.Decoder;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.support.HttpMessageConverterCustomizer;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ResourceLoader;

@Configuration
@EnableFeignClients
@Profile("!revocation")
public class CloudDccFeignClientConfiguration {

  private static final Logger logger = LoggerFactory.getLogger(CloudDccFeignClientConfiguration.class);

  private final CloudDccFeignHttpClientProvider feignClientProvider;

  private final DistributionServiceConfig distributionServiceConfig;

  /**
   * Create an instance.
   */
  public CloudDccFeignClientConfiguration(CloudDccFeignHttpClientProvider feignClientProvider,
      DistributionServiceConfig distributionServiceConfig) {
    logger.debug("Creating Cloud DCC Feign Client Configuration");
    this.feignClientProvider = feignClientProvider;
    this.distributionServiceConfig = distributionServiceConfig;
  }

  @Bean
  @Profile("!revocation")
  public Client feignClient() {
    return feignClientProvider.createFeignClient();
  }

  /**
   * Retrier configuration for Feign DCC client.
   */
  @Bean
  @Profile("!revocation")
  public Retryer retryer() {
    long retryPeriod = TimeUnit.SECONDS.toMillis(
        distributionServiceConfig.getDigitalGreenCertificate().getClient().getRetryPeriod());

    long maxRetryPeriod = TimeUnit.SECONDS.toMillis(
        distributionServiceConfig.getDigitalGreenCertificate().getClient().getMaxRetryPeriod());

    int maxAttempts = distributionServiceConfig.getDigitalGreenCertificate().getClient().getMaxRetryAttempts();

    return new Retryer.Default(retryPeriod, maxRetryPeriod, maxAttempts);
  }

  /**
   * Feign decoder for validation of json files against a given schema.
   * @param messageConverters Message converters to use.
   * @param customizers Customizers to use.
   * @param resourceLoader A resource loader.
   * @param jsonValidationService A validation service that checks for json schema violations.
   * @return The decoder.
   */
  @Bean
  public Decoder jsonSchemaDecoder(ObjectFactory<HttpMessageConverters> messageConverters,
      ObjectProvider<HttpMessageConverterCustomizer> customizers, ResourceLoader resourceLoader,
      JsonValidationService jsonValidationService) {
    Decoder jsonSchemaDecoder = new JsonSchemaDecoder(messageConverters, customizers, resourceLoader,
        jsonValidationService);
    return new ResponseEntityDecoder(jsonSchemaDecoder);
  }

  /**
   * A service to validate json against a schema.
   * @param resourceLoader A resource loader.
   * @return The validation service.
   */
  @Bean
  public JsonValidationService jsonValidationService(ResourceLoader resourceLoader) {
    return new JsonValidationService(resourceLoader);
  }
}
