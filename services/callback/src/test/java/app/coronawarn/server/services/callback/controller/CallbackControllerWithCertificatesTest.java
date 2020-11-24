
package app.coronawarn.server.services.callback.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;

import java.io.FileInputStream;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import app.coronawarn.server.common.persistence.domain.FederationBatchInfo;
import app.coronawarn.server.common.persistence.domain.FederationBatchStatus;
import app.coronawarn.server.common.persistence.service.FederationBatchInfoService;
import app.coronawarn.server.services.callback.ServerApplication;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, //
    classes = { ServerApplication.class })
@DirtiesContext
class CallbackControllerWithCertificatesTest {

  @LocalServerPort
  int randomServerPort;

  @SpyBean
  FederationBatchInfoService spyFederationClient;

  @Test
  void shouldInsertBatchInfoWithCertificate() throws Exception {
    String batchTag = UUID.randomUUID().toString().substring(0, 11);
    LocalDate date = LocalDate.now();

    String url = "https://localhost:" + randomServerPort + "/version/v1/callback?batchTag=" + batchTag + "&date="
        + date;
    HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
    connection.setRequestMethod("GET");
    KeyStore ks = KeyStore.getInstance("PKCS12");
    ks.load(new FileInputStream("src/test/resources/efgs.p12"), "123456".toCharArray());
    KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
    kmf.init(ks, "123456".toCharArray());
    SSLContext sc = SSLContext.getInstance("TLS");
    sc.init(kmf.getKeyManagers(), trustAllCerts, null);
    connection.setSSLSocketFactory(sc.getSocketFactory());
    int status = connection.getResponseCode();
    connection.disconnect();

    assertThat(status).isEqualTo(OK.value());
    assertThat(spyFederationClient.findByStatus(FederationBatchStatus.UNPROCESSED))
        .contains(new FederationBatchInfo(batchTag, date));
  }

  private static final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
    @Override
    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
      return new X509Certificate[0];
    }

    @Override
    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
    }

    @Override
    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
    }
  } };
}
