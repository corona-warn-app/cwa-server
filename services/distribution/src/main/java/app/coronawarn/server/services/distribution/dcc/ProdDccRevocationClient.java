package app.coronawarn.server.services.distribution.dcc;

import app.coronawarn.server.common.persistence.domain.DccRevocationEntry;
import app.coronawarn.server.services.distribution.dcc.decode.DccRevocationListDecoder;
import app.coronawarn.server.services.distribution.dgc.client.ProdDigitalCovidCertificateClient;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!fake-dcc-revocation")
public class ProdDccRevocationClient implements DccRevocationClient {

  private static final Logger logger = LoggerFactory.getLogger(ProdDigitalCovidCertificateClient.class);

  public static final Marker AUDIT = MarkerFactory.getMarker("AUDIT");

  private final DccRevocationFeignClient dccRevocationFeignClient;
  private final DccRevocationListDecoder dccRevocationListDecoder;

  public ProdDccRevocationClient(DccRevocationFeignClient dccRevocationFeignClient,
      DccRevocationListDecoder dccRevocationListDecoder) {
    this.dccRevocationFeignClient = dccRevocationFeignClient;
    this.dccRevocationListDecoder = dccRevocationListDecoder;
  }

  @Override
  public Optional<List<DccRevocationEntry>> getDccRevocationList() throws FetchDccListException {
    logger.debug("Get Revocation List from DCC");
    try {
      return Optional.of(dccRevocationListDecoder.decode(dccRevocationFeignClient.getRevocationList().getBody()));
    } catch (Exception e) {
      throw new FetchDccListException("DCC Revocation List could not be fetched because of: ", e);
    }
  }
}
