

package app.coronawarn.server.services.eventregistration.service;

import app.coronawarn.server.services.eventregistration.config.EventRegistrationConfiguration;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.security.PrivateKey;
import java.security.Security;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
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
  CryptoProvider(ResourceLoader resourceLoader, EventRegistrationConfiguration eventRegistrationConfiguration) {
    privateKey = loadPrivateKey(resourceLoader, eventRegistrationConfiguration);
    Security.addProvider(new BouncyCastleProvider());
  }

  /**
   * Returns the {@link PrivateKey} configured in the application properties.
   *
   * @return private key
   */
  public PrivateKey getPrivateKey() {
    return privateKey;
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


  private PrivateKey loadPrivateKey(ResourceLoader resourceLoader,
      EventRegistrationConfiguration eventRegistrationConfiguration) {
    String path = eventRegistrationConfiguration.getPrivateKey();
    Resource privateKeyResource = resourceLoader.getResource(path);
    try (InputStream privateKeyStream = privateKeyResource.getInputStream()) {
      return getPrivateKeyFromStream(privateKeyStream);
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to load private key from " + path, e);
    }
  }
}
