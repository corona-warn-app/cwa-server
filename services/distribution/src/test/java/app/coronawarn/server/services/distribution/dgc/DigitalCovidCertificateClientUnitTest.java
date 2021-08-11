package app.coronawarn.server.services.distribution.dgc;

import static app.coronawarn.server.services.distribution.dgc.client.TestDigitalCovidCertificateClient.DISEASE_AGENT_TARGETED_HASH;
import static app.coronawarn.server.services.distribution.dgc.client.TestDigitalCovidCertificateClient.RULE_1_HASH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import app.coronawarn.server.common.shared.util.SerializationUtils;
import app.coronawarn.server.services.distribution.dgc.client.DigitalCovidCertificateFeignClient;
import app.coronawarn.server.services.distribution.dgc.client.ProdDigitalCovidCertificateClient;
import app.coronawarn.server.services.distribution.dgc.client.TestDigitalCovidCertificateClient;
import app.coronawarn.server.services.distribution.dgc.exception.FetchBusinessRulesException;
import app.coronawarn.server.services.distribution.dgc.exception.FetchValueSetsException;
import feign.FeignException.FeignClientException;
import feign.RetryableException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ResourceLoader;

@ExtendWith(MockitoExtension.class)
class DigitalCovidCertificateClientUnitTest {

  public static final String DE = "DE";

  TestDigitalCovidCertificateClient testDigitalCovidCertificateClient;

  ProdDigitalCovidCertificateClient prodDigitalCovidCertificateClient;

  @Mock
  DigitalCovidCertificateFeignClient digitalCovidCertificateFeignClient;

  @Mock
  ResourceLoader resourceLoader;

  @BeforeEach
  void setup() {
    testDigitalCovidCertificateClient = new TestDigitalCovidCertificateClient(resourceLoader);
    prodDigitalCovidCertificateClient = new ProdDigitalCovidCertificateClient(digitalCovidCertificateFeignClient);
  }

  @Test
  void shouldThrowDccExceptionWhenJsonNotFound() {
    try (MockedStatic<SerializationUtils> utilities = Mockito.mockStatic(SerializationUtils.class)) {
      utilities.when(() -> SerializationUtils.readConfiguredJsonOrDefault(any(),any(),any(),any()))
          .thenReturn(Optional.empty());

      assertThat(testDigitalCovidCertificateClient.getCountryList()).isEmpty();
      assertThat(testDigitalCovidCertificateClient.getRules()).isEmpty();
      assertThrows(FetchBusinessRulesException.class,
          () -> testDigitalCovidCertificateClient.getCountryRuleByHash(DE, RULE_1_HASH));
      assertThrows(FetchValueSetsException.class,
          () -> testDigitalCovidCertificateClient.getValueSet(DISEASE_AGENT_TARGETED_HASH));
    }
  }

  @Test
  void shouldThrowFetchExceptionWhenClientThrowsConnectionException() {
    when(digitalCovidCertificateFeignClient.getCountryRule(eq("test"), eq("test")))
        .thenThrow(RetryableException.class);
    assertThrows(FetchBusinessRulesException.class,
        () -> prodDigitalCovidCertificateClient.getCountryRuleByHash(any(), any()));
  }

  @Test
  void shouldThrowExceptionWhenGetCountryListFails() {
    when(digitalCovidCertificateFeignClient.getCountryList())
        .thenThrow(FeignClientException.class);
    assertThrows(FetchBusinessRulesException.class,
        () -> prodDigitalCovidCertificateClient.getCountryList());
  }

  @Test
  void shouldThrowExceptionWhenGetValuesetsFails() {
    when(digitalCovidCertificateFeignClient.getValueSets())
        .thenThrow(FeignClientException.class);
    assertThrows(FetchValueSetsException.class,
        () -> prodDigitalCovidCertificateClient.getValueSets());
  }

  @Test
  void shouldThrowExceptionWhenGetValueSetFails() {
    when(digitalCovidCertificateFeignClient.getValueSet(any()))
        .thenThrow(FeignClientException.class);
    assertThatThrownBy(() -> prodDigitalCovidCertificateClient.getValueSet(any()))
        .isExactlyInstanceOf(FetchValueSetsException.class).hasCauseExactlyInstanceOf(FeignClientException.class);
  }

  @Test
  void shouldThrowExceptionWhenGetRulesFails() {
    when(digitalCovidCertificateFeignClient.getRules())
        .thenThrow(FeignClientException.class);
    assertThrows(FetchBusinessRulesException.class,
        () -> prodDigitalCovidCertificateClient.getRules());
  }
}
