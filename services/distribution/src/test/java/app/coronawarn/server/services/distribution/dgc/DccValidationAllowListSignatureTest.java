package app.coronawarn.server.services.distribution.dgc;

import static app.coronawarn.server.common.shared.util.SecurityUtils.ecdsaSignatureVerification;
import static app.coronawarn.server.common.shared.util.SecurityUtils.getPublicKeyFromString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import app.coronawarn.server.common.protocols.internal.dgc.ValidationServiceAllowlist;
import app.coronawarn.server.common.shared.util.SecurityUtils;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.AllowList;
import app.coronawarn.server.services.distribution.dgc.client.JsonValidationService;
import app.coronawarn.server.services.distribution.dgc.dsc.DigitalCovidValidationCertificateToProtobufMapping;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.Optional;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@EnableConfigurationProperties(value = DistributionServiceConfig.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    DistributionServiceConfig.class,
    DigitalCovidValidationCertificateToProtobufMapping.class,
    JsonValidationService.class
},
    initializers = ConfigDataApplicationContextInitializer.class)
class DccValidationAllowListSignatureTest {

  @Autowired
  private DistributionServiceConfig distributionServiceConfig;

  @Autowired
  private DigitalCovidValidationCertificateToProtobufMapping digitalCovidValidationCertificateToProtobufMapping;

  @Test
  void testLoadValuesForValidationServiceAllowList() throws NoSuchAlgorithmException, InvalidKeySpecException {
    byte[] signature = distributionServiceConfig.getDigitalGreenCertificate().getAllowListSignature();
    AllowList content = distributionServiceConfig.getDigitalGreenCertificate().getAllowList();
    PublicKey publicKey = getPublicKeyFromString(
        distributionServiceConfig.getDigitalGreenCertificate().getAllowListCertificate());

    assertThat(signature).isNotEmpty();
    assertThat(content).isNotNull();
    assertThat(publicKey).isNotNull();
  }

  @Test
  void testVerifySignature() throws NoSuchAlgorithmException, InvalidKeySpecException {
    String content = distributionServiceConfig.getDigitalGreenCertificate().getAllowListAsString();
    byte[] signature = distributionServiceConfig.getDigitalGreenCertificate().getAllowListSignature();
    PublicKey publicKey = getPublicKeyFromString(
        distributionServiceConfig.getDigitalGreenCertificate().getAllowListCertificate());

    //noinspection CatchMayIgnoreException
    try {
      ecdsaSignatureVerification(
          signature,
          publicKey,
          content.getBytes(StandardCharsets.UTF_8));
    } catch (Throwable t) {
      fail(t.getMessage());
    }
  }

  @Test
  void testValidateSchema() {
    String allowListJson = distributionServiceConfig.getDigitalGreenCertificate().getAllowListAsString();
    assertThat(digitalCovidValidationCertificateToProtobufMapping.validateSchema(allowListJson))
        .isTrue();
    Optional<ValidationServiceAllowlist> optionalProtobuf =
        digitalCovidValidationCertificateToProtobufMapping.constructProtobufMapping();
    assertThat(optionalProtobuf).isPresent();
    assertThat(optionalProtobuf.get().getServiceProvidersList()).isEmpty();
  }

  @Test
  void testValidateSchemaInexistent() {
    try {
      digitalCovidValidationCertificateToProtobufMapping.validateSchema(null);
    } catch (Exception e) {
      assertThat(e.getMessage()).startsWith("A JSONObject text must begin with");
    }
  }

  @Test
  void testValidateSchemaInvalid() {
    AllowList allowList = distributionServiceConfig.getDigitalGreenCertificate().getAllowList();
    allowList.getCertificates()
        .forEach(certificateAllowList -> certificateAllowList.setFingerprint256("notAcceptedChar$"));
    JSONObject jsonObject = new JSONObject(allowList);
    String stringifiedModifiedAllowList = jsonObject.toString();
    assertThat(digitalCovidValidationCertificateToProtobufMapping.validateSchema(stringifiedModifiedAllowList))
        .isFalse();
  }

