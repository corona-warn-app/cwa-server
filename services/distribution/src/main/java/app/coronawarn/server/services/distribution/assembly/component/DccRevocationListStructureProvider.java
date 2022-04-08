package app.coronawarn.server.services.distribution.assembly.component;

import app.coronawarn.server.common.persistence.domain.RevocationEntry;
import app.coronawarn.server.common.persistence.service.DccRevocationListService;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.dcc.DccRevocationClient;
import app.coronawarn.server.services.distribution.dcc.FetchDccListException;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("revocation")
public class DccRevocationListStructureProvider {

  private static final Logger logger = LoggerFactory.getLogger(DccRevocationListStructureProvider.class);

  private final CryptoProvider cryptoProvider;
  private final DistributionServiceConfig distributionServiceConfig;
  private final DccRevocationClient dccRevocationClient;
  private final DccRevocationListService dccRevocationListService;

  /**
   * Creates the CDN structure for DCC Revocation list.
   */
  public DccRevocationListStructureProvider(CryptoProvider cryptoProvider,
      DistributionServiceConfig distributionServiceConfig, DccRevocationClient dccRevocationClient,
      DccRevocationListService dccRevocationListService) {
    this.cryptoProvider = cryptoProvider;
    this.distributionServiceConfig = distributionServiceConfig;
    this.dccRevocationClient = dccRevocationClient;
    this.dccRevocationListService = dccRevocationListService;
  }

  /**
   * Fetch DCC Revocation List.
   */
  public void fetchDccRevocationList() {
    try {
      Optional<List<RevocationEntry>> revocationEntryList = dccRevocationClient.getDccRevocationList();
      revocationEntryList.ifPresent(revocationList -> dccRevocationListService.store(revocationList));
    } catch (FetchDccListException e) {
      logger.warn("Fetching DCC Revocation List failed. ", e);
    } catch (Exception e) {
      logger.warn("Storing DCC Revocation List failed. ", e);
    }
  }

  /*  public DirectoryOnDisk getDccRevocationListDirectory() {
    return constructArchiveToPublish(distributionServiceConfig.getDccRevocation());
  }

  private DccRevocationDirectory constructArchiveToPublish(DccRevocation dccRevocationConfig) {
    DccRevocationDirectory dgcDirectory = new DccRevocationDirectory(
        distributionServiceConfig, dccRevocationConfig, cryptoProvider);

    return dgcDirectory;
  }*/
}
