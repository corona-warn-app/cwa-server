package app.coronawarn.server.services.distribution.dgc.dsc.decode;

import app.coronawarn.server.common.shared.util.SerializationUtils;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.dgc.Certificates;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!fake-dsc-client")
public class DscListDecoder {

  @Autowired
  private DistributionServiceConfig distributionServiceConfig;

  /**
   * TODO: add javadoc.
   */
  public Certificates decode(String data) {
    String encodedSignature = data.substring(0, data.indexOf("{")).trim();
    byte[] signature = Base64.getDecoder().decode(encodedSignature);
    String trustedList = data.substring(encodedSignature.length()).trim();

    /*    validateSignature(
            distributionServiceConfig.getDigitalGreenCertificate().getDscClient().getPublicKey(),
            trustedList,
            signature,
            "SHA256withECDSA"
        );*/

    return SerializationUtils.deserializeJson(trustedList,
        typeFactory -> typeFactory.constructType(Certificates.class));

  }

  private void validateSignature(String key, byte[] data, byte[] signature, String algorithm) {

  }
}
