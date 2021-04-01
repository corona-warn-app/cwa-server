package app.coronawarn.server.services.distribution.assembly.component;

import static org.mockito.Mockito.when;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import org.junit.Rule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import app.coronawarn.server.common.persistence.service.TraceTimeIntervalWarningService;
import app.coronawarn.server.common.persistence.service.common.KeySharingPoliciesChecker;
import app.coronawarn.server.services.distribution.assembly.qrcode.QrCodeTemplateLoader;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.tracewarnings.TraceTimeIntervalWarningsPackageBundler;
import app.coronawarn.server.services.distribution.assembly.transformation.EnfParameterAdapter;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.config.TransmissionRiskLevelEncoding;

@EnableConfigurationProperties(
    value = {DistributionServiceConfig.class, TransmissionRiskLevelEncoding.class})
@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    classes = {DistributionServiceConfig.class,QrCodeTemplateLoader.class},
    initializers = ConfigFileApplicationContextInitializer.class)
public class QrCodePosterTemplateStructureProviderTest {

  private static final String PARENT_TEST_FOLDER = "parent";

  @Autowired
  DistributionServiceConfig distributionServiceConfig;

  @Autowired
  QrCodeTemplateLoader qrCodeTemplateLoader;

  @Mock
  TraceTimeIntervalWarningService traceTimeWarningService;

  @MockBean
  OutputDirectoryProvider outputDirectoryProvider;

  QrCodePosterTemplateStructureProvider underTest;

  @Rule
  TemporaryFolder testOutputFolder = new TemporaryFolder();

  @BeforeEach
  public void setup() throws IOException {
    underTest = new QrCodePosterTemplateStructureProvider(distributionServiceConfig, qrCodeTemplateLoader);
    // create a specific test folder for later assertions of structures.
    testOutputFolder.create();
    File outputDirectory = testOutputFolder.newFolder(PARENT_TEST_FOLDER);
    Directory<WritableOnDisk> testDirectory = new DirectoryOnDisk(outputDirectory);
    when(outputDirectoryProvider.getDirectory()).thenReturn(testDirectory);
  }

  @Test
  void should_create_correct_file_structure() {
    WritableOnDisk qrArchiveAndroid = underTest.getQrCodeTemplateForAndroid();
    WritableOnDisk qrArchiveIos = underTest.getQrCodeTemplateForIos();
    Assertions.assertEquals("qr_code_poster_template_android", qrArchiveAndroid.getName());
    Assertions.assertEquals("qr_code_poster_template_ios", qrArchiveIos.getName());
  }
}