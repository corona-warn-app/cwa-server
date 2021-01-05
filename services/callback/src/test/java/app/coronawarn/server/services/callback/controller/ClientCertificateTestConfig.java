package app.coronawarn.server.services.callback.controller;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

public class ClientCertificateTestConfig {

  private static final String CLIENT_CERTIFICATE = "../../docker-compose-test-secrets/efgs.p12";
  private static final String CERTIFICATE_PWD = "123456";
  private static final String TLS = "TLS";
  private static final String SUN_X509 = "SunX509";
  private static final String PKCS12 = "PKCS12";

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

  @Bean
  public RestTemplateBuilder myRestTemplateBuilder() {
    return new RestTemplateBuilder() {
      @Override
      public ClientHttpRequestFactory buildRequestFactory() {
        try {
          KeyStore ks = KeyStore.getInstance(PKCS12);
          ks.load(new FileInputStream(CLIENT_CERTIFICATE), CERTIFICATE_PWD.toCharArray());
          KeyManagerFactory kmf = KeyManagerFactory.getInstance(SUN_X509);
          kmf.init(ks, CERTIFICATE_PWD.toCharArray());
          SSLContext sc = SSLContext.getInstance(TLS);
          sc.init(kmf.getKeyManagers(), trustAllCerts, null);

          ClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory() {
            @Override
            protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
              super.prepareConnection(connection, httpMethod);
              ((HttpsURLConnection) connection).setSSLSocketFactory(sc.getSocketFactory());
            }
          };
          return requestFactory;
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    };
  }
}
