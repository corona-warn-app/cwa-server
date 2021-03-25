package app.coronawarn.server.services.submission.helpers;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import app.coronawarn.server.services.submission.config.CryptoProvider;

public class CryptoSignUtils {

  private static final String TEST_PRIVATE_KEY_PATH = "/crypto/testprivatekey.pem";

  public static byte[] sign(byte[] contentBytes, CryptoProvider cryptoProvider) {
    try {
      Signature signatureAlgorithm = Signature.getInstance(cryptoProvider.getSignatureAlgorithm());
      signatureAlgorithm.initSign(loadPrivateKey());
      signatureAlgorithm.update(contentBytes);
      return signatureAlgorithm.sign();
    } catch (InvalidKeyException | NoSuchAlgorithmException e) {
    } catch (SignatureException e) {
    }
    return null;
  }

  private static PrivateKey loadPrivateKey() {
    URL privateKeyResource = CryptoSignUtils.class.getResource(TEST_PRIVATE_KEY_PATH);
    try (InputStream privateKeyStream = new FileInputStream(privateKeyResource.getFile())) {
      return getPrivateKeyFromStream(privateKeyStream);
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to load private key from " + TEST_PRIVATE_KEY_PATH, e);
    }
  }

  private static PrivateKey getPrivateKeyFromStream(InputStream privateKeyStream)
      throws IOException {
    PEMParser pemParser = new PEMParser(new InputStreamReader(privateKeyStream));
    JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
    var parsedObject = pemParser.readObject();
    if (parsedObject instanceof PEMKeyPair) {
      return converter.getPrivateKey(((PEMKeyPair) parsedObject).getPrivateKeyInfo());
    } else {
      PrivateKeyInfo privateKeyInfo = PrivateKeyInfo.getInstance(parsedObject);
      return converter.getPrivateKey(privateKeyInfo);
    }
  }
}
