package app.coronawarn.server.services.distribution.assembly.component;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CryptoProvider.class},
    initializers = ConfigFileApplicationContextInitializer.class)
public class CryptoProviderTest {

  @Autowired
  CryptoProvider cryptoProvider;

  @Test
  public void constructorInitializesCryptoArtifacts() {
    assertNotNull(cryptoProvider.getPrivateKey());
    assertNotNull(cryptoProvider.getCertificate());
  }
}
