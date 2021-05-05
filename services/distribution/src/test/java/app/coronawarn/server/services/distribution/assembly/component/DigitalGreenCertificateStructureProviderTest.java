package app.coronawarn.server.services.distribution.assembly.component;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.archive.decorator.signing.DistributionArchiveSigningDecorator;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.dgc.DigitalGreenCertificateToProtobufMapping;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
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
    classes = {DigitalGreenCertificateToProtobufMapping.class, CryptoProvider.class},
    initializers = ConfigDataApplicationContextInitializer.class)
public class DigitalGreenCertificateStructureProviderTest {

  private static final String PARENT_TEST_FOLDER = "parent";

  @Autowired
  DistributionServiceConfig distributionServiceConfig;

  @Autowired
  CryptoProvider cryptoProvider;

  @Autowired
  DigitalGreenCertificateToProtobufMapping dgcToProtobufMapping;

  @MockBean
  OutputDirectoryProvider outputDirectoryProvider;

  @Rule
  TemporaryFolder testOutputFolder = new TemporaryFolder();

  DigitalGreenCertificateStructureProvider underTest;

  private DirectoryOnDisk digitalGreenCertificates;

  @BeforeEach
  public void setup() throws IOException {
    underTest = new DigitalGreenCertificateStructureProvider(distributionServiceConfig, cryptoProvider,
        dgcToProtobufMapping);
    // create a specific test folder for later assertions of structures.
    testOutputFolder.create();
    File outputDirectory = testOutputFolder.newFolder(PARENT_TEST_FOLDER);
    Directory<WritableOnDisk> testDirectory = new DirectoryOnDisk(outputDirectory);
    when(outputDirectoryProvider.getDirectory()).thenReturn(testDirectory);
    digitalGreenCertificates = underTest.getDigitalGreenCertificates();
    digitalGreenCertificates.prepare(new ImmutableStack<>());
  }

  @Test
  void should_create_correct_file_structure() {
    assertEquals("ehn-dgc", digitalGreenCertificates.getName());
    DirectoryOnDisk valueSet = (DirectoryOnDisk) digitalGreenCertificates.getWritables().iterator().next();
    assertEquals("value-sets", valueSet.getName());

    DirectoryOnDisk en = (DirectoryOnDisk) valueSet.getWritables().iterator().next();
    assertEquals("en", en.getName());

    Writable index = en.getWritables().iterator().next();
    assertEquals("index", index.getName());

    List<String> archiveContent = ((DistributionArchiveSigningDecorator) index).getWritables().stream()
        .map(Writable::getName).collect(Collectors.toList());

    assertThat(archiveContent).containsAll(Set.of("export.bin", "export.sig"));
  }
}
