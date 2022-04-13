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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DccRevocationListStructureProvider {

  private static final Logger logger = LoggerFactory.getLogger(DccRevocationListStructureProvider.class);
  private static final String KID_ARCHIVE = "kid";
  public static final String CHUNK = "chunk";

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

  private DirectoryOnDisk constructArchiveToPublish(CryptoProvider cryptoProvider) {

    DirectoryOnDisk dccRlDirectory = new DirectoryOnDisk(
        distributionServiceConfig.getDccRevocation().getDccRevocationDirectory());
    Map<Integer, List<RevocationEntry>> revocationEntriesByKidAndHash =
        dccRevocationListService.getRevocationListEntries()
            .stream().collect(Collectors.groupingBy(RevocationEntry::getKidTypeHash));
    getDccRevocationKidListArchive().ifPresent(dccRlDirectory::addWritable);
    getDccRevocationKidTypeDirectories(revocationEntriesByKidAndHash).forEach(kidTypeDirectory ->
        dccRlDirectory.addWritable(kidTypeDirectory));
    return dccRlDirectory;
  }

  private List<DirectoryOnDisk> getDccRevocationKidTypeDirectories(
      Map<Integer, List<RevocationEntry>> revocationEntriesByKidAndHash) {
    List<DirectoryOnDisk> kidTypeDirectories = new ArrayList<>();
    revocationEntriesByKidAndHash.keySet().forEach(kidType -> {
      DirectoryOnDisk kidTypeDirectory =
          new DirectoryOnDisk(revocationEntriesByKidAndHash.get(kidType).get(0).toString());
      getDccRevocationKidTypeArchive(revocationEntriesByKidAndHash.get(kidType))
          .ifPresent(kidTypeDirectory::addWritable);
      getKidTypeXandYDirectories(revocationEntriesByKidAndHash.get(kidType))
          .forEach(directoryXY -> kidTypeDirectory.addWritable(directoryXY));
      kidTypeDirectories.add(kidTypeDirectory);
    });
    return kidTypeDirectories;
  }

  private List<DirectoryOnDisk> getKidTypeXandYDirectories(List<RevocationEntry> revocationEntryList) {
    List<DirectoryOnDisk> directoryXY = new ArrayList<>();
    Map<Integer, List<RevocationEntry>> revocationEntriesGrouped = revocationEntryList.stream()
        .collect(Collectors.groupingBy(RevocationEntry::getXHash));

    revocationEntriesGrouped.keySet().forEach(xhashRevocationEntry -> {
      DirectoryOnDisk directoryX = new DirectoryOnDisk(
          Hex.toHexString(revocationEntriesGrouped.get(xhashRevocationEntry)
              .get(0).getXhash()));

      getDccRevocationYDirectories(revocationEntriesGrouped.get(xhashRevocationEntry))
          .forEach(yhashDirectory -> directoryX.addWritable(yhashDirectory));
      directoryXY.add(directoryX);
    });
    return directoryXY;
  }

  private List<DirectoryOnDisk> getDccRevocationYDirectories(
      List<RevocationEntry> xrevocationEntryList) {

    Map<Integer, List<RevocationEntry>> revocationEntriesGroupedByHashY = xrevocationEntryList.stream()
        .collect(Collectors.groupingBy(RevocationEntry::getYHash));
    List<DirectoryOnDisk> yhashDirectories = new ArrayList<>();
    revocationEntriesGroupedByHashY.keySet().forEach(yhashEntry -> {
      DirectoryOnDisk directoryHashY = new DirectoryOnDisk(Hex.toHexString(
          revocationEntriesGroupedByHashY.get(yhashEntry).get(0).getYhash()));
      getDccRevocationKidTypeChunk(revocationEntriesGroupedByHashY.get(yhashEntry))
          .ifPresent(directoryHashY::addWritable);
      yhashDirectories.add(directoryHashY);
    });
    return yhashDirectories;
  }

  private Optional<Writable<WritableOnDisk>> getDccRevocationKidTypeChunk(
      List<RevocationEntry> yhashRevocationEntryList) {
    ArchiveOnDisk kidArchive = new ArchiveOnDisk(CHUNK);

    try {
      kidArchive
          .addWritable(new FileOnDisk(EXPORT_BIN,
              dccRevocationToProtobufMapping.constructProtobufMappingChunkList(yhashRevocationEntryList)
                  .toByteArray()));
      logger.info("Kid Revocation list archive has been added to the dcc-rl distribution folder");

      return Optional.of(new DistributionArchiveSigningDecorator(kidArchive, cryptoProvider,
          distributionServiceConfig));
    } catch (Exception e) {
      logger.error("Creating Kid Revocation list archive has failed :", e);
    }

    return Optional.empty();
  }

  private Optional<Writable<WritableOnDisk>> getDccRevocationKidListArchive() {
    ArchiveOnDisk kidArchive = new ArchiveOnDisk(KID_ARCHIVE);
    Map<Integer, List<RevocationEntry>> revocationEntriesByKidAndHash =
        dccRevocationListService.getRevocationListEntries()
            .stream().collect(Collectors.groupingBy(RevocationEntry::getKidHash));
    try {
      kidArchive
          .addWritable(new FileOnDisk(EXPORT_BIN,
              dccRevocationToProtobufMapping.constructProtobufMappingKidList(revocationEntriesByKidAndHash)
                  .toByteArray()));
      logger.info("Kid Revocation list archive has been added to the dcc-rl distribution folder");

      return Optional.of(new DistributionArchiveSigningDecorator(kidArchive, cryptoProvider,
          distributionServiceConfig));
    } catch (Exception e) {
      logger.error("Creating Kid Revocation list archive has failed :", e);
    }

    return Optional.empty();
  }

  private Optional<Writable<WritableOnDisk>> getDccRevocationKidTypeArchive(List<RevocationEntry> revocationEntries) {

    ArchiveOnDisk kidTypeArchive = new ArchiveOnDisk(distributionServiceConfig.getOutputFileName());
    try {
      kidTypeArchive
          .addWritable(new FileOnDisk(EXPORT_BIN,
              dccRevocationToProtobufMapping.constructProtobufMappingKidType(revocationEntries)
                  .toByteArray()));
      logger.info("Kid Type Revocation index archive has been added to the dcc-rl distribution folder");

      return Optional.of(new DistributionArchiveSigningDecorator(kidTypeArchive, cryptoProvider,
          distributionServiceConfig));
    } catch (Exception e) {
      logger.error("Creating Kid Type Revocation index archive has failed :", e);
    }
    return Optional.empty();
  }
}
