package app.coronawarn.server.services.distribution.assembly.component;

import static app.coronawarn.server.services.distribution.dgc.BusinessRule.RuleType.COMMON_COVID_LOGIC;

import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class CommonCovidLogicStructureProvider {

  private final DistributionServiceConfig distributionServiceConfig;
  private final CommonCovidLogicArchiveBuilder commonCovidLogicArchiveBuilder;

  public CommonCovidLogicStructureProvider(
      DistributionServiceConfig distributionServiceConfig,
      CommonCovidLogicArchiveBuilder commonCovidLogicArchiveBuilder) {
    this.distributionServiceConfig = distributionServiceConfig;
    this.commonCovidLogicArchiveBuilder = commonCovidLogicArchiveBuilder;
  }

  public Optional<Writable<WritableOnDisk>> getCommonCovidLogicRules() {
    return getCommonCovidLogicRulesArchive(
        distributionServiceConfig.getDigitalGreenCertificate().getCommonCovidLogic());
  }

  private Optional<Writable<WritableOnDisk>> getCommonCovidLogicRulesArchive(String archiveName) {
    return commonCovidLogicArchiveBuilder
        .setArchiveName(archiveName)
        .setExportBinaryFilename(distributionServiceConfig.getDigitalGreenCertificate().getExportArchiveName())
        .setRuleType(COMMON_COVID_LOGIC)
        .build();
  }
}
