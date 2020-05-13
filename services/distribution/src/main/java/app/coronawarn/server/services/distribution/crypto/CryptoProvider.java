package app.coronawarn.server.services.distribution.crypto;

import app.coronawarn.server.services.distribution.io.IO;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

@Component
public class CryptoProvider {

  @Value("${app.coronawarn.server.services.distribution.paths.privatekey}")
  private String privateKeyPath;

  @Value("${app.coronawarn.server.services.distribution.paths.certificate}")
  private String certificatePath;

  private PrivateKey privateKey;
  private Certificate certificate;

  public CryptoProvider() {
    Security.addProvider(new BouncyCastleProvider());
  }

  private static PrivateKey getPrivateKeyFromFile(File privateKeyFile) throws IOException {
    PEMParser pemParser = new PEMParser(Files.newBufferedReader(privateKeyFile.toPath(),
        StandardCharsets.UTF_8));
    PrivateKeyInfo privateKeyInfo = (PrivateKeyInfo) pemParser.readObject();
    return new JcaPEMKeyConverter().getPrivateKey(privateKeyInfo);
  }

  private static Certificate getCertificateFromFile(File certificateFile) throws IOException,
      CertificateException {
    return getCertificateFromBytes(IO.getBytesFromFile(certificateFile));
  }

  private static Certificate getCertificateFromBytes(byte[] bytes) throws CertificateException {
    CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
    InputStream certificateByteStream = new ByteArrayInputStream(bytes);
    return certificateFactory.generateCertificate(certificateByteStream);
  }

  public PrivateKey getPrivateKey() {
    if (this.privateKey == null) {
      try {
        this.privateKey = getPrivateKeyFromFile(ResourceUtils.getFile(privateKeyPath));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return privateKey;
  }

  public Certificate getCertificate() {
    if (this.certificate == null) {
      try {
        this.certificate = getCertificateFromFile(ResourceUtils.getFile(certificatePath));
      } catch (IOException | CertificateException e) {
        throw new RuntimeException(e);
      }
    }
    return certificate;
  }
}
