package app.coronawarn.server.services.distribution.assembly.structure.file.decorators;

import static app.coronawarn.server.services.distribution.common.Helpers.prepareAndWrite;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.coronawarn.server.common.protocols.internal.SignedPayload;
import app.coronawarn.server.services.distribution.assembly.component.CryptoProvider;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryImpl;
import app.coronawarn.server.services.distribution.assembly.structure.file.File;
import app.coronawarn.server.services.distribution.assembly.structure.file.FileImpl;
import app.coronawarn.server.services.distribution.assembly.structure.file.decorator.SigningDecorator;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.rules.TemporaryFolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CryptoProvider.class},
    initializers = ConfigFileApplicationContextInitializer.class)
public class SigningDecoratorTest {

  @Rule
  private TemporaryFolder outputFolder = new TemporaryFolder();

  @Autowired
  CryptoProvider cryptoProvider;

  private static final byte[] bytes = "foo".getBytes();
  private Directory parent;
  private File decoree;
  private File decorator;

  @BeforeEach
  public void setup() throws IOException {
    outputFolder.create();
    parent = new DirectoryImpl(outputFolder.newFolder());
    decoree = new FileImpl("bar", bytes);
    decorator = new SigningDecorator(decoree, cryptoProvider);

    parent.addFile(decorator);

    prepareAndWrite(parent);
  }

  @Test
  public void checkCertificate() throws IOException, CertificateEncodingException {
    byte[] writtenBytes = Files.readAllBytes(decoree.getFileOnDisk().toPath());
    SignedPayload signedPayload = SignedPayload.parseFrom(writtenBytes);

    assertArrayEquals(cryptoProvider.getCertificate().getEncoded(),
        signedPayload.getCertificateChain().toByteArray());
  }

  @Test
  public void checkSignature()
      throws IOException, CertificateException, NoSuchProviderException, NoSuchAlgorithmException,
      InvalidKeyException, SignatureException {
    byte[] writtenBytes = Files.readAllBytes(decoree.getFileOnDisk().toPath());
    SignedPayload signedPayload = SignedPayload.parseFrom(writtenBytes);

    InputStream certificateByteStream = new ByteArrayInputStream(
        signedPayload.getCertificateChain().toByteArray());
    CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
    Certificate certificate = certificateFactory.generateCertificate(certificateByteStream);

    Signature signature = Signature.getInstance("Ed25519", "BC");
    signature.initVerify(certificate);
    signature.update(signedPayload.getPayload().toByteArray());

    assertTrue(signature.verify(signedPayload.getSignature().toByteArray()));
  }
}
