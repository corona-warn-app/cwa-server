package app.coronawarn.server.services.distribution.assembly.component;

import app.coronawarn.server.common.protocols.internal.dgc.ValueSets;
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
import app.coronawarn.server.services.distribution.dgc.client.DigitalCovidCertificateClient;
import app.coronawarn.server.services.distribution.dgc.dsc.DigitalSigningCertificatesToProtobufMapping;
import app.coronawarn.server.services.distribution.dgc.exception.DigitalCovidCertificateException;
import app.coronawarn.server.services.distribution.dgc.exception.FetchBusinessRulesException;
import app.coronawarn.server.services.distribution.dgc.exception.FetchDscTrustListException;
import app.coronawarn.server.services.distribution.dgc.exception.FetchValueSetsException;
import app.coronawarn.server.services.distribution.dgc.structure.DigitalCertificatesDirectory;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Reads configuration parameters from the respective files in the class path or from DCC client and build a {@link
 * DigitalCertificatesStructureProvider} with them.
 */
@Component
public class DigitalCertificatesStructureProvider {

  private static final Logger logger = LoggerFactory.getLogger(DigitalCertificatesStructureProvider.class);

  public static final String ONBOARDED_COUNTRIES = "onboarded-countries";
  public static final String DIGITAL_CERTIFICATES_STRUCTURE_PROVIDER = "dscs";
  public static final String ACCEPTANCE_RULES = "acceptance-rules";
  public static final String INVALIDATION_RULES = "invalidation-rules";

  private final DistributionServiceConfig distributionServiceConfig;
  private final CryptoProvider cryptoProvider;
  private final DigitalGreenCertificateToProtobufMapping dgcToProtobufMapping;
  private final DigitalGreenCertificateToCborMapping dgcToCborMapping;
  private final DigitalSigningCertificatesToProtobufMapping digitalSigningCertificatesToProtobufMapping;
  private final DigitalCovidCertificateClient digitalCovidCertificateClient;
  private final BusinessRulesArchiveBuilder businessRulesArchiveBuilder;

  /**
   * Create an instance.
   */
  public DigitalCertificatesStructureProvider(DistributionServiceConfig distributionServiceConfig,
      CryptoProvider cryptoProvider, DigitalGreenCertificateToProtobufMapping dgcToProtobufMapping,
      DigitalGreenCertificateToCborMapping dgcToCborMapping,
      DigitalSigningCertificatesToProtobufMapping digitalSigningCertificatesToProtobufMapping,
      DigitalCovidCertificateClient digitalCovidCertificateClient,
      BusinessRulesArchiveBuilder businessRulesArchiveBuilder) {
    this.distributionServiceConfig = distributionServiceConfig;
    this.cryptoProvider = cryptoProvider;
    this.dgcToProtobufMapping = dgcToProtobufMapping;
    this.dgcToCborMapping = dgcToCborMapping;
    this.digitalSigningCertificatesToProtobufMapping = digitalSigningCertificatesToProtobufMapping;
    this.digitalCovidCertificateClient = digitalCovidCertificateClient;
    this.businessRulesArchiveBuilder = businessRulesArchiveBuilder;
  }

  /**
   * Returns the publishable archive with the Digital Green Certificates protobuf structures for mobile clients and
   * Business rules Cbor encoded structures.
   */
  public DirectoryOnDisk getDigitalGreenCertificates() {
    return constructArchiveToPublish(distributionServiceConfig.getDigitalGreenCertificate());
  }

  private DigitalCertificatesDirectory constructArchiveToPublish(DigitalGreenCertificate dgcConfig) {
    DigitalCertificatesDirectory dgcDirectory = new DigitalCertificatesDirectory(
        distributionServiceConfig, dgcConfig, cryptoProvider);
    try {
      ValueSets valueSets = dgcToProtobufMapping.constructProtobufMapping();
      dgcDirectory.addValueSetsToAll(valueSets);
    } catch (FetchValueSetsException e) {
      logger.error("Digital green certificate valuesets were not written because of: ", e);
    }

    getOnboardedCountriesArchive().ifPresent(dgcDirectory::addWritable);
    getRulesArchive(RuleType.Acceptance, ACCEPTANCE_RULES).ifPresent(dgcDirectory::addWritable);
    getRulesArchive(RuleType.Invalidation, INVALIDATION_RULES).ifPresent(dgcDirectory::addWritable);
    getDscsArchive().ifPresent(dgcDirectory::addWritable);

    return dgcDirectory;
  }

  /**
   * Create onboarded countries Archive. If any exception is thrown during fetching data and packaging process, an empty
   * Archive will be published in order to not override any previous archive on CDN with broken data.
   *
   * @return - Onboarded countries archive.
   */
  private Optional<Writable<WritableOnDisk>> getOnboardedCountriesArchive() {
    ArchiveOnDisk onboardedCountries = new ArchiveOnDisk(ONBOARDED_COUNTRIES);
    try {
      onboardedCountries
          .addWritable(new FileOnDisk("export.bin", dgcToCborMapping.constructCborCountries()));
      logger.info("Onboarded countries archive has been added to the DGC distribution folder");

      return Optional.of(new DistributionArchiveSigningDecorator(onboardedCountries, cryptoProvider,
          distributionServiceConfig));
    } catch (DigitalCovidCertificateException e) {
      logger.error("Onboarded countries archive was not overwritten because of:", e);
    } catch (FetchBusinessRulesException e) {
      logger.error("Onboarded countries archive was not overwritten because countries could not been fetched:", e);
    }

    return Optional.empty();
  }

  /**
   * Create DSCs archive.
   *
   * @return - DSCs archive
   */
  private Optional<Writable<WritableOnDisk>> getDscsArchive() {
    ArchiveOnDisk dscsArchive = new ArchiveOnDisk(DIGITAL_CERTIFICATES_STRUCTURE_PROVIDER);
    try {
      dscsArchive
          .addWritable(new FileOnDisk("export.bin",
              digitalSigningCertificatesToProtobufMapping.constructProtobufMapping().toByteArray()));
      logger.info("Digital signing certificate archive has been added to the DGC distribution folder");
    } catch (UnableToLoadFileException | FetchDscTrustListException e) {
      logger.error("Digital signing certificate archive was not overwritten because of:", e);
      return Optional.empty();
    }

    return Optional.of(new DistributionArchiveSigningDecorator(dscsArchive, cryptoProvider,
        distributionServiceConfig));
  }

  /**
   * Create business rules Archive. If any exception is thrown during fetching data and packaging process, an empty
   * Archive will be published in order to not override any previous archive on CDN with broken data. Provided rules are
   * filtered by rule type parameter which could be 'Acceptance' or 'Invalidation'.
   *
   * @param ruleType    - rule type to receive rules for
   * @param archiveName - archive name for packaging rules
   * @return - business rules archive
   */
  private Optional<Writable<WritableOnDisk>> getRulesArchive(RuleType ruleType,
      String archiveName) {
    return businessRulesArchiveBuilder
        .setArchiveName(archiveName)
        .setExportBinaryFilename(distributionServiceConfig.getDigitalGreenCertificate().getExportArchiveName())
        .setRuleType(ruleType)
        .setBusinessRuleItemSupplier(digitalCovidCertificateClient::getRules)
        .setBusinessRuleSupplier(digitalCovidCertificateClient::getCountryRuleByHash)
        .build();
  }
}
