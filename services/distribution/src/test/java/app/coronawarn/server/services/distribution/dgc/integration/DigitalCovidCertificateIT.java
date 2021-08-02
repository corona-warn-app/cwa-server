package app.coronawarn.server.services.distribution.dgc.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.dgc.ApacheHttpTestConfiguration;
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
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
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

  private static final Logger logger = LoggerFactory.getLogger(DigitalCovidCertificateIT.class);

  @Autowired
  private DigitalCovidCertificateClient digitalCovidCertificateClient;

  @Test
  public void shouldFetchAllRulesItemsAndEachRuleAfter() throws FetchBusinessRulesException {
    List<BusinessRuleItem> rules = digitalCovidCertificateClient.getRules();
    assertThat(rules).isNotEmpty();

    for (BusinessRuleItem businessRuleItem : rules) {
      digitalCovidCertificateClient.getCountryRuleByHash(businessRuleItem.getCountry(), businessRuleItem.getHash())
          .ifPresent(businessRule -> {
            assertThat(businessRule.getCountry()).isEqualTo(businessRuleItem.getCountry());
            assertThat(businessRule.getIdentifier()).isNotEmpty();
            assertTrue(isAcceptanceOrInvalidation(businessRule.getType()));
          });
    }
  }

  @Test
  public void shouldFetchCountryList() throws FetchBusinessRulesException {
    List<String> countries = digitalCovidCertificateClient.getCountryList();
    assertThat(countries).isNotEmpty();
  }

  @Test
  public void shouldFetchAllValuesetsMetadataAndEachValuesetAfter() throws FetchValueSetsException {
    List<ValueSetMetadata> valuesets = digitalCovidCertificateClient.getValueSets();
    assertThat(valuesets).isNotEmpty();

    int counter = 0;
    for (ValueSetMetadata valueSetMetadata : valuesets) {
      try {
        Optional<ValueSet> valueSetOptional = digitalCovidCertificateClient.getValueSet(valueSetMetadata.getHash());
        assertThat(valueSetOptional).isPresent();
        assertThat(valueSetOptional.get().getValueSetId()).isNotEmpty();
        assertThat(valueSetOptional.get().getValueSetValues()).isNotEmpty();
        counter++;
      } catch (final FetchValueSetsException e) {
        logger.warn("Hash: '" + valueSetMetadata.getHash() + "' throwed exception!", e.getCause());
      }
    }
    assertTrue(counter > 0, "All valuesets failed!!!");
  }

  private boolean isAcceptanceOrInvalidation(String type) {
    return type.equalsIgnoreCase(RuleType.Invalidation.name()) || type.equalsIgnoreCase(RuleType.Acceptance.name());
  }
}
