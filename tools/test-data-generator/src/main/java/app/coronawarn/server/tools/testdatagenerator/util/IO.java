package app.coronawarn.server.tools.testdatagenerator.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

public class IO {

  public static PrivateKey getPrivateKeyFromFile(File privateKeyPath) throws IOException {
    PEMParser pemParser = new PEMParser(Files.newBufferedReader(privateKeyPath.toPath(),
        StandardCharsets.UTF_8));
    PrivateKeyInfo privateKeyInfo = (PrivateKeyInfo) pemParser.readObject();
    return new JcaPEMKeyConverter().getPrivateKey(privateKeyInfo);
  }

  public static Certificate getCertificateFromFile(File certificatePath) throws IOException,
      CertificateException {
    return getCertificateFromBytes(getBytesFromFile(certificatePath));
  }

  public static Certificate getCertificateFromBytes(byte[] bytes) throws CertificateException {
    CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
    InputStream certificateByteStream = new ByteArrayInputStream(bytes);
    return certificateFactory.generateCertificate(certificateByteStream);
  }

  public static byte[] getBytesFromFile(File file) throws IOException {
    return Files.readAllBytes(file.toPath());
  }

  public static void makeFile(File root, String name) {
    File directory = new File(root.getPath() + "/" + name);
    try {
      directory.createNewFile();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void writeBytesToFile(byte[] bytes, File outputFile) {
    try {
      outputFile.createNewFile();
      FileOutputStream outputFileStream = new FileOutputStream(outputFile);
      outputFileStream.write(bytes);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
