package app.coronawarn.server.services.distribution.dcc;

import app.coronawarn.server.common.persistence.domain.DccRevocationEntry;
import app.coronawarn.server.services.distribution.dgc.Certificates;
import org.springframework.core.io.ResourceLoader;

import java.util.List;
import java.util.Optional;

import static app.coronawarn.server.common.shared.util.SerializationUtils.readConfiguredJsonOrDefault;

public class TestDccRevocationClient implements DccRevocationClient {

  private final ResourceLoader resourceLoader;

  public TestDccRevocationClient(ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

  @Override
  public Optional<List<DccRevocationEntry>> getDccRevocationList() throws FetchDccListException {
      return readConfiguredJsonOrDefault(resourceLoader, null, "trustList/ubirchDSC.json", Certificates.class);
    }
  }
}
