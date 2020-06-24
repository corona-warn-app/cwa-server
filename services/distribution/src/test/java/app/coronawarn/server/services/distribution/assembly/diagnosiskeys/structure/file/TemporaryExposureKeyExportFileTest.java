package app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.file;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.Signature;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Set;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.rules.TemporaryFolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@EnableConfigurationProperties(value = DistributionServiceConfig.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DistributionServiceConfig.class},
    initializers = ConfigFileApplicationContextInitializer.class)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
class TemporaryExposureKeyExportFileTest {

  public static final String TEK_EXPORT_CHECKSUM_FILE_NAME = "export.bin.checksum";

  @Rule
  TemporaryFolder outputFolder = new TemporaryFolder();

  @Autowired
  DistributionServiceConfig distributionServiceConfig;

  @BeforeEach
  void setup() throws IOException {
    outputFolder.create();
  }

  @Test
  void testChecksumIsDeterministic() throws IOException {
    File outputFile1 = createFile();
    byte[] checksum1 = readChecksumFile(outputFile1);

    File outputFile2 = createFile();
    byte[] checksum2 = readChecksumFile(outputFile2);

    assertThat(Arrays.equals(checksum1, checksum2));
  }

  @Test
  void testChecksumChangesWhenPrivateKeyIsModified() throws IOException {
    File outputFile1 = createFile();
    byte[] checksum1 = readChecksumFile(outputFile1);

    modifySignaturePrivateKeyVersion();

    File outputFile2 = createFile();
    byte[] checksum2 = readChecksumFile(outputFile2);

    assertThat(!(Arrays.equals(checksum1, checksum2)));
  }

  private TemporaryExposureKeyExportFile createTemporaryExposureKeyExportFile() {
    return TemporaryExposureKeyExportFile.fromDiagnosisKeys(
        Set.of(
            DiagnosisKey.builder()
                .withKeyData(new byte[16])
                .withRollingStartIntervalNumber(1)
                .withTransmissionRiskLevel(1)
                .withRollingPeriod(144)
                .withSubmissionTimestamp(1)
                .build()
        ),
        "DE", 0, 10, distributionServiceConfig
    );
  }

  private File createFile() throws IOException {
    TemporaryExposureKeyExportFile tekExportFile = createTemporaryExposureKeyExportFile();
    File outputFile = outputFolder.newFolder();
    Directory<WritableOnDisk> directory = new DirectoryOnDisk(outputFile);
    directory.addWritable(tekExportFile);
    directory.prepare(new ImmutableStack<>());
    directory.write();
    return outputFile;
  }

  private byte[] readChecksumFile(File outputFile1) throws IOException {
    return Files.readAllBytes(outputFile1.toPath().resolve(TEK_EXPORT_CHECKSUM_FILE_NAME));
  }

  private void modifySignaturePrivateKeyVersion() {
    Signature signature = distributionServiceConfig.getSignature();
    String verificationKeyVersion = signature.getVerificationKeyVersion();
    signature.setVerificationKeyVersion(verificationKeyVersion + "x");
  }
}
