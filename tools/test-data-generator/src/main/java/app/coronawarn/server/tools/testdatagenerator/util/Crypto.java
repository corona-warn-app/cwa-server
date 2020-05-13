package app.coronawarn.server.tools.testdatagenerator.util;

import java.io.File;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

public class Crypto {

  private final PrivateKey privateKey;
  private final Certificate certificate;

  public Crypto(PrivateKey privateKey, Certificate certificate) {
    this.privateKey = privateKey;
    this.certificate = certificate;
  }

  public Crypto(File privateKeyFile, File certificateFile) throws IOException,
      CertificateException {
    this(IO.getPrivateKeyFromFile(privateKeyFile), IO.getCertificateFromFile(certificateFile));
  }

  public PrivateKey getPrivateKey() {
    return privateKey;
  }

  public Certificate getCertificate() {
    return certificate;
  }
}
