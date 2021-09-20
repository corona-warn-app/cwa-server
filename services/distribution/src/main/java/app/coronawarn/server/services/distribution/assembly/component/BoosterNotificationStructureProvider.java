package app.coronawarn.server.services.distribution.assembly.component;


import static app.coronawarn.server.services.distribution.dgc.BusinessRule.RuleType.BoosterNotification;

import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.dgc.client.DigitalCovidCertificateClient;
import app.coronawarn.server.services.distribution.dgc.client.DigitalCovidCertificateFeignClient;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Reads Booster notification rules from the respective files in the class path or from dcc client and build a {@link
 * BoosterNotificationStructureProvider} with them.
 */

@Component
public class BoosterNotificationStructureProvider {

  private final DistributionServiceConfig distributionServiceConfig;
  private final DigitalCovidCertificateClient digitalCovidCertificateClient;
  private final BusinessRulesArchiveBuilder businessRulesArchiveBuilder;

  /**
   * Create an instance.
   */
  public BoosterNotificationStructureProvider(DistributionServiceConfig distributionServiceConfig,
      DigitalCovidCertificateClient digitalCovidCertificateClient,
      BusinessRulesArchiveBuilder businessRulesArchiveBuilder) {
    this.distributionServiceConfig = distributionServiceConfig;
    this.digitalCovidCertificateClient = digitalCovidCertificateClient;
    this.businessRulesArchiveBuilder = businessRulesArchiveBuilder;
  }

  /**
   * Fetches the Booster Notification Rules using
   * {@link DigitalCovidCertificateFeignClient#getBoosterNotificationRules()}.
   * Encode them in CBOR format.
   * Pack them in an archive signed and ready to be published on CDN.
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
        .setExportBinaryFilename(distributionServiceConfig.getDigitalGreenCertificate().getExportArchiveName())
        .setRuleType(BoosterNotification)
        .setBusinessRuleItemSupplier(digitalCovidCertificateClient::getBoosterNotificationRules)
        .setBusinessRuleSupplier(digitalCovidCertificateClient::getBoosterNotificationRuleByHash)
        .build();
  }
}
