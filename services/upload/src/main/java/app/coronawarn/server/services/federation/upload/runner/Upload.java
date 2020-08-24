package app.coronawarn.server.services.federation.upload.runner;

import app.coronawarn.server.services.federation.upload.signing.CryptoProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class Upload implements ApplicationRunner {

  private final CryptoProvider cryptoProvider;

  private static final Logger logger = LoggerFactory
      .getLogger(Upload.class);

  public Upload(CryptoProvider cryptoProvider) {
    this.cryptoProvider = cryptoProvider;
  }

  @Override
  public void run(ApplicationArguments args) throws Exception {
    logger.info("Running Upload Job");
    var key = this.cryptoProvider.getPrivateKey();
    logger.info(key.getFormat());
  }
}
