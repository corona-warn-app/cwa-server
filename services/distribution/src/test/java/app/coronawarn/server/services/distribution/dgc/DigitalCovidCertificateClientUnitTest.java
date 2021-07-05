package app.coronawarn.server.services.distribution.dgc;

import app.coronawarn.server.common.shared.exception.UnableToLoadFileException;
import app.coronawarn.server.common.shared.util.SerializationUtils;
import app.coronawarn.server.services.distribution.dgc.BusinessRule.RuleType;
import app.coronawarn.server.services.distribution.dgc.client.DigitalCovidCertificateClient;
import app.coronawarn.server.services.distribution.dgc.client.TestDigitalCovidCertificateClient;
import app.coronawarn.server.services.distribution.dgc.exception.DigitalCovidCertificateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import java.util.Collections;
import java.util.Optional;

import static app.coronawarn.server.services.distribution.dgc.DigitalGreenCertificateToCborMapping.DCC_VALIDATION_RULE_JSON_CLASSPATH;
import static app.coronawarn.server.services.distribution.dgc.client.TestDigitalCovidCertificateClient.DISEASE_AGENT_TARGETED_HASH;
import static app.coronawarn.server.services.distribution.dgc.client.TestDigitalCovidCertificateClient.RULE_1_HASH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DigitalCovidCertificateClientUnitTest {

  public static final String DE = "DE";

  TestDigitalCovidCertificateClient testDigitalCovidCertificateClient;

  @Mock
  ResourceLoader resourceLoader;

  @BeforeEach
  void setup() {
    testDigitalCovidCertificateClient = new TestDigitalCovidCertificateClient(resourceLoader);
  }

  @Test
  void shouldThrowDccExceptionWhenJsonNotFound() {
    try (MockedStatic<SerializationUtils> utilities = Mockito.mockStatic(SerializationUtils.class)) {
      utilities.when(() -> SerializationUtils.readConfiguredJsonOrDefault(any(),any(),any(),any()))
          .thenThrow(UnableToLoadFileException.class);

      assertThatExceptionOfType(DigitalCovidCertificateException.class).isThrownBy(
          () -> testDigitalCovidCertificateClient.getCountryList());
      assertThatExceptionOfType(DigitalCovidCertificateException.class).isThrownBy(
          () -> testDigitalCovidCertificateClient.getRules());
      assertThatExceptionOfType(DigitalCovidCertificateException.class).isThrownBy(
          () -> testDigitalCovidCertificateClient.getCountryRuleByHash(DE, RULE_1_HASH));
      assertThatExceptionOfType(DigitalCovidCertificateException.class).isThrownBy(
          () -> testDigitalCovidCertificateClient.getValueSet(DISEASE_AGENT_TARGETED_HASH));
    }
  }

}
