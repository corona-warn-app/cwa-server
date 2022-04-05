package app.coronawarn.server.services.distribution.dgc.dsc;

import app.coronawarn.server.common.persistence.domain.DccRevocationEntry;
import app.coronawarn.server.services.distribution.dcc.FetchDccListException;
import app.coronawarn.server.services.distribution.dgc.exception.FetchDscTrustListException;
import org.json.simple.parser.ParseException;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * This is a wrapper interface retrieving Digital Signing Certificates data.
 * Used to make HTTP request to Digital Signing Certificates server.
 * Used to retrieve mock sample data from classpath.
 */
public interface DigitalSigningCertificatesClient {

  Optional<List<DccRevocationEntry>> getDscTrustList()
      throws FetchDscTrustListException, IOException, ParseException, FetchDccListException;
}
