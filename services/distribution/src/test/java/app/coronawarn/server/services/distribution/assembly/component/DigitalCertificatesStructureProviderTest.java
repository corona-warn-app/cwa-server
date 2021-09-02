package app.coronawarn.server.services.distribution.assembly.component;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import app.coronawarn.server.common.shared.collection.ImmutableStack;
import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.archive.decorator.signing.DistributionArchiveSigningDecorator;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryOnDisk;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.dgc.DigitalGreenCertificateToCborMapping;
import app.coronawarn.server.services.distribution.dgc.DigitalGreenCertificateToProtobufMapping;
import app.coronawarn.server.services.distribution.dgc.client.TestDigitalCovidCertificateClient;
import app.coronawarn.server.services.distribution.dgc.dsc.DigitalSigningCertificatesClient;
import app.coronawarn.server.services.distribution.dgc.dsc.DigitalSigningCertificatesToProtobufMapping;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@EnableConfigurationProperties(value = {DistributionServiceConfig.class})
@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    classes = {DigitalGreenCertificateToProtobufMapping.class, DigitalGreenCertificateToCborMapping.class,
        CryptoProvider.class, DistributionServiceConfig.class, TestDigitalCovidCertificateClient.class,
        DigitalSigningCertificatesToProtobufMapping.class, DigitalSigningCertificatesClient.class},
    initializers = ConfigDataApplicationContextInitializer.class)
@ActiveProfiles({"fake-dcc-client", "fake-dsc-client"})
class DigitalCertificatesStructureProviderTest {

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

  @Autowired
  DigitalSigningCertificatesToProtobufMapping digitalSigningCertificatesToProtobufMapping;

  @MockBean
  DigitalSigningCertificatesClient digitalSigningCertificatesClient;
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
  void should_create_correct_file_structure_for_valuesets() {
    DigitalCertificatesStructureProvider underTest = new DigitalCertificatesStructureProvider(
        distributionServiceConfig, cryptoProvider, dgcToProtobufMapping, dgcToCborMappingMock,
        digitalSigningCertificatesToProtobufMapping);
    DirectoryOnDisk digitalGreenCertificates = underTest.getDigitalGreenCertificates();
    digitalGreenCertificates.prepare(new ImmutableStack<>());

    assertEquals("ehn-dgc", digitalGreenCertificates.getName());
    List<String> supportedLanguages = digitalGreenCertificates.getWritables().stream().map(Writable::getName).collect(
        Collectors.toList());
    List<String> expectedLanguages = Arrays.asList("de", "en", "bg", "pl", "ro", "tr");
    assertTrue(supportedLanguages.containsAll(expectedLanguages));

    (digitalGreenCertificates.getWritables()).stream()
        .filter(writableOnDisk -> writableOnDisk instanceof DirectoryOnDisk)
        .map(directory -> ((DirectoryOnDisk) directory).getWritables().iterator().next())
        .forEach(valueSet -> {
          assertEquals("value-sets", valueSet.getName());
          List<String> archiveContent = ((DistributionArchiveSigningDecorator) valueSet).getWritables().stream()
              .map(Writable::getName).collect(Collectors.toList());
          assertTrue((archiveContent).containsAll(Set.of("export.bin", "export.sig")));
        });
  }

  @Test
  void should_create_correct_file_structure_for_business_rules() {
    DigitalCertificatesStructureProvider underTest = new DigitalCertificatesStructureProvider(
        distributionServiceConfig, cryptoProvider, dgcToProtobufMapping, dgcToCborMappingMock,
        digitalSigningCertificatesToProtobufMapping);
    DirectoryOnDisk digitalGreenCertificates = underTest.getDigitalGreenCertificates();
    digitalGreenCertificates.prepare(new ImmutableStack<>());

    assertEquals("ehn-dgc", digitalGreenCertificates.getName());

    List<Writable<WritableOnDisk>> businessRulesArchives = digitalGreenCertificates.getWritables().stream()
        .filter(writableOnDisk -> writableOnDisk instanceof DistributionArchiveSigningDecorator)
        .collect(Collectors.toList());

    assertThat(businessRulesArchives).hasSize(4);

    assertThat(businessRulesArchives.stream().filter(filterByArchiveName("onboarded-countries"))).hasSize(1);
    assertThat(businessRulesArchives.stream().filter(filterByArchiveName("acceptance-rules"))).hasSize(1);
    assertThat(businessRulesArchives.stream().filter(filterByArchiveName("invalidation-rules"))).hasSize(1);
    assertThat(businessRulesArchives.stream().filter(filterByArchiveName("dscs"))).hasSize(1);
  }

  private Predicate<Writable<WritableOnDisk>> filterByArchiveName(String archiveName) {
    return writable -> writable.getName().equals(archiveName);
  }
}
