package app.coronawarn.server.common.federation.client;

import javax.net.ssl.HostnameVerifier;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("disable-ssl-client-verification-verify-hostname")
public class NoopHostnameVerifierProvider implements HostnameVerifierProvider {

  private static final Logger logger = LoggerFactory.getLogger(NoopHostnameVerifierProvider.class);

  public NoopHostnameVerifierProvider() {
    logger.warn("The submission service is started with verification service TLS hostname validation disabled. "
        + "This should never be used in PRODUCTION!");
  }

  @Override
  public HostnameVerifier createHostnameVerifier() {
    return new NoopHostnameVerifier();
  }
}
