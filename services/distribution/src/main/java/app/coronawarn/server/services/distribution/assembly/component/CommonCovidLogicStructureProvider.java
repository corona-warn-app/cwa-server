package app.coronawarn.server.services.distribution.assembly.component;

import static app.coronawarn.server.services.distribution.dgc.BusinessRule.RuleType.COMMON_COVID_LOGIC;

import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryOnDisk;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.dgc.client.DigitalCovidCertificateClient;
import app.coronawarn.server.services.distribution.dgc.exception.FetchBusinessRulesException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!revocation")
public class CommonCovidLogicStructureProvider {

  private static final Logger logger = LoggerFactory.getLogger(CommonCovidLogicStructureProvider.class);

  private final DistributionServiceConfig distributionServiceConfig;
  private final DigitalCovidCertificateClient digitalCovidCertificateClient;
  private final CommonCovidLogicArchiveBuilder commonCovidLogicArchiveBuilder;

  /**
   * Common Covid Logic Structure Provider for config files.
   *
   * @param distributionServiceConfig      distributionServiceConfig
   * @param digitalCovidCertificateClient  digitalCovidCertificateClient
   * @param commonCovidLogicArchiveBuilder commonCovidLogicArchiveBuilder
   */
  public CommonCovidLogicStructureProvider(DistributionServiceConfig distributionServiceConfig,
      DigitalCovidCertificateClient digitalCovidCertificateClient,
      CommonCovidLogicArchiveBuilder commonCovidLogicArchiveBuilder) {
    this.distributionServiceConfig = distributionServiceConfig;
    this.digitalCovidCertificateClient = digitalCovidCertificateClient;
    this.commonCovidLogicArchiveBuilder = commonCovidLogicArchiveBuilder;
  }

  /**
   * CommonCovidLogic directory.
   *
   * @return CommonCovidLogic rules config.
   */
  public Optional<Writable<WritableOnDisk>> getCommonCovidLogicRules() {
    try {
      Optional<Writable<WritableOnDisk>> cclDirectory = getCommonCovidLogicRulesDirectory(
          distributionServiceConfig.getDigitalGreenCertificate().getCclDirectory());
      if (cclDirectory.isPresent() && ((DirectoryOnDisk) cclDirectory.get()).getWritables().isEmpty()) {
        return Optional.empty();
      }
      return cclDirectory;
    } catch (FetchBusinessRulesException e) {
      logger.error(String
          .format("%s archive was not overwritten because config business rules could not been fetched: ",
              distributionServiceConfig.getDigitalGreenCertificate().getCclDirectory()), e);
    }
    return Optional.empty();
  }

  private Optional<Writable<WritableOnDisk>> getCommonCovidLogicRulesDirectory(String directoryName)
      throws FetchBusinessRulesException {
    return commonCovidLogicArchiveBuilder
        .setDirectoryName(directoryName)
        .setRuleType(COMMON_COVID_LOGIC)
        .setExportBinaryFilename(distributionServiceConfig.getDigitalGreenCertificate().getExportArchiveName())
        .setBusinessRuleItemSupplier(digitalCovidCertificateClient::getCommonCovidLogicRules)
        .setBusinessRuleSupplier(digitalCovidCertificateClient::getCommonCovidLogicRuleByHash)
        .build();
  }
}
