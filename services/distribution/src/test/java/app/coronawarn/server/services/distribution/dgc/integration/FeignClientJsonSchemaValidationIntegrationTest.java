package app.coronawarn.server.services.distribution.dgc.integration;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.dgc.ApacheHttpTestConfiguration;
import app.coronawarn.server.services.distribution.dgc.BusinessRule;
import app.coronawarn.server.services.distribution.dgc.ValueSet;
import app.coronawarn.server.services.distribution.dgc.client.CloudDccFeignClientConfiguration;
import app.coronawarn.server.services.distribution.dgc.client.CloudDccFeignHttpClientProvider;
import app.coronawarn.server.services.distribution.dgc.client.DigitalCovidCertificateClient;
import app.coronawarn.server.services.distribution.dgc.client.ProdDigitalCovidCertificateClient;
import app.coronawarn.server.services.distribution.dgc.client.signature.DccSignatureValidator;
import app.coronawarn.server.services.distribution.dgc.exception.FetchBusinessRulesException;
import app.coronawarn.server.services.distribution.dgc.exception.FetchValueSetsException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.everit.json.schema.ValidationException;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.io.IOException;
import java.util.Optional;

import static app.coronawarn.server.common.shared.util.SerializationUtils.readConfiguredJsonOrDefault;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.fail;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DistributionServiceConfig.class, ProdDigitalCovidCertificateClient.class,
    CloudDccFeignClientConfiguration.class, CloudDccFeignHttpClientProvider.class, ApacheHttpTestConfiguration.class,
    DccSignatureValidator.class, SignatureValidationMockConfiguration.class},
    initializers = ConfigDataApplicationContextInitializer.class)
@ImportAutoConfiguration({FeignAutoConfiguration.class, HttpMessageConvertersAutoConfiguration.class})
@ActiveProfiles({"integration-test", "dcc-client-factory"})
public class FeignClientJsonSchemaValidationIntegrationTest {

  public static final String X_SIGNATURE = "X-SIGNATURE";
  private static final WireMockServer wireMockServer = new WireMockServer(options().port(1234));
  private static final String VALID_RULE_JSON_FILE = "dgc/json-validation/ccl-configuration.json";
  private static final String INVALILD_RULE_JSON_FILE = "dgc/json-validation/rule_invalid.json";
  private static final String UNMAPPED_URL_JSON_FILE = "dgc/json-validation/valueset.json";

  @Autowired
  DigitalCovidCertificateClient digitalCovidCertificateClient;

  @Autowired
  ResourceLoader resourceLoader;

  @BeforeAll
  public static void setup() {
    wireMockServer.start();
  }

  @Test
  void shouldPassValidation() throws IOException {

    stubRules(VALID_RULE_JSON_FILE, "/rules/DE/abcabc", BusinessRule.class);
    try {
      digitalCovidCertificateClient.getCountryRuleByHash("DE", "abcabc");
    } catch (FetchBusinessRulesException e) {
      fail("Failed to validate rules", e);
    }
  }

  @Test
  void shouldPassValidationDueToUnmappedURL() {
    stubRules(UNMAPPED_URL_JSON_FILE, "/valuesets/abcabc", ValueSet.class);
    try {
      digitalCovidCertificateClient.getValueSet("abcabc");
    } catch (FetchValueSetsException e) {
      fail("Failed to validate rules", e);
      e.printStackTrace();
    } catch (ValidationException ex) {
      fail("Schema evaluation should not take place for value sets", ex);
    }
  }

  @Test
  void provokeValidationErrorInInterceptor() {
    //make sure our json decoder is actually called
    //This is quite hard to do, since the client that wraps this is built via Spring
    //So we can't easily mock it and check for method invocations
    //all we can do is to provoke an exception that only the json decoder will throw
    stubRules(INVALILD_RULE_JSON_FILE, "/rules/DE/abcabc", BusinessRule.class);
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

  public <T, U extends Class> void stubRules(String jsonFilePath, String path, U returnObject) {
    Optional<T> returnObjectOptional =
        readConfiguredJsonOrDefault(resourceLoader, jsonFilePath, jsonFilePath, returnObject);
    T businessRule = returnObjectOptional.get();

    wireMockServer.stubFor(
        get(urlPathEqualTo(path))
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
                    //signature validation is mocked
                    .withHeader(X_SIGNATURE, "mock-signature")
                    .withBody(asJsonString(businessRule))));
  }

  public static String asJsonString(final Object obj) {
    try {
      return new ObjectMapper().writeValueAsString(obj);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
