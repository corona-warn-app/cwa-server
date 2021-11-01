package app.coronawarn.server.services.distribution.dgc.integration;

import static app.coronawarn.server.common.shared.util.SerializationUtils.readConfiguredJsonOrDefault;
import static app.coronawarn.server.services.distribution.dgc.integration.DigitalCovidCertificateIT.RULES_SIGNATURE;
import static app.coronawarn.server.services.distribution.dgc.integration.DigitalCovidCertificateIT.X_SIGNATURE;
import static app.coronawarn.server.services.distribution.dgc.integration.DigitalCovidCertificateIT.asJsonString;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

import app.coronawarn.server.common.shared.util.SecurityUtils;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.dgc.ApacheHttpTestConfiguration;
import app.coronawarn.server.services.distribution.dgc.BusinessRuleItem;
import app.coronawarn.server.services.distribution.dgc.client.CloudDccFeignClientConfiguration;
import app.coronawarn.server.services.distribution.dgc.client.CloudDccFeignHttpClientProvider;
import app.coronawarn.server.services.distribution.dgc.client.DigitalCovidCertificateClient;
import app.coronawarn.server.services.distribution.dgc.client.ProdDigitalCovidCertificateClient;
import app.coronawarn.server.services.distribution.dgc.client.signature.DccSignatureValidator;
import app.coronawarn.server.services.distribution.dgc.exception.DigitalCovidCertificateSignatureException;
import app.coronawarn.server.services.distribution.dgc.exception.FetchBusinessRulesException;
import com.github.tomakehurst.wiremock.WireMockServer;
import feign.RetryableException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DistributionServiceConfig.class, ProdDigitalCovidCertificateClient.class,
    CloudDccFeignClientConfiguration.class, CloudDccFeignHttpClientProvider.class, ApacheHttpTestConfiguration.class,
    DccSignatureValidator.class},
    initializers = ConfigDataApplicationContextInitializer.class)
@ImportAutoConfiguration({FeignAutoConfiguration.class, HttpMessageConvertersAutoConfiguration.class})
@ActiveProfiles("dcc-client-factory")
public class DigitalCovidCertificateSignatureTest {

  private static final WireMockServer wireMockServer = new WireMockServer(options().port(1234));

  @Autowired
  DigitalCovidCertificateClient digitalCovidCertificateClient;

  @Autowired
  ResourceLoader resourceLoader;

  @BeforeAll
  public static void setup() {
    wireMockServer.start();
  }

  @Test
  public void shouldThrowRuntimeExceptionWhenPublicKeyIsWrong() {
    stubRules();

    try (MockedStatic<SecurityUtils> utilities = Mockito.mockStatic(SecurityUtils.class)) {
      utilities.when(() -> SecurityUtils.ecdsaSignatureVerification(any(), any(), any()))
          .thenThrow(NoSuchAlgorithmException.class)
          .thenThrow(SignatureException.class);

      Exception runtime = Assert.assertThrows(FetchBusinessRulesException.class,
          () -> digitalCovidCertificateClient.getRules());
      assertThat(runtime.getCause()).isInstanceOf(DigitalCovidCertificateSignatureException.class);

      Exception secondException = Assert.assertThrows(FetchBusinessRulesException.class,
          () -> digitalCovidCertificateClient.getRules());
      assertThat(secondException.getCause()).isInstanceOf(RetryableException.class);
      assertThat(secondException.getCause().getCause()).isInstanceOf(IOException.class);
      assertThat(secondException.getCause().getCause().getCause()).isInstanceOf(SignatureException.class);
    }
  }

  @Test
  public void shouldRetryAndThrowCheckedException() {
    stubRules();

    try (MockedStatic<SecurityUtils> utilities = Mockito.mockStatic(SecurityUtils.class)) {
      utilities.when(() -> SecurityUtils.getPublicKeyFromString(any()))
          .thenThrow(NoSuchAlgorithmException.class)
          .thenThrow(InvalidKeySpecException.class);

      Exception exception = Assert.assertThrows(FetchBusinessRulesException.class,
          () -> digitalCovidCertificateClient.getRules());
      assertThat(exception.getCause()).isInstanceOf(DigitalCovidCertificateSignatureException.class);

      Exception secondException = Assert.assertThrows(FetchBusinessRulesException.class,
          () -> digitalCovidCertificateClient.getRules());
      assertThat(secondException.getCause()).isInstanceOf(DigitalCovidCertificateSignatureException.class);
    }
  }

  public void stubRules() {
    Optional<BusinessRuleItem[]> businessRuleList =
        readConfiguredJsonOrDefault(resourceLoader, null, "dgc/rules.json", BusinessRuleItem[].class);
    List<BusinessRuleItem> businessRuleItemList = Arrays.asList(businessRuleList.get());

    wireMockServer.stubFor(
        get(urlPathEqualTo("/rules"))
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
                    .withHeader(X_SIGNATURE, RULES_SIGNATURE)
                    .withBody(asJsonString(businessRuleItemList))));
  }
}
