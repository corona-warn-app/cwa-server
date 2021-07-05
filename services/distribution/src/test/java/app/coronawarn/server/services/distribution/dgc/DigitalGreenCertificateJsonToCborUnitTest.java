package app.coronawarn.server.services.distribution.dgc;

import static app.coronawarn.server.services.distribution.dgc.DigitalGreenCertificateToCborMapping.DCC_VALIDATION_RULE_JSON_CLASSPATH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import app.coronawarn.server.services.distribution.dgc.BusinessRule.RuleType;
import app.coronawarn.server.services.distribution.dgc.client.DigitalCovidCertificateClient;
import app.coronawarn.server.services.distribution.dgc.exception.DigitalCovidCertificateException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Optional;
import org.junit.BeforeClass;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

@ExtendWith(MockitoExtension.class)
class DigitalGreenCertificateJsonToCborUnitTest {

  public static final String RANDOM_STRING = "random_string";
  public static final String DE = "DE";
  public static final String BAD_IDENTIFIER = "BAD-IDENTIFIER";

  DigitalGreenCertificateToCborMapping digitalGreenCertificateToCborMapping;

  @Mock
  DigitalCovidCertificateClient digitalCovidCertificateClient;

  @Mock
  ResourceLoader resourceLoader;

  @BeforeEach
  void setup() {
    digitalGreenCertificateToCborMapping = new DigitalGreenCertificateToCborMapping(
        digitalCovidCertificateClient, resourceLoader);
  }

  @Test
  void shouldThrowWhenRuleByHashAndCountryIsNotFetched() throws DigitalCovidCertificateException {
    when(digitalCovidCertificateClient.getRules()).thenReturn(Collections.singletonList(mockBusinessRuleItem()));
    when(digitalCovidCertificateClient.getCountryRuleByHash(any(),any())).thenReturn(Optional.empty());

    assertThatExceptionOfType(DigitalCovidCertificateException.class).isThrownBy(
        () -> digitalGreenCertificateToCborMapping.constructRules(RuleType.Acceptance));
  }

  @Test
  void shouldThrowWhenRuleObjectFailsValidation() throws DigitalCovidCertificateException {
    Resource validationSchema = new ClassPathResource(DCC_VALIDATION_RULE_JSON_CLASSPATH);

    when(resourceLoader.getResource(any())).thenReturn(validationSchema);
    when(digitalCovidCertificateClient.getRules()).thenReturn(Collections.singletonList(mockBusinessRuleItem()));
    when(digitalCovidCertificateClient.getCountryRuleByHash(any(),any())).thenReturn(Optional.of(mockBusinessRule()));

    DigitalCovidCertificateException exception = assertThrows(DigitalCovidCertificateException.class,
            () -> digitalGreenCertificateToCborMapping.constructRules(RuleType.Acceptance));
    assertThat(exception.getMessage()).contains("is not valid");
  }

  @Test
  void shouldThrowWhenValidationSchemaIsNotFound() throws DigitalCovidCertificateException {
    Resource validationSchema = new ClassPathResource(RANDOM_STRING);

    when(resourceLoader.getResource(any())).thenReturn(validationSchema);
    when(digitalCovidCertificateClient.getRules()).thenReturn(Collections.singletonList(mockBusinessRuleItem()));
    when(digitalCovidCertificateClient.getCountryRuleByHash(any(),any())).thenReturn(Optional.of(mockBusinessRule()));

    DigitalCovidCertificateException exception = assertThrows(DigitalCovidCertificateException.class,
        () -> digitalGreenCertificateToCborMapping.constructRules(RuleType.Acceptance));
    assertThat(exception.getMessage()).contains("could not be found");
  }

  private BusinessRuleItem mockBusinessRuleItem() {
    BusinessRuleItem ruleItem = new BusinessRuleItem();
    ruleItem.setCountry(DE);
    ruleItem.setHash(RANDOM_STRING);

    return ruleItem;
  }

  private BusinessRule mockBusinessRule() {
    BusinessRule businessRule = new BusinessRule();
    businessRule.setIdentifier(BAD_IDENTIFIER);
    businessRule.setType(RuleType.Acceptance.name());
    businessRule.setCountry(DE);

    return businessRule;
  }

}