  @Test
  void testConstructProtobufMapping() {
    Optional<ValidationServiceAllowlist> validationServiceAllowlist =
        digitalCovidValidationCertificateToProtobufMapping.constructProtobufMapping();
    assertThat(validationServiceAllowlist).isPresent();
  }

  @Test
  void testConstructProtobufMappingEmpty() {
    try (MockedStatic<SecurityUtils> utilities = Mockito.mockStatic(SecurityUtils.class)) {
      utilities.when(() -> getPublicKeyFromString(any())).thenThrow(new NoSuchAlgorithmException());
      Optional<ValidationServiceAllowlist> validationServiceAllowlist =
          digitalCovidValidationCertificateToProtobufMapping.constructProtobufMapping();
      assertThat(validationServiceAllowlist).isEmpty();
    }
  }

  @Mock
  private HttpClientBuilder httpClientBuilder;
  @Mock
  private CloseableHttpClient httpClient;

  @Test
  void testWithMockedHttpClientReturningNull() throws IOException {
    try (MockedStatic<HttpClients> httpClientsMockedStatic = Mockito.mockStatic(HttpClients.class)) {
      httpClientsMockedStatic.when(HttpClients::custom).thenReturn(httpClientBuilder);
      when(httpClientBuilder.setSSLHostnameVerifier(any())).thenReturn(httpClientBuilder);
      when(httpClientBuilder.build()).thenReturn(httpClient);
      when(httpClient.execute(any())).thenReturn(null);
      assertThat(digitalCovidValidationCertificateToProtobufMapping.constructProtobufMapping()).isPresent();
    }
  }

  @Test
  void testWithMockedHttpClientReturningResponse() throws IOException {
    try (MockedStatic<HttpClients> httpClientsMockedStatic = Mockito.mockStatic(HttpClients.class)) {
      httpClientsMockedStatic.when(HttpClients::custom).thenReturn(httpClientBuilder);
      when(httpClientBuilder.setSSLHostnameVerifier(any())).thenReturn(httpClientBuilder);
      when(httpClientBuilder.build()).thenReturn(httpClient);
      CloseableHttpResponse closeableHttpResponse = mock(CloseableHttpResponse.class);
      when(httpClient.execute(any())).thenReturn(closeableHttpResponse);
      when(closeableHttpResponse.getEntity()).thenReturn(new StringEntity("{}"));
      assertThat(digitalCovidValidationCertificateToProtobufMapping.constructProtobufMapping()).isPresent();
    }
  }

  @Mock
  private SSLSession sslSession;

  @Test
  void testValidateHostnameWithNoCertificates()
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, SSLPeerUnverifiedException {
    when(sslSession.getPeerCertificates()).thenReturn(new Certificate[0]);
    Method method = DigitalCovidValidationCertificateToProtobufMapping.class
        .getDeclaredMethod("validateHostname", SSLSession.class, String.class);
    method.setAccessible(true);
    Boolean returnValue = (Boolean) method.invoke(digitalCovidValidationCertificateToProtobufMapping, sslSession, null);
    assertThat(returnValue).isFalse();
  }

  @Test
  void testValidateHostnameWithCertificateEqualToFingerPrint()
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, SSLPeerUnverifiedException {
    Certificate certificate = new Certificate("type") {
      @Override
      public byte[] getEncoded() {
        return new byte[0];
      }

      @Override
      public void verify(PublicKey key) {
      }

      @Override
      public void verify(PublicKey key, String sigProvider) {
      }

      @Override
      public String toString() {
        return "fingerPrint";
      }

      @Override
      public PublicKey getPublicKey() {
        return null;
      }
    };
    Certificate[] certificates = new Certificate[]{certificate};
    when(sslSession.getPeerCertificates()).thenReturn(certificates);
    Method method = DigitalCovidValidationCertificateToProtobufMapping.class
        .getDeclaredMethod("validateHostname", SSLSession.class, String.class);
    method.setAccessible(true);
    Boolean returnValue = (Boolean) method.invoke(
        digitalCovidValidationCertificateToProtobufMapping,
        sslSession,
        "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
    assertThat(returnValue).isTrue();
  }
}
