

package app.coronawarn.server.services.distribution.dgc.dsc;

import app.coronawarn.server.services.distribution.dgc.Certificates;
import app.coronawarn.server.services.distribution.dgc.exception.FetchDscTrustListException;
import java.util.Optional;

/**
 * This is a wrapper interface retrieving Digital Signign Certificates data.
 * Used to make HTTP request to Digital Signign Certificates server.
 * Used to retrieve mock sample data from classpath.
 */
public interface DigitalSigningCertificatesClient {

  Optional<Certificates> getDscTrustList() throws FetchDscTrustListException;
}
