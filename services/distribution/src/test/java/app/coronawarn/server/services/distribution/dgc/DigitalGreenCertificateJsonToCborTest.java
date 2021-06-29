package app.coronawarn.server.services.distribution.dgc;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.dgc.BusinessRule.RuleType;
import app.coronawarn.server.services.distribution.dgc.client.TestDigitalCovidCertificateClient;
import app.coronawarn.server.services.distribution.dgc.exception.DigitalCovidCertificateException;
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

import static org.assertj.core.api.Assertions.assertThat;

@EnableConfigurationProperties(value = DistributionServiceConfig.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DistributionServiceConfig.class, DigitalGreenCertificateToCborMapping.class,
    TestDigitalCovidCertificateClient.class},
    initializers = ConfigDataApplicationContextInitializer.class)
@ActiveProfiles("fake-dcc-client")
class DigitalGreenCertificateJsonToCborTest {

  public static final String ID_ACCEPTANCE_1 = "GR-CZ-0001";
  public static final String ID_ACCEPTANCE_2 = "VR-RO-1";
  public static final String ID_INVALIDATION_1 = "VR-DE-1";

  @Autowired
  DistributionServiceConfig distributionServiceConfig;

  @Autowired
  DigitalGreenCertificateToCborMapping digitalGreenCertificateToCborMapping;

  @Test
  void shouldConstructCorrectAcceptanceRules() throws DigitalCovidCertificateException {
    List<BusinessRule> businessRules = digitalGreenCertificateToCborMapping.constructRules(RuleType.Acceptance);

    assertThat(businessRules).hasSize(2);
    assertThat(businessRules.stream().filter(filterByRuleType(RuleType.Acceptance))).hasSize(2);
    assertThat(businessRules.stream().filter(filterByRuleIdentifier(ID_ACCEPTANCE_1)).findAny()).isPresent();
    assertThat(businessRules.stream().filter(filterByRuleIdentifier(ID_ACCEPTANCE_2)).findAny()).isPresent();
  }

  @Test
  void shouldConstructCorrectInvalidationRules() throws DigitalCovidCertificateException {
    List<BusinessRule> businessRules = digitalGreenCertificateToCborMapping.constructRules(RuleType.Invalidation);

    assertThat(businessRules).hasSize(1);
    assertThat(businessRules.stream().filter(filterByRuleType(RuleType.Invalidation))).hasSize(1);
    assertThat(businessRules.stream().filter(filterByRuleIdentifier(ID_INVALIDATION_1)).findAny()).isPresent();
  }

  private Predicate<BusinessRule> filterByRuleType(RuleType ruleType) {
    return businessRule -> businessRule.getType().equalsIgnoreCase(ruleType.name());
  }

  private Predicate<BusinessRule> filterByRuleIdentifier(String identifier) {
    return businessRule -> businessRule.getIdentifier().equalsIgnoreCase(identifier);
  }

}
