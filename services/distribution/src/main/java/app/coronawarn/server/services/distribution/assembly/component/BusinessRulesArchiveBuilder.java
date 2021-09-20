package app.coronawarn.server.services.distribution.assembly.component;

import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.archive.ArchiveOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.archive.decorator.signing.DistributionArchiveSigningDecorator;
import app.coronawarn.server.services.distribution.assembly.structure.file.FileOnDisk;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.dgc.BusinessRule;
import app.coronawarn.server.services.distribution.dgc.BusinessRule.RuleType;
import app.coronawarn.server.services.distribution.dgc.BusinessRuleItem;
import app.coronawarn.server.services.distribution.dgc.DigitalGreenCertificateToCborMapping;
import app.coronawarn.server.services.distribution.dgc.exception.DigitalCovidCertificateException;
import app.coronawarn.server.services.distribution.dgc.exception.FetchBusinessRulesException;
import app.coronawarn.server.services.distribution.dgc.functions.BusinessRuleItemSupplier;
import app.coronawarn.server.services.distribution.dgc.functions.BusinessRuleSupplier;
import java.util.List;
import java.util.Optional;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class BusinessRulesArchiveBuilder {

  private static final Logger logger = LoggerFactory.getLogger(BusinessRulesArchiveBuilder.class);

  private final DigitalGreenCertificateToCborMapping dgcToCborMapping;
  private final CryptoProvider cryptoProvider;
  private final DistributionServiceConfig distributionServiceConfig;

  private String archiveName;
  private String exportBinaryFilename;
  private RuleType ruleType;
  private BusinessRuleItemSupplier<List<BusinessRuleItem>> businessRuleItemSupplier;
  private BusinessRuleSupplier<BusinessRule, String, String> businessRuleSupplier;

  public BusinessRulesArchiveBuilder(DigitalGreenCertificateToCborMapping dgcToCborMapping,
      CryptoProvider cryptoProvider, DistributionServiceConfig distributionServiceConfig) {
    this.dgcToCborMapping = dgcToCborMapping;
    this.cryptoProvider = cryptoProvider;
    this.distributionServiceConfig = distributionServiceConfig;
  }

  /**
   * Builds a archive on disk containing business rules.
   * @return - optional archive on disk
   */
  public Optional<Writable<WritableOnDisk>> build() {
    ArchiveOnDisk rulesArchive = new ArchiveOnDisk(
        Strings.isEmpty(archiveName) ? distributionServiceConfig.getDefaultArchiveName() : archiveName);

    try {
      rulesArchive
          .addWritable(new FileOnDisk(exportBinaryFilename,
              dgcToCborMapping.constructCborRules(ruleType, businessRuleItemSupplier, businessRuleSupplier)));
      logger.info(archiveName + " archive has been added to the DGC distribution folder");

      return Optional.of(
          new DistributionArchiveSigningDecorator(rulesArchive, cryptoProvider, distributionServiceConfig));
    } catch (DigitalCovidCertificateException e) {
      logger.error(archiveName + " archive was not overwritten because of:", e);
    } catch (FetchBusinessRulesException e) {
      logger.error(archiveName + " archive was not overwritten because business rules could not been fetched:", e);
    }

    return Optional.empty();
  }

  public BusinessRulesArchiveBuilder setArchiveName(String archiveName) {
    this.archiveName = archiveName;
    return this;
  }

  public BusinessRulesArchiveBuilder setRuleType(RuleType ruleType) {
    this.ruleType = ruleType;
    return this;
  }

  public BusinessRulesArchiveBuilder setBusinessRuleItemSupplier(
      BusinessRuleItemSupplier<List<BusinessRuleItem>> businessRuleItemSupplier) {
    this.businessRuleItemSupplier = businessRuleItemSupplier;
    return this;
  }

  public BusinessRulesArchiveBuilder setBusinessRuleSupplier(
      BusinessRuleSupplier<BusinessRule, String, String> businessRuleSupplier) {
    this.businessRuleSupplier = businessRuleSupplier;
    return this;
  }

  public BusinessRulesArchiveBuilder setExportBinaryFilename(String exportBinaryFilename) {
    this.exportBinaryFilename = exportBinaryFilename;
    return this;
  }

}
