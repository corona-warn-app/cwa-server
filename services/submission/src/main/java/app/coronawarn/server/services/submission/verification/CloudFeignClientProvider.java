package app.coronawarn.server.services.submission.verification;

import static java.util.concurrent.TimeUnit.SECONDS;

import app.coronawarn.server.common.federation.client.AbstractFeignClientProvider;
import app.coronawarn.server.common.federation.client.hostname.HostnameVerifierProvider;
import app.coronawarn.server.services.submission.config.SubmissionServiceConfig;
import app.coronawarn.server.services.submission.config.SubmissionServiceConfig.FeignRetry;
import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class CloudFeignClientProvider extends AbstractFeignClientProvider {

  private final FeignRetry retry;

  /**
   * Creates a {@link CloudFeignClientProvider} that provides feign clients with fixed key and trust material.
   *
   * @param config                   config attributes of {@link SubmissionServiceConfig}
   * @param hostnameVerifierProvider provider {@link SubmissionServiceConfig}
   */
  public CloudFeignClientProvider(final SubmissionServiceConfig config,
      final HostnameVerifierProvider hostnameVerifierProvider) {

    super(config.getConnectionPoolSize(), config.getClient().getSsl().getTrustStore(),
        config.getClient().getSsl().getTrustStorePassword(), config.getClient().getSsl().getKeyStore(),
        config.getClient().getSsl().getKeyStorePassword(), config.getClient().getSsl().getKeyPassword(),
        hostnameVerifierProvider);

    retry = config.getFeignRetry();
  }

  /**
   * Creates new {@link Retryer} with {@link #retry} values.
   * 
   * @return {@link Retryer} with {@link #retry} values.
   */
  @Bean
  public Retryer retryer() {
    return new Retryer.Default(retry.getPeriod(), SECONDS.toMillis(retry.getMaxPeriod()), retry.getMaxAttempts());
  }
}
