package app.coronawarn.server.services.distribution.crypto;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class CryptoProviderTest {

  @Test
  public void constructorInitializesCryptoArtifacts() throws Exception {
    var cryptoProvider = new CryptoProvider();

    assertNotNull(cryptoProvider.getPrivateKey());
    assertNotNull(cryptoProvider.getCertificate());
  }
}
