package app.coronawarn.server.services.distribution.assembly.component;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.coronawarn.server.common.shared.collection.ImmutableStack;
import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryOnDisk;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.dgc.DigitalGreenCertificateToCborMapping;
import app.coronawarn.server.services.distribution.dgc.DigitalGreenCertificateToProtobufMapping;
import app.coronawarn.server.services.distribution.dgc.client.DigitalCovidCertificateClient;
import app.coronawarn.server.services.distribution.dgc.client.TestDigitalCovidCertificateClient;
import app.coronawarn.server.services.distribution.dgc.dsc.DigitalSigningCertificatesClient;
import app.coronawarn.server.services.distribution.dgc.dsc.DigitalSigningCertificatesToProtobufMapping;
import app.coronawarn.server.services.distribution.dgc.exception.FetchBusinessRulesException;
import app.coronawarn.server.services.distribution.dgc.exception.FetchValueSetsException;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.rules.TemporaryFolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@EnableConfigurationProperties(value = {DistributionServiceConfig.class})
@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    classes = {DigitalGreenCertificateToProtobufMapping.class, DigitalGreenCertificateToCborMapping.class,
        CryptoProvider.class, DistributionServiceConfig.class, TestDigitalCovidCertificateClient.class,
        DigitalSigningCertificatesToProtobufMapping.class, DigitalSigningCertificatesClient.class,
        BusinessRulesArchiveBuilder.class},
    initializers = ConfigDataApplicationContextInitializer.class)
@ActiveProfiles({"fake-dcc-client", "fake-dsc-client"})
class ValueSetsNotFetchedStructureProviderTest {

  private static final String PARENT_TEST_FOLDER = "parent";

  @Autowired
  DistributionServiceConfig distributionServiceConfig;

  @Autowired
  CryptoProvider cryptoProvider;

  @MockBean
  DigitalGreenCertificateToProtobufMapping dgcToProtobufMapping;

  @Autowired
  DigitalGreenCertificateToCborMapping dgcToCborMappingMock;

  @Autowired
  BusinessRulesArchiveBuilder businessRulesArchiveBuilder;

  @MockBean
  OutputDirectoryProvider outputDirectoryProvider;

  @Autowired
  DigitalSigningCertificatesToProtobufMapping digitalSigningCertificatesToProtobufMapping;

  @MockBean
  DigitalSigningCertificatesClient digitalSigningCertificatesClient;

  @Autowired
  DigitalCovidCertificateClient digitalCovidCertificateClient;

  @Autowired
  ResourceLoader resourceLoader;

  @Rule
  TemporaryFolder testOutputFolder = new TemporaryFolder();

  @BeforeEach
  public void setup() throws IOException, FetchValueSetsException, FetchBusinessRulesException {
    // create a specific test folder for later assertions of structures.
    testOutputFolder.create();
    File outputDirectory = testOutputFolder.newFolder(PARENT_TEST_FOLDER);
    Directory<WritableOnDisk> testDirectory = new DirectoryOnDisk(outputDirectory);
    when(outputDirectoryProvider.getDirectory()).thenReturn(testDirectory);
    when(dgcToProtobufMapping.constructProtobufMapping()).thenThrow(FetchValueSetsException.class);
  }

  @Test
  void should_not_contain_valuesets_if_any_is_not_fetched() throws FetchValueSetsException {
    DigitalCertificatesStructureProvider underTest = new DigitalCertificatesStructureProvider(
        distributionServiceConfig, cryptoProvider, dgcToProtobufMapping, dgcToCborMappingMock,
        digitalSigningCertificatesToProtobufMapping, digitalCovidCertificateClient, businessRulesArchiveBuilder);
    DirectoryOnDisk digitalGreenCertificates = underTest.getDigitalGreenCertificates();
    digitalGreenCertificates.prepare(new ImmutableStack<>());

    assertEquals("ehn-dgc", digitalGreenCertificates.getName());

    List<String> expectedLanguages = Arrays
        .stream(distributionServiceConfig.getDigitalGreenCertificate().getSupportedLanguages())
        .map(String::toLowerCase)
        .collect(Collectors.toList());
    List<String> writableNames = digitalGreenCertificates.getWritables()
        .stream().map(Writable::getName).collect(Collectors.toList());
    assertTrue(writableNames.containsAll(expectedLanguages));

    boolean areLanguageFoldersEmpty = digitalGreenCertificates.getWritables().stream()
        .filter(writableOnDiskWritable -> expectedLanguages.contains(writableOnDiskWritable.getName()))
        .filter(writableOnDiskWritable -> writableOnDiskWritable instanceof DirectoryOnDisk)
        .allMatch(directory -> ((DirectoryOnDisk) directory).getWritables().isEmpty());

    assertTrue(areLanguageFoldersEmpty);
    verify(dgcToProtobufMapping, times(1)).constructProtobufMapping();
  }
}
