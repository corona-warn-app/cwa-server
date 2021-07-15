
package app.coronawarn.server.services.distribution.dgc.dsc;

import app.coronawarn.server.services.distribution.dgc.exception.FetchDscTrustListException;
import java.util.List;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * This is an implementation with test data for interface retrieving Digital Covid Certificate data. Used to make HTTP
 * request to Digital Signign Certificates server.
 */
@Component
@Profile("!fake-dsc-client")
public class ProdDigitalSigningCertificatesClient implements DigitalSigningCertificatesClient {

  private static final Logger logger = LoggerFactory.getLogger(ProdDigitalSigningCertificatesClient.class);

  private final DigitalSigningCertificatesFeignClient digitalSigningCertificatesFeignClient;

  public ProdDigitalSigningCertificatesClient(
      DigitalSigningCertificatesFeignClient digitalCovidCertificateFeignClient) {
    this.digitalSigningCertificatesFeignClient = digitalCovidCertificateFeignClient;
  }

  @Override
  public List<JSONObject> getDscTrustList() throws FetchDscTrustListException {
    logger.debug("Get rules from DCC");
    try {
      return digitalSigningCertificatesFeignClient.getDscTrustList().getBody();
    } catch (Exception e) {
      throw new FetchDscTrustListException("DSC Trust List could not be fetched because of: ", e);
    }

  }
}
