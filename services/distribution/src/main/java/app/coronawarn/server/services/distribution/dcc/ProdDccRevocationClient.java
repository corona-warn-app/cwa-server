package app.coronawarn.server.services.distribution.dcc;

import app.coronawarn.server.services.distribution.dgc.Certificates;
import app.coronawarn.server.services.distribution.dgc.client.ProdDigitalCovidCertificateClient;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class ProdDccRevocationClient implements DccRevocationClient {

  private static final Logger logger = LoggerFactory.getLogger(ProdDigitalCovidCertificateClient.class);

  public static final Marker AUDIT = MarkerFactory.getMarker("AUDIT");

  private final DccRevocationFeignClient digitalCovidCertificateClient;

  public ProdDccRevocationClient(DccRevocationFeignClient digitalCovidCertificateClient) {
    this.digitalCovidCertificateClient = digitalCovidCertificateClient;
  }

  @Override
  public Optional<Certificates> getDccRevocationList() throws FetchDccListException {
    return Optional.empty();
  }
}
