package app.coronawarn.server.services.federation.upload.payload.signing;

import app.coronawarn.server.services.federation.upload.config.UploadServiceConfig;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
public class CryptoProvider {

  private final PrivateKey privateKey;

  /**
   * Creates a CryptoProvider, using {@link BouncyCastleProvider}.
   */
  CryptoProvider(ResourceLoader resourceLoader, UploadServiceConfig uploadServiceConfig) {
    privateKey = loadPrivateKey(resourceLoader, uploadServiceConfig);
    Security.addProvider(new BouncyCastleProvider());
  }

  private static PrivateKey getPrivateKeyFromStream(InputStream privateKeyStream) throws IOException {
    InputStreamReader privateKeyStreamReader = new InputStreamReader(privateKeyStream);
    Object parsed = new PEMParser(privateKeyStreamReader).readObject();
    KeyPair pair = new JcaPEMKeyConverter().getKeyPair((PEMKeyPair) parsed);
    return pair.getPrivate();
  }

  /**
   * Returns the {@link PrivateKey} configured in the application properties.
   */
  public PrivateKey getPrivateKey() {
    return privateKey;
  }

  private PrivateKey loadPrivateKey(ResourceLoader resourceLoader,
      UploadServiceConfig uploadServiceConfig) {
    String path = uploadServiceConfig.getPrivateKey();
    Resource privateKeyResource = resourceLoader.getResource(path);
    try (InputStream privateKeyStream = privateKeyResource.getInputStream()) {
      return getPrivateKeyFromStream(privateKeyStream);
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to load private key from " + path, e);
    }
  }
}
