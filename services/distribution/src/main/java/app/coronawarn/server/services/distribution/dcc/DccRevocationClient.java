package app.coronawarn.server.services.distribution.dcc;

import app.coronawarn.server.services.distribution.dgc.Certificates;
import java.util.Optional;

public interface DccRevocationClient {
  Optional<Certificates> getDccRevocationList() throws FetchDccListException;
}
