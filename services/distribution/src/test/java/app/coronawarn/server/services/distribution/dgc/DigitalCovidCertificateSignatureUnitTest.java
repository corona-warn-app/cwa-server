package app.coronawarn.server.services.distribution.dgc;

import static app.coronawarn.server.common.shared.util.SerializationUtils.readConfiguredJsonOrDefault;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.Client;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.DigitalGreenCertificate;
import app.coronawarn.server.services.distribution.dgc.client.DigitalCovidCertificateFeignClient;
import app.coronawarn.server.services.distribution.dgc.client.ProdDigitalCovidCertificateClient;
import app.coronawarn.server.services.distribution.dgc.client.signature.DccSignatureValidator;
import app.coronawarn.server.services.distribution.dgc.exception.FetchBusinessRulesException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import app.coronawarn.server.services.distribution.dgc.exception.FetchValueSetsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.testcontainers.shaded.okhttp3.Response;

@ExtendWith(MockitoExtension.class)
class DigitalCovidCertificateSignatureUnitTest {

  public static final String PUBLIC_KEY = "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAELewBqw6LfbtjOQIE9WfX0o75TVgh8Xh9/eLYtrerzErmXuk3D3kOvPgimjZ8V/0MGrqJDkpXbtIKRqOq47yHIg==";

  ProdDigitalCovidCertificateClient prodDigitalCovidCertificateClient;

  @Mock
  DigitalCovidCertificateFeignClient digitalCovidCertificateFeignClient;

  @Mock
  ResourceLoader resourceLoader;

  @Mock
  DistributionServiceConfig distributionServiceConfig;

  DccSignatureValidator dccSignatureValidator;


  @BeforeEach
  void setup() {
    when(distributionServiceConfig.getDigitalGreenCertificate()).thenReturn(mockDccConfigurations());
    this.dccSignatureValidator = new DccSignatureValidator(distributionServiceConfig);
    this.prodDigitalCovidCertificateClient = new ProdDigitalCovidCertificateClient(
        digitalCovidCertificateFeignClient, dccSignatureValidator);
  }

  @Test
  void should_verify_country_list_signature() throws IOException, FetchBusinessRulesException {
    String path = "dgc/signature-verification/country-list.json";
    Resource resource = new ClassPathResource(path);
    String signature = getSignature("dgc/signature-verification/country-list-signature");
    when(resourceLoader.getResource(path)).thenReturn(resource);

    Optional<String[]> countries = readConfiguredJsonOrDefault(resourceLoader, path, null, String[].class);
    assertThat(countries).isPresent();

    ResponseEntity<List<String>> responseEntity = mockResponseEntity(Arrays.asList(countries.get()), signature);
    when(digitalCovidCertificateFeignClient.getCountryList()).thenReturn(responseEntity);

    List<String> countryList = prodDigitalCovidCertificateClient.getCountryList();
    assertThat(countryList).isNotEmpty();
  }

  @Test
  void should_verify_get_rules_signature() throws IOException, FetchBusinessRulesException {
    String path = "dgc/signature-verification/get-rules.json";
    Resource resource = new ClassPathResource(path);
    String signature = getSignature("dgc/signature-verification/get-rules-signature");
    when(resourceLoader.getResource(path)).thenReturn(resource);

    Optional<BusinessRuleItem[]> optionalBusinessRuleItems = readConfiguredJsonOrDefault(
        resourceLoader, path, null, BusinessRuleItem[].class);
    assertThat(optionalBusinessRuleItems).isPresent();

    ResponseEntity<List<BusinessRuleItem>> responseEntity = mockResponseEntity(
        Arrays.asList(optionalBusinessRuleItems.get()), signature);
    when(digitalCovidCertificateFeignClient.getRules()).thenReturn(responseEntity);

    List<BusinessRuleItem> countryList = prodDigitalCovidCertificateClient.getRules();
    assertThat(countryList).isNotEmpty();
  }

  @Test
  void should_verify_value_sets_signature() throws IOException, FetchValueSetsException {
    String path = "dgc/signature-verification/get-value-sets.json";
    Resource resource = new ClassPathResource(path);
    String signature = getSignature("dgc/signature-verification/get-value-sets-signature");
    when(resourceLoader.getResource(path)).thenReturn(resource);

    Optional<ValueSetMetadata[]> optionalValueSetMetadata = readConfiguredJsonOrDefault(
        resourceLoader, path, null, ValueSetMetadata[].class);
    assertThat(optionalValueSetMetadata).isPresent();

    ResponseEntity<List<ValueSetMetadata>> responseEntity = mockResponseEntity(
        Arrays.asList(optionalValueSetMetadata.get()), signature);
    when(digitalCovidCertificateFeignClient.getValueSets()).thenReturn(responseEntity);

    List<ValueSetMetadata> countryList = prodDigitalCovidCertificateClient.getValueSets();
    assertThat(countryList).isNotEmpty();
  }

  private String getSignature(String path) throws IOException {
    InputStream in = getResourceStream(path);
    return new String(Objects.requireNonNull(in).readAllBytes(), StandardCharsets.UTF_8);
  }

  private InputStream getResourceStream(String path) {
    return this.getClass().getClassLoader().getResourceAsStream(path);
  }

  private <T> ResponseEntity<T> mockResponseEntity(T body, String signature) {
    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.add("X-SIGNATURE", signature);

    return ResponseEntity.ok()
        .headers(responseHeaders)
        .body(body);
  }

  private DigitalGreenCertificate mockDccConfigurations() {
    DigitalGreenCertificate digitalGreenCertificate = new DigitalGreenCertificate();
    Client client = new Client();
    client.setPublicKey(PUBLIC_KEY);
    digitalGreenCertificate.setClient(client);

    return digitalGreenCertificate;
  }



}
