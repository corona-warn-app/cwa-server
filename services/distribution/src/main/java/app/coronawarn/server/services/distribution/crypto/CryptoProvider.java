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
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

@Component
public class CryptoProvider {

  private static final String PRIVATE_KEY_PATH = "classpath:certificates/client/private.pem";
  private static final String CERTIFICATE_PATH = "classpath:certificates/chain/certificate.crt";

  private final PrivateKey privateKey;
  private final Certificate certificate;

  public CryptoProvider() throws IOException, CertificateException {
    Security.addProvider(new BouncyCastleProvider());
    this.privateKey = getPrivateKeyFromFile(ResourceUtils.getFile(PRIVATE_KEY_PATH));
    this.certificate = getCertificateFromFile(ResourceUtils.getFile(CERTIFICATE_PATH));
  }

  private static PrivateKey getPrivateKeyFromFile(File privateKeyPath) throws IOException {
    PEMParser pemParser = new PEMParser(Files.newBufferedReader(privateKeyPath.toPath(),
        StandardCharsets.UTF_8));
    PrivateKeyInfo privateKeyInfo = (PrivateKeyInfo) pemParser.readObject();
    return new JcaPEMKeyConverter().getPrivateKey(privateKeyInfo);
  }

  private static Certificate getCertificateFromFile(File certificatePath) throws IOException,
      CertificateException {
    return getCertificateFromBytes(IO.getBytesFromFile(certificatePath));
  }

  private static Certificate getCertificateFromBytes(byte[] bytes) throws CertificateException {
    CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
    InputStream certificateByteStream = new ByteArrayInputStream(bytes);
    return certificateFactory.generateCertificate(certificateByteStream);
  }

  public PrivateKey getPrivateKey() {
    return privateKey;
  }

  public Certificate getCertificate() {
    return certificate;
  }
}
