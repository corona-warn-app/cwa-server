package app.coronawarn.server.services.distribution.dgc.integration;

import static app.coronawarn.server.common.shared.util.SerializationUtils.readConfiguredJsonOrDefault;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.dgc.ApacheHttpTestConfiguration;
import app.coronawarn.server.services.distribution.dgc.BusinessRule;
import app.coronawarn.server.services.distribution.dgc.BusinessRule.RuleType;
import app.coronawarn.server.services.distribution.dgc.BusinessRuleItem;
import app.coronawarn.server.services.distribution.dgc.ValueSet;
import app.coronawarn.server.services.distribution.dgc.ValueSetMetadata;
import app.coronawarn.server.services.distribution.dgc.client.CloudDccFeignClientConfiguration;
import app.coronawarn.server.services.distribution.dgc.client.CloudDccFeignHttpClientProvider;
import app.coronawarn.server.services.distribution.dgc.client.DigitalCovidCertificateClient;
import app.coronawarn.server.services.distribution.dgc.client.ProdDigitalCovidCertificateClient;
import app.coronawarn.server.services.distribution.dgc.client.signature.DccSignatureValidator;
import app.coronawarn.server.services.distribution.dgc.exception.FetchBusinessRulesException;
import app.coronawarn.server.services.distribution.dgc.exception.FetchValueSetsException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
public class DigitalCovidCertificateIT {

  public static final String RULE_COUNTRY = "CH";
  public static final String RULE_HASH = "f4e0acac6a7e2b556516d8c3ae51277c4102e7e386a59d26b6ea7b693ff8d54f";
  public static final String VALUESET_HASH = "f4e0acac6a7e2b556516d8c3ae51277c4102e7e386a59d26b6ea7b693ff8d54f";

  public static final String X_SIGNATURE = "X-SIGNATURE";
  public static final String RULES_SIGNATURE = "MEUCIQDqPV3YZJkHv14XU6+ZwZhlX+wg7kMI64shqRnnjmMEXwIgcf9hukkmchoJ1vKFdosb/dH+cPF+hpsNj9stPJDPQQc=";
  public static final String RULE_SIGNATURE = "MEUCIF98aQgbmrEpV9iOwczJJeJD0S+7E99NN7SrDrkQO+rgAiEAglp4rtZXbQSioA1ur+I8ir+Qmx4J1Ltd/20jErIpzqI=";
  public static final String COUNTRIES_SIGNATURE = "MEYCIQCf5qB5VxZGLVem+whVin7PU5kIAguJ4QvdgVb1DifskwIhAObGjRjIiw1hQMYvnzZc1DJQl4flNnudDjBu5DNlonSz";
  public static final String VALUESETS_SIGNATURE = "MEUCIQCPgW4cRVm/jHZJc9usf1Bwy60pHrLwpxfiTod06j13JQIgAuZUi/APjjFimil27M0sS2YVkLXcrrFAVE+arCamIzE=";
  public static final String VALUESET_SIGNATURE = "MEQCIHM3YLHGEuVPYqKo7nTd+y/X19aG9oh5TFP3dX/mXQrpAiB/UDN6Lz3MXeeMkgnxp2vj26OTLRuEGtAyUHIZ/yG9rw==";

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
  public void shouldFetchAllRulesItems() throws FetchBusinessRulesException {
    stubRules();

    List<BusinessRuleItem> rules = digitalCovidCertificateClient.getRules();
    assertThat(rules).isNotEmpty();
  }

  @Test
  public void shouldFetchSpecificRule() throws FetchBusinessRulesException, IOException {
    stubRuleByHash();

    BusinessRule businessRule = digitalCovidCertificateClient.getCountryRuleByHash(RULE_COUNTRY, RULE_HASH);

    assertThat(businessRule.getIdentifier()).isNotEmpty();
    assertThat(businessRule.getCountry()).isEqualTo(RULE_COUNTRY);
    assertTrue(isAcceptanceOrInvalidation(businessRule.getType()));
  }

  @Test
  public void shouldFetchCountryList() throws FetchBusinessRulesException {
    stubCountries();
    List<String> countries = digitalCovidCertificateClient.getCountryList();
    assertThat(countries).isNotEmpty();
  }

  @Test
  public void shouldFetchAllValuesetsMetadata() throws FetchValueSetsException {
    stubValueSets();

    List<ValueSetMetadata> valuesets = digitalCovidCertificateClient.getValueSets();
    assertThat(valuesets).isNotEmpty();
  }

  @Test
  public void shouldFetchSpecificValueset() throws FetchValueSetsException, IOException {
    stubValueSetByHash();
    ValueSet valueSet = digitalCovidCertificateClient.getValueSet(VALUESET_HASH);
    assertThat(valueSet).isNotNull();
    assertThat(valueSet.getValueSetId()).isNotEmpty();
    assertThat(valueSet.getValueSetValues()).isNotEmpty();
  }

  private boolean isAcceptanceOrInvalidation(String type) {
    return type.equalsIgnoreCase(RuleType.Invalidation.name()) || type.equalsIgnoreCase(RuleType.Acceptance.name());
  }

  private void stubValueSetByHash() throws IOException {
    String content = new String(new ClassPathResource("dgc/wiremock/valueset.json").getInputStream().readAllBytes(), StandardCharsets.UTF_8);

    wireMockServer.stubFor(
        get(urlPathMatching("/valuesets/.*"))
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
                    .withHeader(X_SIGNATURE, VALUESET_SIGNATURE)
                    .withBody(content)));
  }

  private void stubValueSets() {
    Optional<ValueSetMetadata[]> valuesets =
        readConfiguredJsonOrDefault(resourceLoader, null, "dgc/wiremock/valuesets.json", ValueSetMetadata[].class);
    List<ValueSetMetadata> valueSetsList = Arrays.asList(valuesets.get());

    wireMockServer.stubFor(
        get(urlPathEqualTo("/valuesets"))
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
                    .withHeader(X_SIGNATURE, VALUESETS_SIGNATURE)
                    .withBody(asJsonString(valueSetsList))));
  }

  private void stubRuleByHash() {
    Optional<BusinessRule> businessRule =
        readConfiguredJsonOrDefault(resourceLoader, null, "dgc/wiremock/rule.json", BusinessRule.class);

    wireMockServer.stubFor(
        get(urlPathEqualTo("/rules/" + RULE_COUNTRY + "/" + RULE_HASH))
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
                    .withHeader(X_SIGNATURE, RULE_SIGNATURE)
                    .withBody(asJsonString(businessRule.get()))));
  }

  private void stubRules() {
    Optional<BusinessRuleItem[]> businessRuleList =
        readConfiguredJsonOrDefault(resourceLoader, null, "dgc/wiremock/rules.json", BusinessRuleItem[].class);
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

  private void stubCountries() {
    Optional<String[]> countriesList =
        readConfiguredJsonOrDefault(resourceLoader, null, "dgc/wiremock/countries.json", String[].class);
    List<String> countries = Arrays.asList(countriesList.get());

    wireMockServer.stubFor(
        get(urlPathEqualTo("/countrylist"))
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
                    .withHeader(X_SIGNATURE, COUNTRIES_SIGNATURE)
                    .withBody(asJsonString(countries))));
  }

  public static String asJsonString(final Object obj) {
    try {
      return new ObjectMapper().writeValueAsString(obj);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
