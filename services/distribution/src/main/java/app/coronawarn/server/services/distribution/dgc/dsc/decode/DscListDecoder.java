package app.coronawarn.server.services.distribution.dgc.dsc.decode;

import static app.coronawarn.server.common.shared.util.SecurityUtils.ecdsaSignatureVerification;
import static app.coronawarn.server.common.shared.util.SecurityUtils.getEcdsaEncodeFromSignature;
import static app.coronawarn.server.common.shared.util.SecurityUtils.getPublicKeyFromString;

import app.coronawarn.server.common.shared.util.SerializationUtils;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.dgc.Certificates;
import app.coronawarn.server.services.distribution.dgc.exception.DscListDecodeException;
import java.security.PublicKey;
import java.util.Base64;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!fake-dsc-client")
public class DscListDecoder {

  public static final String CONTENT_STARTS_CHAR = "{";
  private final DistributionServiceConfig distributionServiceConfig;

  public DscListDecoder(DistributionServiceConfig distributionServiceConfig) {
    this.distributionServiceConfig = distributionServiceConfig;
  }

  /**
   * TODO: add javadoc.
   */
  public Certificates decode(String data) throws DscListDecodeException {
    try {
      PublicKey publicKey = getPublicKeyFromString(
          distributionServiceConfig.getDigitalGreenCertificate().getDscClient().getPublicKey());

      String signature = data.substring(0, data.indexOf(CONTENT_STARTS_CHAR)).trim();
      String content = data.substring(signature.length()).trim();

      byte[] base64DecodedSignature = Base64.getDecoder().decode(signature);
      byte[] ecdsaSignature = getEcdsaEncodeFromSignature(base64DecodedSignature);

      ecdsaSignatureVerification(ecdsaSignature, publicKey, content);

      return SerializationUtils.deserializeJson(content,
          typeFactory -> typeFactory.constructType(Certificates.class));
    } catch (Exception e) {
      throw new DscListDecodeException("Dsc list cannot be decoded because of "
          + "signature verification failinig caused by: ", e);
    }
  }

}
