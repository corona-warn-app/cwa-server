package app.coronawarn.server.services.distribution.dcc;

import app.coronawarn.server.common.persistence.domain.DccRevocationEntry;
import java.util.List;
import java.util.Optional;

public interface DccRevocationClient {

  Optional<List<DccRevocationEntry>> getDccRevocationList() throws FetchDccListException;
}
