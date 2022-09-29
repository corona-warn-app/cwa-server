package app.coronawarn.server.services.distribution.assembly.component;

import static app.coronawarn.server.common.shared.util.SerializationUtils.cborEncode;

import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.archive.Archive;
import app.coronawarn.server.services.distribution.assembly.structure.archive.ArchiveOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.archive.decorator.signing.DistributionArchiveSigningDecorator;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.file.FileOnDisk;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.dgc.BusinessRule;
import app.coronawarn.server.services.distribution.dgc.BusinessRule.RuleType;
import app.coronawarn.server.services.distribution.dgc.BusinessRuleItem;
import app.coronawarn.server.services.distribution.dgc.exception.DigitalCovidCertificateException;
import app.coronawarn.server.services.distribution.dgc.exception.FetchBusinessRulesException;
import app.coronawarn.server.services.distribution.dgc.functions.BusinessRuleItemSupplier;
import app.coronawarn.server.services.distribution.dgc.functions.BusinessRuleSupplier;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component
@Scope("prototype")
@Profile("!revocation")
public class CommonCovidLogicArchiveBuilder {

  private static final Logger logger = LoggerFactory.getLogger(CommonCovidLogicArchiveBuilder.class);

  public static final String JSON_SCHEMA_PATH = "dgc";
  public static final String CCL_JSON_SCHEMA = JSON_SCHEMA_PATH + "/ccl-configuration.json";
  public static final String CONFIG_V = "config-v";

  private final DistributionServiceConfig distributionServiceConfig;

  private String directoryName;
  private RuleType ruleType;
  private String exportBinaryFilename;
  private BusinessRuleItemSupplier<List<BusinessRuleItem>> businessRuleItemSupplier;
  private BusinessRuleSupplier<BusinessRule, String, String> businessRuleSupplier;
  private final CryptoProvider cryptoProvider;

  /**
   * Builds the Structure of the config files for different versions.
   *
   * @param distributionServiceConfig distributionServiceConfig
   * @param cryptoProvider            cryptoProvider
   */
  public CommonCovidLogicArchiveBuilder(
      DistributionServiceConfig distributionServiceConfig,
      CryptoProvider cryptoProvider) {
    this.distributionServiceConfig = distributionServiceConfig;
    this.cryptoProvider = cryptoProvider;
  }

  /**
   * Builds a directory on disk containing ccl rules.
   *
   * @return - optional archive on disk
   */
  public Optional<Writable<WritableOnDisk>> build()
      throws FetchBusinessRulesException {
    final DirectoryOnDisk rulesDirectory = new DirectoryOnDisk(
        ObjectUtils.isEmpty(directoryName) ? distributionServiceConfig.getDefaultArchiveName() : directoryName);

    Collection<BusinessRule> businessRules = getValidBusinessRules(getBusinessRuleItemsFilterByIdentifier());
    Map<Integer, Collection<BusinessRule>> filteredBusinessRules = BusinessRule.filterAndSort(businessRules);

    Collection<Archive<WritableOnDisk>> rulesArchives = getCommonCovidLogicArchives(filteredBusinessRules);

    rulesArchives.forEach(rulesDirectory::addWritable);

    return Optional.of(rulesDirectory);
  }

  private Collection<Archive<WritableOnDisk>> getCommonCovidLogicArchives(
      Map<Integer, Collection<BusinessRule>> filteredBusinessRules) {
    return filteredBusinessRules
        .keySet()
        .stream()
        .map(key -> {
          ArchiveOnDisk rulesArchive = new ArchiveOnDisk(
              ObjectUtils.isEmpty(CONFIG_V + key) ? distributionServiceConfig.getDefaultArchiveName() : CONFIG_V + key);
          try {
            rulesArchive
                .addWritable(
                    new FileOnDisk(exportBinaryFilename, cborEncodeOrElseThrow(filteredBusinessRules.get(key))));
            return new DistributionArchiveSigningDecorator(rulesArchive, cryptoProvider, distributionServiceConfig);

          } catch (DigitalCovidCertificateException e) {
            logger.error(String.format("%s archive was not overwritten because of: ", CONFIG_V + key), e);
          }
          return rulesArchive;
        }).collect(Collectors.toList());
  }

  private Collection<BusinessRule> getValidBusinessRules(Collection<BusinessRuleItem> businessRulesItems) {
    Collection<BusinessRule> businessRules = new ArrayList<>();
    for (BusinessRuleItem businessRuleItem : businessRulesItems) {
      BusinessRule businessRule = null;
      try {
        businessRule = businessRuleSupplier.get(businessRuleItem.getCountry(), businessRuleItem.getHash());
        if (businessRule != null && businessRule.getType().equalsIgnoreCase(ruleType.getType())) {
          businessRules.add(businessRule);
        }
      } catch (FetchBusinessRulesException e) {
        logger.error("Config archive was not overwritten because business rule could not been fetched:", e);
      }
    }
    return businessRules;
  }

  private Collection<BusinessRuleItem> getBusinessRuleItemsFilterByIdentifier() throws FetchBusinessRulesException {
    return businessRuleItemSupplier.get().stream()
        .filter(businessRuleItem -> List.of(distributionServiceConfig.getDigitalGreenCertificate().getCclAllowList())
            .contains(businessRuleItem.getIdentifier()))
        .collect(Collectors.toList());
  }

  public CommonCovidLogicArchiveBuilder setDirectoryName(String directoryName) {
    this.directoryName = directoryName;
    return this;
  }

  public CommonCovidLogicArchiveBuilder setRuleType(RuleType ruleType) {
    this.ruleType = ruleType;
    return this;
  }

  public CommonCovidLogicArchiveBuilder setBusinessRuleItemSupplier(
      BusinessRuleItemSupplier<List<BusinessRuleItem>> businessRuleItemSupplier) {
    this.businessRuleItemSupplier = businessRuleItemSupplier;
    return this;
  }

  public CommonCovidLogicArchiveBuilder setBusinessRuleSupplier(
      BusinessRuleSupplier<BusinessRule, String, String> businessRuleSupplier) {
    this.businessRuleSupplier = businessRuleSupplier;
    return this;
  }

  private byte[] cborEncodeOrElseThrow(Object subject) throws DigitalCovidCertificateException {
    try {
      return cborEncode(subject);
    } catch (JsonProcessingException e) {
      throw new DigitalCovidCertificateException("Cbor encryption failed because of Json processing:", e);
    }
  }

  public CommonCovidLogicArchiveBuilder setExportBinaryFilename(String exportBinaryFilename) {
    this.exportBinaryFilename = exportBinaryFilename;
    return this;
  }
}
