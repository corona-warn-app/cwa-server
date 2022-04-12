package app.coronawarn.server.services.distribution.assembly.component;

import static app.coronawarn.server.services.distribution.assembly.component.DigitalCertificatesStructureProvider.EXPORT_BIN;

import app.coronawarn.server.common.persistence.domain.RevocationEntry;
import app.coronawarn.server.common.persistence.service.DccRevocationListService;
import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.archive.ArchiveOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.archive.decorator.signing.DistributionArchiveSigningDecorator;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.file.FileOnDisk;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.dcc.DccRevocationClient;
import app.coronawarn.server.services.distribution.dcc.DccRevocationListToProtobufMapping;
import app.coronawarn.server.services.distribution.dcc.FetchDccListException;
import app.coronawarn.server.services.distribution.dcc.structure.DccRevocationDirectory;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DccRevocationListStructureProvider {

  private static final Logger logger = LoggerFactory.getLogger(DccRevocationListStructureProvider.class);
  private static final String KID_ARCHIVE = "kid";

  private final CryptoProvider cryptoProvider;
  private final DistributionServiceConfig distributionServiceConfig;
  private final DccRevocationClient dccRevocationClient;
  private final DccRevocationListService dccRevocationListService;
  private final DccRevocationListToProtobufMapping dccRevocationToProtobufMapping;

  /**
   * Creates the CDN structure for DCC Revocation list.
   */
  public DccRevocationListStructureProvider(CryptoProvider cryptoProvider,
      DistributionServiceConfig distributionServiceConfig, DccRevocationClient dccRevocationClient,
      DccRevocationListService dccRevocationListService,
      DccRevocationListToProtobufMapping dccRevocationToProtobufMapping) {
    this.cryptoProvider = cryptoProvider;
    this.distributionServiceConfig = distributionServiceConfig;
    this.dccRevocationClient = dccRevocationClient;
    this.dccRevocationListService = dccRevocationListService;
    this.dccRevocationToProtobufMapping = dccRevocationToProtobufMapping;
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

  public DirectoryOnDisk getDccRevocationDirectory() {
    return constructArchiveToPublish(cryptoProvider);
  }

  private DccRevocationDirectory constructArchiveToPublish(CryptoProvider cryptoProvider) {
    DccRevocationDirectory dccRlDirectory = new DccRevocationDirectory(distributionServiceConfig, cryptoProvider);
    getDccRevocationKidListArchive().ifPresent(dccRlDirectory::addWritable);
    return dccRlDirectory;
  }

  private Optional<Writable<WritableOnDisk>> getDccRevocationKidListArchive() {
    ArchiveOnDisk kidArchive = new ArchiveOnDisk(KID_ARCHIVE);
    Map<Integer, List<RevocationEntry>> revocationEntriesByKidAndHash =
        dccRevocationListService.getRevocationListEntries()
            .stream().collect(Collectors.groupingBy(RevocationEntry::getKidHash));
    try {
      kidArchive
          .addWritable(new FileOnDisk(EXPORT_BIN,
              dccRevocationToProtobufMapping.constructProtobufMapping(revocationEntriesByKidAndHash).toByteArray()));
      logger.info("Kid Revocation list archive has been added to the dcc-rl distribution folder");

      return Optional.of(new DistributionArchiveSigningDecorator(kidArchive, cryptoProvider,
          distributionServiceConfig));
    } catch (Exception e) {
      logger.error("Creating Kid Revocation list archive has failed :", e);
    }

    return Optional.empty();
  }
}
