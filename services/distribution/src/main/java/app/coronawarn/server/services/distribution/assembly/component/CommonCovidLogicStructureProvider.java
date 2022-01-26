package app.coronawarn.server.services.distribution.assembly.component;

import static app.coronawarn.server.services.distribution.dgc.BusinessRule.RuleType.COMMON_COVID_LOGIC;

import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectoryOnDisk;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.dgc.client.DigitalCovidCertificateClient;
import app.coronawarn.server.services.distribution.dgc.exception.DigitalCovidCertificateException;
import app.coronawarn.server.services.distribution.dgc.exception.FetchBusinessRulesException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
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
  public DirectoryOnDisk getCommonCovidLogicRules() {
    IndexDirectoryOnDisk<String> cclDirectory = new IndexDirectoryOnDisk<>(
        distributionServiceConfig.getDigitalGreenCertificate().getCclDirectory(),
        ignoredValue -> Set.of(),
        Object::toString);

    getCommonCovidLogicRulesArchive(
        distributionServiceConfig.getDigitalGreenCertificate().getCommonCovidLogic()).forEach(ccl ->
        cclDirectory.addWritable(ccl));

    return cclDirectory;
  }

  private List<Writable<WritableOnDisk>> getCommonCovidLogicRulesArchive(String archiveName) {
    try {
      return commonCovidLogicArchiveBuilder
          .setArchiveName(archiveName)
          .setRuleType(COMMON_COVID_LOGIC)
          .setBusinessRuleItemSupplier(digitalCovidCertificateClient::getCommonCovidLogicRules)
          .setBusinessRuleSupplier(digitalCovidCertificateClient::getCommonCovidLogicRuleByHash)
          .build();
    } catch (DigitalCovidCertificateException e) {
      logger.error(String.format("%s archive was not overwritten because of: ", archiveName), e);
    } catch (FetchBusinessRulesException e) {
      logger.error(String
          .format("%s archive was not overwritten because business rules could not been fetched: ", archiveName), e);
    }
    return Collections.emptyList();
  }
}
