package app.coronawarn.server.services.distribution.dgc.dsc.decode;

import static app.coronawarn.server.common.shared.util.SecurityUtils.base64decode;
import static app.coronawarn.server.common.shared.util.SecurityUtils.ecdsaSignatureVerification;
import static app.coronawarn.server.common.shared.util.SecurityUtils.getEcdsaEncodeFromSignature;
import static app.coronawarn.server.common.shared.util.SecurityUtils.getPublicKeyFromString;
import static app.coronawarn.server.services.distribution.dgc.client.ProdDigitalCovidCertificateClient.AUDIT;

import app.coronawarn.server.common.shared.util.SerializationUtils;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.dgc.CertificateStructure;
import app.coronawarn.server.services.distribution.dgc.Certificates;
import app.coronawarn.server.services.distribution.dgc.exception.DscListDecodeException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Collection;
import org.bouncycastle.jcajce.provider.asymmetric.x509.CertificateFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!fake-dsc-client")
public class DscListDecoder {

  private static final Logger logger = LoggerFactory.getLogger(DscListDecoder.class);

  public static final char CONTENT_STARTS_CHAR = '{';
  private final DistributionServiceConfig distributionServiceConfig;

  public DscListDecoder(DistributionServiceConfig distributionServiceConfig) {
    this.distributionServiceConfig = distributionServiceConfig;
  }

  /**
   * Decode the trust list of certificates. Verifies the trust list content by using the ECDSA signature logic. Filters
   * only X509 valid format certificates from the response.
   *
   * @param data - trust list response from DSC as string.
   * @return - object wrapping the list of certificates.
   * @throws DscListDecodeException - thrown if any exception is caught and special treatment if signature verification
   *                                fails.
   */
  public Certificates decode(String data) throws DscListDecodeException {
    try {
      PublicKey publicKey = getPublicKeyFromString(
          distributionServiceConfig.getDigitalGreenCertificate().getDscClient().getPublicKey());

      String signature = data.substring(0, data.indexOf(CONTENT_STARTS_CHAR)).trim();
      String content = data.substring(signature.length()).trim();

      byte[] ecdsaSignature = getEcdsaEncodeFromSignature(base64decode(signature));

      ecdsaSignatureVerification(ecdsaSignature, publicKey, content.getBytes(StandardCharsets.UTF_8));
      logger.info(AUDIT, "DSC list - {}", content);
      Certificates certificates = SerializationUtils.deserializeJson(content,
          typeFactory -> typeFactory.constructType(Certificates.class));
      return filterValidCertificates(certificates);

    } catch (SignatureException e) {
      throw new DscListDecodeException("Signature verification failed! DSC list NOT decoded.", e);
    } catch (Exception e) {
      throw new DscListDecodeException("DSC list NOT decoded.", e);
    }
  }

  /**
   * Filters out from the Certificates object wrapper, the invalid X509 format certificates.
   */
  private Certificates filterValidCertificates(Certificates certificates) {
    final Collection<CertificateStructure> validCertificates = new ArrayList<>(certificates.getCertificates().size());

    final CertificateFactory certificateFactory = new CertificateFactory();
    for (CertificateStructure certificate : certificates.getCertificates()) {
      InputStream certificateStream = new ByteArrayInputStream(base64decode(certificate.getRawData()));
      try {
        certificateFactory.engineGenerateCertificate(certificateStream);
        validCertificates.add(certificate);
      } catch (CertificateException e) {
        logger.error("Skipping certificate (kid=" + certificate.getKid() + ") due to validation failure.", e);
      }
    }
    certificates.setCertificates(validCertificates);

    return certificates;
  }
}
