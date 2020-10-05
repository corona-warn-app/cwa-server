

package app.coronawarn.server.services.submission.verification;

import javax.net.ssl.HostnameVerifier;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!disable-ssl-client-verification-verify-hostname")
public class DefaultHostnameVerifierProvider implements HostnameVerifierProvider {

  @Override
  public HostnameVerifier createHostnameVerifier() {
    return new DefaultHostnameVerifier();
  }
}
