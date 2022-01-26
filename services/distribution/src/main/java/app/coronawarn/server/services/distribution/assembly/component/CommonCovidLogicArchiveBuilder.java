package app.coronawarn.server.services.distribution.assembly.component;

import static app.coronawarn.server.common.shared.util.SerializationUtils.cborEncode;
import static app.coronawarn.server.common.shared.util.SerializationUtils.validateJsonSchema;

import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.archive.ArchiveOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.archive.decorator.signing.DistributionArchiveSigningDecorator;
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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.everit.json.schema.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component
@Scope("prototype")
public class CommonCovidLogicArchiveBuilder {

  public static final String COMMON_COVID_LOGIC_JSON_CLASSPATH = "dgc/ccl-configuration.json";
  public static final String CONFIG_V = "config-v";

  private final DistributionServiceConfig distributionServiceConfig;
  private final ResourceLoader resourceLoader;

  private String archiveName;
  private RuleType ruleType;
  private BusinessRuleItemSupplier<List<BusinessRuleItem>> businessRuleItemSupplier;
  private BusinessRuleSupplier<BusinessRule, String, String> businessRuleSupplier;
  private final CryptoProvider cryptoProvider;

  /**
   * Builds the Structure of the config files for different versions.
   * @param distributionServiceConfig distributionServiceConfig
   * @param resourceLoader resourceLoader
   * @param cryptoProvider cryptoProvider
   */
  public CommonCovidLogicArchiveBuilder(
      DistributionServiceConfig distributionServiceConfig, ResourceLoader resourceLoader,
      CryptoProvider cryptoProvider) {
    this.distributionServiceConfig = distributionServiceConfig;
    this.resourceLoader = resourceLoader;
    this.cryptoProvider = cryptoProvider;
  }

  /**
   * Builds a archive on disk containing ccl rules.
   *
   * @return - optional archive on disk
   */
  public List<Writable<WritableOnDisk>> build() throws FetchBusinessRulesException, DigitalCovidCertificateException {
    final ArchiveOnDisk rulesArchive = new ArchiveOnDisk(
        ObjectUtils.isEmpty(archiveName) ? distributionServiceConfig.getDefaultArchiveName() : archiveName);

    List<BusinessRuleItem> businessRulesItems = businessRuleItemSupplier.get();

    List<BusinessRule> businessRules = new ArrayList<>();
    for (BusinessRuleItem businessRuleItem : businessRulesItems) {
      BusinessRule businessRule =
          businessRuleSupplier.get(businessRuleItem.getCountry(), businessRuleItem.getHash());

      if (businessRule.getType().equalsIgnoreCase(ruleType.getType())) {
        try (final InputStream in = resourceLoader.getResource(COMMON_COVID_LOGIC_JSON_CLASSPATH).getInputStream()) {
          validateJsonSchema(businessRule, in);
          businessRules.add(businessRule);
        } catch (JsonProcessingException | ValidationException e) {
          throw new DigitalCovidCertificateException(
              "Rule for country '" + businessRuleItem.getCountry() + "' having hash '" + businessRuleItem.getHash()
                  + "' is not valid", e);
        } catch (IOException e) {
          throw new DigitalCovidCertificateException(
              "Validation rules schema found at: " + COMMON_COVID_LOGIC_JSON_CLASSPATH + "could not be found", e);
        }
      }
    }

    Map<Integer, BusinessRule> filteredBusinessRules = BusinessRule.filterAndSort(businessRules);
    return filteredBusinessRules
        .keySet()
        .stream()
        .map(key -> {
          try {
            rulesArchive
                .addWritable(new FileOnDisk(CONFIG_V + key, cborEncodeOrElseThrow(filteredBusinessRules.get(key))));
          } catch (DigitalCovidCertificateException e) {
            e.printStackTrace();
          }
          return new DistributionArchiveSigningDecorator(rulesArchive, cryptoProvider, distributionServiceConfig);
        })
        .collect(Collectors.toList());
  }

  public CommonCovidLogicArchiveBuilder setArchiveName(String archiveName) {
    this.archiveName = archiveName;
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
}
