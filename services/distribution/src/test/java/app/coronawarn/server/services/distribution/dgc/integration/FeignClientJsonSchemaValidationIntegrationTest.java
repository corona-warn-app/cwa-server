package app.coronawarn.server.services.distribution.dgc.integration;

import static app.coronawarn.server.common.shared.util.SerializationUtils.readConfiguredJsonOrDefault;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.fail;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.dgc.ApacheHttpTestConfiguration;
import app.coronawarn.server.services.distribution.dgc.BusinessRule;
import app.coronawarn.server.services.distribution.dgc.client.CloudDccFeignClientConfiguration;
import app.coronawarn.server.services.distribution.dgc.client.CloudDccFeignHttpClientProvider;
import app.coronawarn.server.services.distribution.dgc.client.DigitalCovidCertificateClient;
import app.coronawarn.server.services.distribution.dgc.client.ProdDigitalCovidCertificateClient;
import app.coronawarn.server.services.distribution.dgc.client.signature.DccSignatureValidator;
import app.coronawarn.server.services.distribution.dgc.exception.FetchBusinessRulesException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import java.util.Optional;
import org.everit.json.schema.ValidationException;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
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
public class FeignClientJsonSchemaValidationIntegrationTest {

  public static final String X_SIGNATURE = "X-SIGNATURE";
  //TODO: create a valid rule signature, with connection to the json in the payload
  public static final String RULES_SIGNATURE
      = "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEMMvW0zun8fNCELK1tsqXsGJPu4p7850ZPCBCoxQ5gs2z5G0in3izL7eTFa5lI7Gkhnz0tN5whVQJObCaqbP55A==";
  private static final WireMockServer wireMockServer = new WireMockServer(options().port(1234));
  private static final String VALID_RULE_JSON_FILE = "dgc/ccl-configuration-sample.json";
  private static final String INVALILD_RULE_JSON_FILE = "dgc/ccl-configuration-sample_missing_required_property.json";
  @Autowired
  DigitalCovidCertificateClient digitalCovidCertificateClient;
  @Autowired
  ResourceLoader resourceLoader;
  @Autowired
  private DistributionServiceConfig distributionServiceConfig;

  @BeforeAll
  public static void setup() {
    wireMockServer.start();
  }

  public static String asJsonString(final Object obj) {
    try {
      return new ObjectMapper().writeValueAsString(obj);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  void shouldPassValidation() {
    stubRules(VALID_RULE_JSON_FILE, "/rules/DE/abcabc");
    try {
      //make sure our json decoder is actually called
      //This is quite hard to do, since the client that wraps this is built via Spring
      //So we can't easily mock it and check for method invocations
      //all we can do is to provoke an exception that only the json decoder will throw
      digitalCovidCertificateClient.getCountryRuleByHash("DE", "abcabc");
    } catch (FetchBusinessRulesException e) {
      fail("Failed to validate rules", e);
    }
  }

  @Test
  void provokeValidationErrorInInterceptor() {
    stubRules(INVALILD_RULE_JSON_FILE, "/rules/DE/abcabc");
    try {
      digitalCovidCertificateClient.getCountryRuleByHash("DE", "abcabc");
    } catch (FetchBusinessRulesException e) {
      // the decode exception
      Throwable cause = e.getCause();
      // the actual validation exception
      Throwable cause1 = cause.getCause();
      Assert.assertEquals(ValidationException.class, cause1.getClass());
    }
  }

  public void stubRules(String jsonFilePath, String path) {
    Optional<BusinessRule> businessRuleOptional =
        readConfiguredJsonOrDefault(resourceLoader, jsonFilePath, jsonFilePath, BusinessRule.class);
    BusinessRule businessRule = businessRuleOptional.get();

    wireMockServer.stubFor(
        get(urlPathEqualTo(path))
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
                    .withHeader(X_SIGNATURE, "abc")
                    //.withHeader(X_SIGNATURE, createSignature(asJsonString(businessRuleItemList)))
                    .withBody(asJsonString(businessRule))));
  }

  private String createSignature(String body) {
    String publicKey = distributionServiceConfig.getDigitalGreenCertificate().getClient().getPublicKey();

    return null;
  }
}
