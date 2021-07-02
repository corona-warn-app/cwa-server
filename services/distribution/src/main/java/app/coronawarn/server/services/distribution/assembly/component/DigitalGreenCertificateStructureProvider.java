package app.coronawarn.server.services.distribution.assembly.component;

import app.coronawarn.server.common.shared.exception.UnableToLoadFileException;
import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.archive.ArchiveOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.archive.decorator.signing.DistributionArchiveSigningDecorator;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.file.FileOnDisk;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.DigitalGreenCertificate;
import app.coronawarn.server.services.distribution.dgc.BusinessRule.RuleType;
import app.coronawarn.server.services.distribution.dgc.DigitalGreenCertificateToCborMapping;
import app.coronawarn.server.services.distribution.dgc.DigitalGreenCertificateToProtobufMapping;
import app.coronawarn.server.services.distribution.dgc.exception.DigitalCovidCertificateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Reads configuration parameters from the respective files in the class path or from DCC client and build a {@link
 * DigitalGreenCertificateStructureProvider} with them.
 */
@Component
public class DigitalGreenCertificateStructureProvider {

  private static final Logger logger = LoggerFactory.getLogger(DigitalGreenCertificateStructureProvider.class);

  public static final String ONBOARDED_COUNTRIES = "onboarded-countries";
  public static final String ACCEPTANCE_RULES = "acceptance-rules";
  public static final String INVALIDATION_RULES = "invalidation-rules";
  public static final String EMPTY_STRING = "";
  public static final String EXPORT_BIN = "export.bin";

  private final DistributionServiceConfig distributionServiceConfig;
  private final CryptoProvider cryptoProvider;
  private final DigitalGreenCertificateToProtobufMapping dgcToProtobufMapping;
  private final DigitalGreenCertificateToCborMapping  dgcToCborMapping;

  /**
   * Create an instance.
   */
  public DigitalGreenCertificateStructureProvider(DistributionServiceConfig distributionServiceConfig,
      CryptoProvider cryptoProvider, DigitalGreenCertificateToProtobufMapping dgcToProtobufMapping,
      DigitalGreenCertificateToCborMapping  dgcToCborMapping) {
    this.distributionServiceConfig = distributionServiceConfig;
    this.cryptoProvider = cryptoProvider;
    this.dgcToProtobufMapping = dgcToProtobufMapping;
    this.dgcToCborMapping = dgcToCborMapping;
  }

  /**
   * Returns the publishable archive with the Digital Green Certificates protobuf structures for mobile clients
   * and Business rules Cbor encoded structures.
   */
  public DirectoryOnDisk getDigitalGreenCertificates() {
    try {
      return constructArchiveToPublish(distributionServiceConfig.getDigitalGreenCertificate());
    } catch (UnableToLoadFileException e) {
      logger.error("We don't generate a value-sets file and this shouldn't override existing ones.", e);
      return new DirectoryOnDisk(EMPTY_STRING);
    }
  }

  private DirectoryOnDisk constructArchiveToPublish(DigitalGreenCertificate dgcConfig)
      throws UnableToLoadFileException {
    DirectoryOnDisk dgcDirectory = new DirectoryOnDisk(dgcConfig.getDgcDirectory());

    for (String currentLanguage: dgcConfig.getSupportedLanguages()) {
      ArchiveOnDisk archiveToPublish = new ArchiveOnDisk(dgcConfig.getValuesetsFileName());
      archiveToPublish.addWritable(new FileOnDisk(EXPORT_BIN,
          dgcToProtobufMapping.constructProtobufMapping().toByteArray()));
      DirectoryOnDisk languageDirectory = new DirectoryOnDisk(currentLanguage.toLowerCase());
      languageDirectory.addWritable(new DistributionArchiveSigningDecorator(
          archiveToPublish, cryptoProvider, distributionServiceConfig));
      dgcDirectory.addWritable(languageDirectory);
      logger.info("Writing digital green certificate to {}/{}/{}.", dgcDirectory.getName(), languageDirectory.getName(),
          archiveToPublish.getName());
    }

    dgcDirectory.addWritable(getOnboardedCountriesArchive());
    dgcDirectory.addWritable(getRulesArchive(RuleType.Acceptance, ACCEPTANCE_RULES));
    dgcDirectory.addWritable(getRulesArchive(RuleType.Invalidation, INVALIDATION_RULES));

    return dgcDirectory;
  }

  /**
   * Create onboarded countries Archive. If any exception is thrown during fetching data and packaging process,
   * an empty Archive will be published in order to not override any previous archive on CDN with broken data.
   * @return - Onboarded countries archive
   */
  private Writable<WritableOnDisk> getOnboardedCountriesArchive() {
    ArchiveOnDisk onboardedCountries = new ArchiveOnDisk(ONBOARDED_COUNTRIES);
    try {
      onboardedCountries
          .addWritable(new FileOnDisk("export.bin", dgcToCborMapping.constructCborCountries()));
      logger.info("Onboarded countries archive has been added to the DGC distribution folder");
    } catch (DigitalCovidCertificateException e) {
      logger.error("Onboarded countries archive was not overwritten because of:", e);
      return new ArchiveOnDisk(EMPTY_STRING);
    }

    return new DistributionArchiveSigningDecorator(onboardedCountries, cryptoProvider,
        distributionServiceConfig);
  }

  /**
   * Create business rules Archive. If any exception is thrown during fetching data and packaging process,
   * an empty Archive will be published in order to not override any previous archive on CDN with broken data.
   * Provided rules are filtered by rule type parameter which could be 'Acceptance' or 'Invalidation'.
   * @param ruleType - rule type to receive rules for
   * @param archiveName - archive name for packaging rules
   * @return - business rules archive
   */
  private Writable<WritableOnDisk> getRulesArchive(RuleType ruleType, String archiveName) {
    ArchiveOnDisk rulesArchive = new ArchiveOnDisk(archiveName);

    try {
      rulesArchive
          .addWritable(new FileOnDisk("export.bin", dgcToCborMapping.constructCborRules(ruleType)));
      logger.info(archiveName + " archive has been added to the DGC distribution folder");
    } catch (DigitalCovidCertificateException e) {
      logger.error(archiveName + " archive was not overwritten because of:", e);
      return new ArchiveOnDisk(EMPTY_STRING);
    }

    return new DistributionArchiveSigningDecorator(rulesArchive, cryptoProvider,
        distributionServiceConfig);
  }
}
