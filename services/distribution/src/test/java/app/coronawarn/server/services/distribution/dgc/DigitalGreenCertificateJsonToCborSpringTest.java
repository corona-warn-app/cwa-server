package app.coronawarn.server.services.distribution.dgc;

import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.dgc.BusinessRule.RuleType;
import app.coronawarn.server.services.distribution.dgc.client.DigitalCovidCertificateClient;
import app.coronawarn.server.services.distribution.dgc.client.TestDigitalCovidCertificateClient;
import app.coronawarn.server.services.distribution.dgc.exception.DigitalCovidCertificateException;
import app.coronawarn.server.services.distribution.dgc.exception.FetchBusinessRulesException;
import java.util.List;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@EnableConfigurationProperties(value = DistributionServiceConfig.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DistributionServiceConfig.class, DigitalGreenCertificateToCborMapping.class,
    TestDigitalCovidCertificateClient.class},
    initializers = ConfigDataApplicationContextInitializer.class)
@ActiveProfiles("fake-dcc-client")
class DigitalGreenCertificateJsonToCborSpringTest {

  public static final String ID_ACCEPTANCE_1 = "RR-NL-0000";
  public static final String ID_ACCEPTANCE_2 = "TR-DE-0003";
  public static final String ID_INVALIDATION_1 = "RR-NL-0003";
  public static final String ID_BN_1 = "BNR-DE-3298";
  public static final String DE = "DE";

  @Autowired
  DistributionServiceConfig distributionServiceConfig;

  @Autowired
  DigitalGreenCertificateToCborMapping digitalGreenCertificateToCborMapping;

  @Autowired
  DigitalCovidCertificateClient digitalCovidCertificateClient;

  @Test
  void shouldConstructCorrectAcceptanceRules() throws DigitalCovidCertificateException, FetchBusinessRulesException {
    List<BusinessRule> businessRules = digitalGreenCertificateToCborMapping
        .constructRules(RuleType.Acceptance, digitalCovidCertificateClient::getRules,
            digitalCovidCertificateClient::getCountryRuleByHash);

    assertThat(businessRules).hasSize(2);
    assertThat(businessRules.stream().filter(filterByRuleType(RuleType.Acceptance))).hasSize(2);
    assertThat(businessRules.stream().filter(filterByRuleIdentifier(ID_ACCEPTANCE_1)).findAny()).isPresent();
    assertThat(businessRules.stream().filter(filterByRuleIdentifier(ID_ACCEPTANCE_2)).findAny()).isPresent();
  }

  @Test
  void shouldConstructCorrectBnRules() throws DigitalCovidCertificateException, FetchBusinessRulesException {
    List<BusinessRule> businessRules = digitalGreenCertificateToCborMapping
        .constructRules(RuleType.BoosterNotification, digitalCovidCertificateClient::getBoosterNotificationRules,
            digitalCovidCertificateClient::getBoosterNotificationRuleByHash);
    assertThat(businessRules).hasSize(1);
    assertThat(businessRules.stream().filter(filterByRuleType(RuleType.BoosterNotification))).hasSize(1);
    assertThat(businessRules.stream().filter(filterByRuleIdentifier(ID_BN_1)).findAny()).isPresent();
  }

  @Test
  void shouldConstructCorrectInvalidationRules() throws DigitalCovidCertificateException, FetchBusinessRulesException {
    List<BusinessRule> businessRules = digitalGreenCertificateToCborMapping
        .constructRules(RuleType.Invalidation, digitalCovidCertificateClient::getRules,
            digitalCovidCertificateClient::getCountryRuleByHash);

    assertThat(businessRules).hasSize(1);
    assertThat(businessRules.stream().filter(filterByRuleType(RuleType.Invalidation))).hasSize(1);
    assertThat(businessRules.stream().filter(filterByRuleIdentifier(ID_INVALIDATION_1)).findAny()).isPresent();
  }

  @Test
  void shouldConstructCborAcceptanceRules() throws DigitalCovidCertificateException, FetchBusinessRulesException {
    byte[] businessRules = digitalGreenCertificateToCborMapping
        .constructCborRules(RuleType.Acceptance, digitalCovidCertificateClient::getRules,
            digitalCovidCertificateClient::getCountryRuleByHash);

    assertThat(businessRules).isNotEmpty();
  }

  private Predicate<BusinessRule> filterByRuleType(RuleType ruleType) {
    return businessRule -> businessRule.getType().equalsIgnoreCase(ruleType.name());
  }

  private Predicate<BusinessRule> filterByRuleIdentifier(String identifier) {
    return businessRule -> businessRule.getIdentifier().equalsIgnoreCase(identifier);
  }

}
