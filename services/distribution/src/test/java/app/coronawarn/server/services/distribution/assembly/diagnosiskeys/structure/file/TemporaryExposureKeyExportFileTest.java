/*-
 * ---license-start
 * Corona-Warn-App
 * ---
 * Copyright (C) 2020 SAP SE and all other contributors
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ---license-end
 */

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

    assertThat(checksum1).isEqualTo(checksum2);
  }

  @Test
  void testChecksumChangesWhenPrivateKeyIsModified() throws IOException {
    File outputFile1 = createFile();
    byte[] checksum1 = readChecksumFile(outputFile1);

    modifySignaturePrivateKeyVersion();

    File outputFile2 = createFile();
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
