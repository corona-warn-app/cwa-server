package app.coronawarn.server.common.federation.client;

import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

class HostnameVerifierProviderTest {

  @ActiveProfiles("!disable-ssl-efgs-verification")
  @Nested
  class DefaultHostnameVerifierProviderTest {

    @Test
    void testDefaultHostnameVerifierProviderInstance() {
      DefaultHostnameVerifierProvider defaultHostnameVerifierProvider = new DefaultHostnameVerifierProvider();

      assertThat(defaultHostnameVerifierProvider.createHostnameVerifier()).isInstanceOf(DefaultHostnameVerifier.class);
    }
  }

  @ActiveProfiles("disable-ssl-efgs-verification")
  @Nested
  class NoopHostnameVerifierProviderTest {

    @Test
    void testDefaultHostnameVerifierProviderInstance() {
      NoopHostnameVerifierProvider noopHostnameVerifierProvider = new NoopHostnameVerifierProvider();

      assertThat(noopHostnameVerifierProvider.createHostnameVerifier()).isInstanceOf(NoopHostnameVerifier.class);
    }
  }
}
