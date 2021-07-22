package app.coronawarn.server.services.distribution.dgc.dsc.decode;

import static app.coronawarn.server.common.shared.util.SecurityUtils.base64decode;
import static app.coronawarn.server.common.shared.util.SecurityUtils.ecdsaSignatureVerification;
import static app.coronawarn.server.common.shared.util.SecurityUtils.getEcdsaEncodeFromSignature;
import static app.coronawarn.server.common.shared.util.SecurityUtils.getPublicKeyFromString;

import app.coronawarn.server.common.shared.util.SerializationUtils;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.dgc.CertificateStructure;
import app.coronawarn.server.services.distribution.dgc.Certificates;
import app.coronawarn.server.services.distribution.dgc.exception.DscListDecodeException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!fake-dsc-client")
public class DscListDecoder {

  private static final Logger logger = LoggerFactory.getLogger(DscListDecoder.class);

  public static final String CONTENT_STARTS_CHAR = "{";
  private final DistributionServiceConfig distributionServiceConfig;

  public DscListDecoder(DistributionServiceConfig distributionServiceConfig) {
    this.distributionServiceConfig = distributionServiceConfig;
  }

  /**
   * Decode the trust list of certificates. Verifies the trust list content by using the ECDSA signature logic. Filters
   * only X509 valid format certificates from the response.
   *
   * @param data - trust list reponse from DSC as string.
   * @return - object wrapping the list of certificates.
   * @throws DscListDecodeException - thrown if any exception is catched and special treatment if signature verification
   *                                fails.
   */
  public Certificates decode(String data) throws DscListDecodeException {
    try {
      PublicKey publicKey = getPublicKeyFromString(
          distributionServiceConfig.getDigitalGreenCertificate().getDscClient().getPublicKey());

      String signature = data.substring(0, data.indexOf(CONTENT_STARTS_CHAR)).trim();
      String content = data.substring(signature.length()).trim();

      byte[] base64DecodedSignature = base64decode(signature);
      byte[] ecdsaSignature = getEcdsaEncodeFromSignature(base64DecodedSignature);

      ecdsaSignatureVerification(ecdsaSignature, publicKey, content);

      Certificates certificates = SerializationUtils.deserializeJson(content,
          typeFactory -> typeFactory.constructType(Certificates.class));
      return filterValidCertificates(certificates);

    } catch (SignatureException e) {
      throw new DscListDecodeException("Dsc list cannot be decoded because of " + "signature verification failinig: ",
          e);
    } catch (Exception e) {
      throw new DscListDecodeException("Dsc list cannot be decoded because of: ", e);
    }
  }

  /**
   * Filters out from the Certificates object wrapper, the invalid X509 format certificates.
   */
  private Certificates filterValidCertificates(Certificates certificates) {
    List<CertificateStructure> validCertificates = new ArrayList<>();

    for (CertificateStructure certificate : certificates.getCertificates()) {
      InputStream certificateStream = new ByteArrayInputStream(base64decode(certificate.getRawData()));
      try {
        CertificateFactory.getInstance("X.509").generateCertificate(certificateStream);
        validCertificates.add(certificate);
      } catch (CertificateException e) {
        logger.error("Certificate having kid " + certificate.getKid() + " has failed X509 certificate validation. "
            + "It will be skipped.", e);
      }
    }
    certificates.setCertificates(validCertificates);

    return certificates;
  }

}
