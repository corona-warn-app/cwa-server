package app.coronawarn.server.services.distribution.assembly.component;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import app.coronawarn.server.common.shared.collection.ImmutableStack;
import app.coronawarn.server.services.distribution.assembly.qrcode.QrCodeTemplateLoader;
import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.archive.Archive;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryOnDisk;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.config.TransmissionRiskLevelEncoding;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Rule;
import org.junit.jupiter.api.Assertions;
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
    classes = {QrCodeTemplateLoader.class, CryptoProvider.class},
    initializers = ConfigDataApplicationContextInitializer.class)
public class QrCodePosterTemplateStructureProviderTest {

  private static final String PARENT_TEST_FOLDER = "parent";

  @Autowired
  DistributionServiceConfig distributionServiceConfig;

  @Autowired
  CryptoProvider cryptoProvider;

  @Autowired
  QrCodeTemplateLoader qrCodeTemplateLoader;

  @MockBean
  OutputDirectoryProvider outputDirectoryProvider;

  QrCodePosterTemplateStructureProvider underTest;

  @Rule
  TemporaryFolder testOutputFolder = new TemporaryFolder();

  private Writable<WritableOnDisk> qrArchiveAndroid;
  private Writable<WritableOnDisk> qrArchiveIos;

  @BeforeEach
  public void setup() throws IOException {
    underTest = new QrCodePosterTemplateStructureProvider(distributionServiceConfig, cryptoProvider,
        qrCodeTemplateLoader);
    // create a specific test folder for later assertions of structures.
    testOutputFolder.create();
    File outputDirectory = testOutputFolder.newFolder(PARENT_TEST_FOLDER);
    Directory<WritableOnDisk> testDirectory = new DirectoryOnDisk(outputDirectory);
    when(outputDirectoryProvider.getDirectory()).thenReturn(testDirectory);
    qrArchiveAndroid = underTest.getQrCodeTemplateForAndroid();
    qrArchiveIos = underTest.getQrCodeTemplateForIos();
    qrArchiveAndroid.prepare(new ImmutableStack<>());
    qrArchiveIos.prepare(new ImmutableStack<>());
  }

  @Test
  void should_create_correct_file_structure() {
    Assertions.assertEquals("qr_code_poster_template_android", qrArchiveAndroid.getName());
    Assertions.assertEquals("qr_code_poster_template_ios", qrArchiveIos.getName());
  }

  @Test
  void should_create_signed_archive() {
    Collection<String> archiveContent;

    archiveContent = ((Archive<WritableOnDisk>) qrArchiveAndroid).getWritables().stream()
        .map(Writable::getName).collect(Collectors.toList());
    assertThat(archiveContent).containsAll(Set.of("export.bin", "export.sig"));

    archiveContent = ((Archive<WritableOnDisk>) qrArchiveIos).getWritables().stream()
        .map(Writable::getName).collect(Collectors.toList());
    assertThat(archiveContent).containsAll(Set.of("export.bin", "export.sig"));
  }
}
