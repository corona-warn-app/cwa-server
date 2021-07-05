package app.coronawarn.server.services.distribution.dgc;

import static app.coronawarn.server.services.distribution.dgc.client.TestDigitalCovidCertificateClient.AGENT_TARGETED_HASH;
import static app.coronawarn.server.services.distribution.dgc.client.TestDigitalCovidCertificateClient.RULE_3_HASH;
import static app.coronawarn.server.services.distribution.dgc.client.TestDigitalCovidCertificateClient.TEST_TYPE_HASH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.util.Strings.isNullOrEmpty;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.dgc.client.CloudDccFeignClientConfiguration;
import app.coronawarn.server.services.distribution.dgc.client.CloudDccFeignHttpClientProvider;
import app.coronawarn.server.services.distribution.dgc.client.DigitalCovidCertificateClient;
import app.coronawarn.server.services.distribution.dgc.client.ProdDigitalCovidCertificateClient;
import app.coronawarn.server.services.distribution.dgc.exception.DigitalCovidCertificateException;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.cloud.openfeign.clientconfig.HttpClientFeignConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

//@EnableConfigurationProperties(value = DistributionServiceConfig.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DistributionServiceConfig.class, ProdDigitalCovidCertificateClient.class,
    CloudDccFeignClientConfiguration.class, CloudDccFeignHttpClientProvider.class},
    initializers = ConfigDataApplicationContextInitializer.class)
//@EnableFeignClients(defaultConfiguration = CloudDccFeignClientConfiguration.class)
@ImportAutoConfiguration({FeignAutoConfiguration.class, HttpMessageConvertersAutoConfiguration.class})
//@ActiveProfiles("fake-dcc-client")
class DigitalCovidCertificateClientTest {

  public static final String DE_HASH = "6821d518570fe9f4417c482ff0d2582a7b6440f243a9034f812e0d71611b611f";
  public static final String NL_HASH = "7021d518570fe9f4417c482ff0d2582a7b6440f243a9034f812e0d71611b611f";
  public static final String CZ_HASH = "7221d518570fe9f4417c482ff0d2582a7b6440f243a9034f812e0d71611b611f";

  public static final String DE = "DE";
  public static final String RO = "RO";
  public static final String NL = "NL";
  public static final String IT = "IT";
  public static final String CZ = "CZ";

  public static final String VALUESET_1_ID = "covid-19-lab-test-type";
  public static final String VALUESET_1_ENTRY_1 = "LP6464-4";
  public static final String VALUESET_1_ENTRY_2 = "LP217198-3";

  public static final String VALUESET_2_ID = "disease-agent-targeted";
  public static final String VALUESET_2_ENTRY_1 = "840539006";

  @Autowired
  private DigitalCovidCertificateClient digitalCovidCertificateClient;

  @Test
  public void testCountryList() throws DigitalCovidCertificateException {
    List<String> countries = digitalCovidCertificateClient.getCountryList();

    assertThat(countries).isNotEmpty();
    assertThat(countries).hasSize(27);
    assertThat(countries.stream().filter(filterByCountryName(DE))).hasSize(1);
    assertThat(countries.stream().filter(filterByCountryName(RO))).hasSize(1);
    assertThat(countries.stream().filter(filterByCountryName(NL))).hasSize(1);
    assertThat(countries.stream().filter(filterByCountryName(IT))).hasSize(1);
  }

  @Test
  public void shouldReturnCorrectValueSetsByHash() throws DigitalCovidCertificateException {
    Optional<ValueSet> valueSet1 = digitalCovidCertificateClient.getValueSet(TEST_TYPE_HASH);

    assertThat(valueSet1).isPresent();
    assertThat(valueSet1.get().getValueSetId()).isEqualTo(VALUESET_1_ID);
    assertThat(valueSet1.get().getValueSetValues().get(VALUESET_1_ENTRY_1)).isNotNull();
    assertThat(valueSet1.get().getValueSetValues().get(VALUESET_1_ENTRY_2)).isNotNull();

    Optional<ValueSet> valueSet2 = digitalCovidCertificateClient.getValueSet(AGENT_TARGETED_HASH);
    assertThat(valueSet2).isPresent();
    assertThat(valueSet2.get().getValueSetId()).isEqualTo(VALUESET_2_ID);
    assertThat(valueSet2.get().getValueSetValues().get(VALUESET_2_ENTRY_1)).isNotNull();
  }

  @Test
  public void shouldThrowDccExceptionWhenTryingToRetrieveValuesetWithNonexistingHash() {
    assertThatExceptionOfType(DigitalCovidCertificateException.class).isThrownBy(
        () -> digitalCovidCertificateClient.getValueSet(RULE_3_HASH));
  }

  @Test
  public void shouldThrowDccExceptionWhenTryingToRetrieveRuleWithNonexistingHash() {
    assertThatExceptionOfType(DigitalCovidCertificateException.class).isThrownBy(
        () -> digitalCovidCertificateClient.getCountryRuleByHash(DE, NL));
  }

  @Test
  public void shouldReturnCorrectValueSets() throws DigitalCovidCertificateException {
    List<ValueSetMetadata> valueSets = digitalCovidCertificateClient.getValueSets();

    assertThat(valueSets).isNotEmpty();
    assertThat(valueSets).hasSize(8);
    assertThat(valueSets).hasSize(8);
    assertThat(valueSets.stream().filter(missingIdOrHash())).hasSize(0);
  }

  @Test
  public void shouldReturnCorrectCountryRulesByHash() throws DigitalCovidCertificateException {
    assertThat(digitalCovidCertificateClient.getCountryRuleByHash(DE, DE_HASH)).isPresent();
    assertThat(digitalCovidCertificateClient.getCountryRuleByHash(NL, NL_HASH)).isPresent();
    assertThat(digitalCovidCertificateClient.getCountryRuleByHash(CZ, CZ_HASH)).isPresent();
  }

  private Predicate<String> filterByCountryName(String name) {
    return countryName -> countryName.equals(name);
  }

  private Predicate<ValueSetMetadata> missingIdOrHash() {
    return valueSet -> isNullOrEmpty(valueSet.getId())
        || isNullOrEmpty(valueSet.getHash());
  }
}
