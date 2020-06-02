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

package app.coronawarn.server.services.distribution.assembly.structure.archive.decorator.signing;

import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.common.protocols.external.exposurenotification.SignatureInfo;
import app.coronawarn.server.common.protocols.external.exposurenotification.TEKSignature;
import app.coronawarn.server.common.protocols.external.exposurenotification.TEKSignatureList;
import app.coronawarn.server.services.distribution.assembly.component.CryptoProvider;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.archive.Archive;
import app.coronawarn.server.services.distribution.assembly.structure.archive.ArchiveOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.file.File;
import app.coronawarn.server.services.distribution.assembly.structure.file.FileOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.List;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.rules.TemporaryFolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@EnableConfigurationProperties(value = DistributionServiceConfig.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CryptoProvider.class, DistributionServiceConfig.class},
    initializers = ConfigFileApplicationContextInitializer.class)
class SigningDecoratorTest {

  @Autowired
  CryptoProvider cryptoProvider;

  @Autowired
  ResourceLoader resourceLoader;

  @Autowired
  DistributionServiceConfig distributionServiceConfig;

  private File<WritableOnDisk> fileToSign;
  private TEKSignatureList signatureList;

  @Rule
  private TemporaryFolder outputFolder = new TemporaryFolder();

  @BeforeEach
  void setup() throws IOException {
    Archive<WritableOnDisk> archive = new ArchiveOnDisk("export.zip");
    fileToSign = new FileOnDisk("export.bin", "123456".getBytes());
    archive.addWritable(fileToSign);

    SigningDecorator<WritableOnDisk> signingDecorator = new TestSigningDecorator(archive, cryptoProvider,
        distributionServiceConfig);

    outputFolder.create();
    java.io.File outputDir = outputFolder.newFolder();
    Directory<WritableOnDisk> directory = new DirectoryOnDisk(outputDir);
    directory.addWritable(signingDecorator);
    directory.prepare(new ImmutableStack<>());

    File<WritableOnDisk> signatureFile = archive.getWritables().stream()
        .filter(writable -> writable.getName().equals("export.sig"))
        .map(writable -> (File<WritableOnDisk>) writable)
        .findFirst()
        .orElseThrow();

    signatureList = TEKSignatureList.parseFrom(signatureFile.getBytes());
  }

  @Test
  void checkSignatureFileStructure() {

    assertThat(signatureList).isNotNull();

    List<TEKSignature> signatures = signatureList.getSignaturesList();
    assertThat(signatures).hasSize(1);

    TEKSignature signature = signatures.get(0);
    assertThat(signature.getSignatureInfo()).isNotNull();
    assertThat(signature.getBatchNum()).isEqualTo(1);
    assertThat(signature.getBatchSize()).isEqualTo(1);
    assertThat(signature.getSignature()).isNotEmpty();
  }

  @Test
  void checkSignatureInfo() {
    SignatureInfo signatureInfo = signatureList.getSignaturesList().get(0).getSignatureInfo();

    assertThat(signatureInfo.getAppBundleId()).isEqualTo("de.rki.coronawarnapp");
    assertThat(signatureInfo.getAndroidPackage()).isEqualTo("de.rki.coronawarnapp");
    assertThat(signatureInfo.getSignatureAlgorithm()).isEqualTo("1.2.840.10045.4.3.2");
    assertThat(signatureInfo.getVerificationKeyId()).isEqualTo("262");
    assertThat(signatureInfo.getVerificationKeyVersion()).isEqualTo("v1");
  }

  @Test
  void checkSignature()
      throws NoSuchProviderException, NoSuchAlgorithmException, SignatureException, IOException, InvalidKeyException, CertificateException {
    byte[] fileBytes = fileToSign.getBytes();
    byte[] signatureBytes = signatureList.getSignaturesList().get(0).getSignature().toByteArray();

    Resource certResource = resourceLoader.getResource("classpath:keys/certificate.crt");
    try (InputStream certStream = certResource.getInputStream()) {
      byte[] bytes = certStream.readAllBytes();
      CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
      InputStream certificateByteStream = new ByteArrayInputStream(bytes);
      Certificate certificate = certificateFactory.generateCertificate(certificateByteStream);

      Signature payloadSignature = Signature.getInstance("SHA256withECDSA", "BC");
      payloadSignature.initVerify(certificate);
      payloadSignature.update(fileBytes);
      assertThat(payloadSignature.verify(signatureBytes)).isTrue();
    }
  }

  private static class TestSigningDecorator extends SigningDecoratorOnDisk {

    public TestSigningDecorator(Archive<WritableOnDisk> archive, CryptoProvider cryptoProvider,
        DistributionServiceConfig distributionServiceConfig) {
      super(archive, cryptoProvider, distributionServiceConfig);
    }

    @Override
    public byte[] getBytesToSign() {
      return this.getWritables().stream()
          .filter(writable -> writable.getName().equals("export.bin"))
          .map(writable -> (File<WritableOnDisk>) writable)
          .map(File::getBytes)
          .findFirst()
          .orElseThrow();
    }

    @Override
    public int getBatchNum() {
      return 1;
    }

    @Override
    public int getBatchSize() {
      return 1;
    }
  }
}
