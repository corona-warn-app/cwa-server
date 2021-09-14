package app.coronawarn.server.services.distribution.assembly.component;


import static app.coronawarn.server.services.distribution.dgc.BusinessRule.RuleType.BoosterNotification;

import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.dgc.DigitalGreenCertificateToCborMapping;
import app.coronawarn.server.services.distribution.dgc.client.DigitalCovidCertificateClient;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Reads Booster notification rules from the respective files in the class path or from dcc client and build a {@link
 * BoosterNotificationStructureProvider} with them.
 */

@Component
public class BoosterNotificationStructureProvider {

  public static final String EXPORT_BINARY_FILENAME = "export.bin";

  private final DistributionServiceConfig distributionServiceConfig;
  private final DigitalCovidCertificateClient digitalCovidCertificateClient;
  private final BusinessRulesArchiveBuilder businessRulesArchiveBuilder;

  /**
   * Create an instance.
   */
  public BoosterNotificationStructureProvider(DistributionServiceConfig distributionServiceConfig,
      CryptoProvider cryptoProvider, DigitalGreenCertificateToCborMapping dgcToCborMapping,
      DigitalCovidCertificateClient digitalCovidCertificateClient,
      BusinessRulesArchiveBuilder businessRulesArchiveBuilder) {
    this.distributionServiceConfig = distributionServiceConfig;
    this.digitalCovidCertificateClient = digitalCovidCertificateClient;
    this.businessRulesArchiveBuilder = businessRulesArchiveBuilder;
  }

  /**
   * Returns the publishable archive with Booster Notification Business rules Cbor encoded structures.
   */
  public Optional<Writable<WritableOnDisk>> getBoosterNotificationRules() {
    return getBoosterNotificationRulesArchive(
        distributionServiceConfig.getDigitalGreenCertificate().getBoosterNotification());
  }

  /**
   * Create business rules Archive. If any exception is thrown during fetching data and packaging process, an empty
   * Archive will be published in order to not override any previous archive on CDN with broken data.
   *
   * @param archiveName - archive name for packaging rules
   * @return - business rules archive
   */
  private Optional<Writable<WritableOnDisk>> getBoosterNotificationRulesArchive(String archiveName) {
    return businessRulesArchiveBuilder
        .setArchiveName(archiveName)
        .setExportBinaryFilename(EXPORT_BINARY_FILENAME)
        .setRuleType(BoosterNotification)
        .setBusinessRuleItemSupplier(digitalCovidCertificateClient::getBoosterNotificationRules)
        .setBusinessRuleSupplier(digitalCovidCertificateClient::getBoosterNotificationRuleByHash)
        .build();
  }
}
