package app.coronawarn.server.services.distribution.dcc;

import app.coronawarn.server.common.persistence.domain.RevocationEntry;
import java.util.List;
import java.util.Optional;

public interface DccRevocationClient {

  Optional<List<RevocationEntry>> getDccRevocationList() throws FetchDccListException;

  String getETag() throws FetchDccListException;
}
