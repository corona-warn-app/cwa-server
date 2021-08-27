package app.coronawarn.server.services.distribution.assembly.component;


import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.archive.ArchiveOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.archive.decorator.signing.DistributionArchiveSigningDecorator;
import app.coronawarn.server.services.distribution.assembly.structure.file.FileOnDisk;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.dgc.BusinessRule.RuleType;
import app.coronawarn.server.services.distribution.dgc.DigitalGreenCertificateToCborMapping;
import app.coronawarn.server.services.distribution.dgc.DigitalGreenCertificateToProtobufMapping;
import app.coronawarn.server.services.distribution.dgc.dsc.DigitalSigningCertificatesToProtobufMapping;
import app.coronawarn.server.services.distribution.dgc.exception.DigitalCovidCertificateException;
import app.coronawarn.server.services.distribution.dgc.exception.FetchBusinessRulesException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Reads Booster notification rules from the respective files in the class path or from dcc client and build a {@link
 * BoosterNotificationStructureProvider} with them.
 */

@Component
public class BoosterNotificationStructureProvider {

  private static final Logger logger = LoggerFactory.getLogger(DigitalCertificatesStructureProvider.class);

  public static final String EXPORT_BIN = "export.bin";

  private final DistributionServiceConfig distributionServiceConfig;
  private final CryptoProvider cryptoProvider;
  private final DigitalGreenCertificateToCborMapping dgcToCborMapping;
  private final DigitalSigningCertificatesToProtobufMapping digitalSigningCertificatesToProtobufMapping;

  /**
   * Create an instance.
   */
  public BoosterNotificationStructureProvider(DistributionServiceConfig distributionServiceConfig,
      CryptoProvider cryptoProvider, DigitalGreenCertificateToProtobufMapping dgcToProtobufMapping,
      DigitalGreenCertificateToCborMapping dgcToCborMapping,
      DigitalSigningCertificatesToProtobufMapping digitalSigningCertificatesToProtobufMapping) {
    this.distributionServiceConfig = distributionServiceConfig;
    this.cryptoProvider = cryptoProvider;
    this.dgcToCborMapping = dgcToCborMapping;
    this.digitalSigningCertificatesToProtobufMapping = digitalSigningCertificatesToProtobufMapping;
  }

  /**
   * Returns the publishable archive with Booster Notification Business rules Cbor encoded structures.
   */
  public Writable<WritableOnDisk> getBoosterNotificationRules() {
    return getRulesArchive(RuleType.BoosterNotification,
        distributionServiceConfig.getDigitalGreenCertificate().getBoosterNotification()).get();
  }

  /**
   * Create business rules Archive. If any exception is thrown during fetching data and packaging process, an empty
   * Archive will be published in order to not override any previous archive on CDN with broken data. Provided rules are
   * filtered by rule type parameter which could be 'Acceptance', 'Invalidation' or 'BoosterNotification'.
   *
   * @param ruleType    - rule type to receive rules for
   * @param archiveName - archive name for packaging rules
   * @return - business rules archive
   */
  private Optional<Writable<WritableOnDisk>> getRulesArchive(RuleType ruleType, String archiveName) {
    ArchiveOnDisk rulesArchive = new ArchiveOnDisk(archiveName);

    try {
      rulesArchive
          .addWritable(new FileOnDisk("export.bin", dgcToCborMapping.constructCborRules(ruleType)));
      logger.info(archiveName + " archive has been added to the DGC distribution folder");
      return Optional.of(new DistributionArchiveSigningDecorator(rulesArchive, cryptoProvider,
          distributionServiceConfig));
    } catch (DigitalCovidCertificateException e) {
      logger.error(archiveName + " archive was not overwritten because of:", e);
    } catch (FetchBusinessRulesException e) {
      logger.error(archiveName + " archive was not overwritten because business rules could not been fetched:", e);
    }

    return Optional.empty();
  }
}
