package app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.file;

import static app.coronawarn.server.services.distribution.common.Helpers.buildDiagnosisKeyForSubmissionTimestamp;
import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.Signature;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@EnableConfigurationProperties(value = DistributionServiceConfig.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DistributionServiceConfig.class},
    initializers = ConfigFileApplicationContextInitializer.class)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
class TemporaryExposureKeyExportFileTest {

  public static final String TEK_EXPORT_CHECKSUM_FILE_NAME = "export.bin.checksum";

  @Autowired
  DistributionServiceConfig distributionServiceConfig;

  @Test
  void testChecksumIsDeterministic(@TempDir File outputFile) throws IOException {
    File outputFile1 = createFile(new File(outputFile,"f1"));
    byte[] checksum1 = readChecksumFile(outputFile1);

    File outputFile2 = createFile(new File(outputFile,"f2"));
    byte[] checksum2 = readChecksumFile(outputFile2);

    assertThat(checksum1).isEqualTo(checksum2);
  }

  @Test
  void testChecksumChangesWhenPrivateKeyIsModified(@TempDir File outputFile) throws IOException {
    File outputFile1 = createFile(new File(outputFile,"f1"));
    byte[] checksum1 = readChecksumFile(outputFile1);

    modifySignaturePrivateKeyVersion();

    File outputFile2 = createFile(new File(outputFile,"f2"));
    byte[] checksum2 = readChecksumFile(outputFile2);

    assertThat(checksum1).isNotEqualTo(checksum2);
  }

  private TemporaryExposureKeyExportFile createTemporaryExposureKeyExportFile() {
    return TemporaryExposureKeyExportFile.fromDiagnosisKeys(
        Set.of(
            buildDiagnosisKeyForSubmissionTimestamp(1)
        ),
        "DE", 0, 10, distributionServiceConfig
    );
  }

  private File createFile(File outputFile) throws IOException {
    TemporaryExposureKeyExportFile tekExportFile = createTemporaryExposureKeyExportFile();
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
