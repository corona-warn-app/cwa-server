package app.coronawarn.server.services.distribution.assembly.component;

import static app.coronawarn.server.services.distribution.dgc.BusinessRule.RuleType.COMMON_COVID_LOGIC;

import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.dgc.client.DigitalCovidCertificateClient;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class CommonCovidLogicStructureProvider {

  private final DistributionServiceConfig distributionServiceConfig;
  private final DigitalCovidCertificateClient digitalCovidCertificateClient;
  private final BusinessRulesArchiveBuilder businessRulesArchiveBuilder;

  public CommonCovidLogicStructureProvider(DistributionServiceConfig distributionServiceConfig,
      DigitalCovidCertificateClient digitalCovidCertificateClient,
      BusinessRulesArchiveBuilder businessRulesArchiveBuilder) {
    this.distributionServiceConfig = distributionServiceConfig;
    this.digitalCovidCertificateClient = digitalCovidCertificateClient;
    this.businessRulesArchiveBuilder = businessRulesArchiveBuilder;
  }

  public Optional<Writable<WritableOnDisk>> getCommonCovidLogicRules() {
    return getCommonCovidLogicRulesArchive(
        distributionServiceConfig.getDigitalGreenCertificate().getCommonCovidLogic());
  }



  private Optional<Writable<WritableOnDisk>> getCommonCovidLogicRulesArchive(String archiveName) {
    return businessRulesArchiveBuilder
        .setArchiveName(archiveName)
        .setExportBinaryFilename(distributionServiceConfig.getDigitalGreenCertificate().getExportArchiveName())
        .setRuleType(COMMON_COVID_LOGIC)
        .setBusinessRuleItemSupplier(digitalCovidCertificateClient::getCommonCovidLogicRules)
        .setBusinessRuleSupplier(digitalCovidCertificateClient::getCommonCovidLogicRuleByHash)
        .build();
  }
}
