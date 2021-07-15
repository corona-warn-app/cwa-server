package app.coronawarn.server.services.distribution.assembly.component;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import app.coronawarn.server.common.shared.collection.ImmutableStack;
import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.archive.decorator.signing.DistributionArchiveSigningDecorator;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryOnDisk;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.dgc.BusinessRule;
import app.coronawarn.server.services.distribution.dgc.BusinessRule.RuleType;
import app.coronawarn.server.services.distribution.dgc.BusinessRuleItem;
import app.coronawarn.server.services.distribution.dgc.DigitalGreenCertificateToCborMapping;
import app.coronawarn.server.services.distribution.dgc.DigitalGreenCertificateToProtobufMapping;
import app.coronawarn.server.services.distribution.dgc.client.DigitalCovidCertificateClient;
import app.coronawarn.server.services.distribution.dgc.client.ProdDigitalCovidCertificateClient;
import app.coronawarn.server.services.distribution.dgc.exception.FetchBusinessRulesException;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.rules.TemporaryFolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@EnableConfigurationProperties(value = {DistributionServiceConfig.class})
@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    classes = {DigitalGreenCertificateToProtobufMapping.class, DigitalGreenCertificateToCborMapping.class,
        CryptoProvider.class, DistributionServiceConfig.class, ProdDigitalCovidCertificateClient.class},
    initializers = ConfigDataApplicationContextInitializer.class)
class DccRulesNotFetchedStructureProviderTest {

  private static final String PARENT_TEST_FOLDER = "parent";

  @Autowired
  DistributionServiceConfig distributionServiceConfig;

  @Autowired
  CryptoProvider cryptoProvider;

  @Autowired
  DigitalGreenCertificateToProtobufMapping dgcToProtobufMapping;

  @Autowired
  DigitalGreenCertificateToCborMapping dgcToCborMappingMock;

  @MockBean
  OutputDirectoryProvider outputDirectoryProvider;

  @MockBean
  DigitalCovidCertificateClient digitalCovidCertificateClient;

  @Rule
  TemporaryFolder testOutputFolder = new TemporaryFolder();

  @BeforeEach
  public void setup() throws IOException {
    // create a specific test folder for later assertions of structures.
    testOutputFolder.create();
    File outputDirectory = testOutputFolder.newFolder(PARENT_TEST_FOLDER);
    Directory<WritableOnDisk> testDirectory = new DirectoryOnDisk(outputDirectory);
    when(outputDirectoryProvider.getDirectory()).thenReturn(testDirectory);
  }

  @Test
  void should_not_contain_acceptance_or_invalidation_rules() throws FetchBusinessRulesException {
    when(digitalCovidCertificateClient.getRules()).thenThrow(new FetchBusinessRulesException("not fetched"));
    when(digitalCovidCertificateClient.getCountryList()).thenReturn(Arrays.asList("DE", "RO"));

    DirectoryOnDisk digitalGreenCertificates = getStructureProviderDirectory();
    assertEquals("ehn-dgc", digitalGreenCertificates.getName());

    List<Writable<WritableOnDisk>> businessRulesArchives = getBusinessRulesArchives(digitalGreenCertificates);
    assertThat(businessRulesArchives).hasSize(1);

    assertThat(businessRulesArchives.stream().filter(filterByArchiveName("onboarded-countries"))).hasSize(1);
    assertThat(businessRulesArchives.stream().filter(filterByArchiveName("acceptance-rules"))).hasSize(0);
    assertThat(businessRulesArchives.stream().filter(filterByArchiveName("invalidation-rules"))).hasSize(0);
  }

  @Test
  void should_contain_empty_acceptance_or_invalidation_rules() throws FetchBusinessRulesException {
    when(digitalCovidCertificateClient.getRules()).thenReturn(Collections.emptyList());
    when(digitalCovidCertificateClient.getCountryList()).thenReturn(Arrays.asList("DE", "RO"));

    DirectoryOnDisk digitalGreenCertificates = getStructureProviderDirectory();
    assertEquals("ehn-dgc", digitalGreenCertificates.getName());

    List<Writable<WritableOnDisk>> businessRulesArchives = getBusinessRulesArchives(digitalGreenCertificates);
    assertThat(businessRulesArchives).hasSize(3);

    assertThat(businessRulesArchives.stream().filter(filterByArchiveName("onboarded-countries"))).hasSize(1);
    assertThat(businessRulesArchives.stream().filter(filterByArchiveName("acceptance-rules"))).hasSize(1);
    assertThat(businessRulesArchives.stream().filter(filterByArchiveName("invalidation-rules"))).hasSize(1);
  }

  @Test
  void should_contain_empty_invalidation_rules() throws FetchBusinessRulesException {
    BusinessRuleItem businessRuleItem = new BusinessRuleItem();
    businessRuleItem.setHash("test1");
    businessRuleItem.setCountry("test1");

    BusinessRuleItem businessRuleItem2 = new BusinessRuleItem();
    businessRuleItem2.setHash("test2");
    businessRuleItem2.setCountry("test2");

    BusinessRule businessRule = new BusinessRule();
    businessRule.setType(RuleType.Acceptance.name());
    BusinessRule businessRule2 = new BusinessRule();
    businessRule2.setType(RuleType.Acceptance.name());

    when(digitalCovidCertificateClient.getRules()).thenReturn(Arrays.asList(businessRuleItem, businessRuleItem2));
    when(digitalCovidCertificateClient.getCountryRuleByHash(eq("test1"), eq("test1")))
        .thenReturn(Optional.of(businessRule));
    when(digitalCovidCertificateClient.getCountryRuleByHash(eq("test2"), eq("test2")))
        .thenReturn(Optional.of(businessRule));
    when(digitalCovidCertificateClient.getCountryList()).thenReturn(Arrays.asList("DE", "RO"));

    DirectoryOnDisk digitalGreenCertificates = getStructureProviderDirectory();
    assertEquals("ehn-dgc", digitalGreenCertificates.getName());

    List<Writable<WritableOnDisk>> businessRulesArchives = getBusinessRulesArchives(digitalGreenCertificates);
    assertThat(businessRulesArchives).hasSize(2);

    assertThat(businessRulesArchives.stream().filter(filterByArchiveName("onboarded-countries"))).hasSize(1);
    // acceptance rules are invalid, they do not pass validation schema, thus archive won't be overwritten.
    assertThat(businessRulesArchives.stream().filter(filterByArchiveName("acceptance-rules"))).hasSize(0);
    // there are no invalid rules, thus they will be overwritten.
    assertThat(businessRulesArchives.stream().filter(filterByArchiveName("invalidation-rules"))).hasSize(1);
  }

  private Predicate<Writable<WritableOnDisk>> filterByArchiveName(String archiveName) {
    return writable -> writable.getName().equals(archiveName);
  }

  private DirectoryOnDisk getStructureProviderDirectory() {
    DigitalGreenCertificateStructureProvider underTest = new DigitalGreenCertificateStructureProvider(
        distributionServiceConfig, cryptoProvider, dgcToProtobufMapping, dgcToCborMappingMock);
    DirectoryOnDisk digitalGreenCertificates = underTest.getDigitalGreenCertificates();
    digitalGreenCertificates.prepare(new ImmutableStack<>());

    return digitalGreenCertificates;
  }

  private List<Writable<WritableOnDisk>> getBusinessRulesArchives(DirectoryOnDisk digitalGreenCertificates) {
    return digitalGreenCertificates.getWritables().stream()
        .filter(writableOnDisk -> writableOnDisk instanceof DistributionArchiveSigningDecorator)
        .collect(Collectors.toList());
  }
}
