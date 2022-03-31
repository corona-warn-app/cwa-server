package app.coronawarn.server.services.distribution.dcc;

import app.coronawarn.server.services.distribution.dgc.Certificates;
import java.util.Optional;

public class TestDccRevocationClient implements DccRevocationClient {

  @Override
  public Optional<Certificates> getDccRevocationList() throws FetchDccListException {
    return Optional.empty();
  }
}
