package app.coronawarn.server.services.distribution.assembly.component;

import app.coronawarn.server.common.shared.exception.DefaultValueSetsMissingException;
import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.archive.ArchiveOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.archive.decorator.signing.DistributionArchiveSigningDecorator;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.file.FileOnDisk;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.DigitalGreenCertificate;
import app.coronawarn.server.services.distribution.dgc.DigitalGreenCertificateToCborMapping;
import app.coronawarn.server.services.distribution.dgc.DigitalGreenCertificateToProtobufMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Reads configuration parameters from the respective files in the class path and build a {@link
 * DigitalGreenCertificateStructureProvider} with them.
 */
@Component
public class DigitalGreenCertificateStructureProvider {

  private static final Logger logger = LoggerFactory.getLogger(DigitalGreenCertificateStructureProvider.class);

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
   * Returns the publishable archive with the Digital Green Certificates protobuf structures for mobile clients.
   */
  public DirectoryOnDisk getDigitalGreenCertificates() {
    try {
      return constructArchiveToPublish(distributionServiceConfig.getDigitalGreenCertificate());
    } catch (DefaultValueSetsMissingException e) {
      logger.error("We don't generate a value-sets file and this shouldn't override existing ones.", e);
      return new DirectoryOnDisk("");
    }
  }

  private DirectoryOnDisk constructArchiveToPublish(DigitalGreenCertificate dgcConfig)
      throws DefaultValueSetsMissingException {
    DirectoryOnDisk dgcDirectory = new DirectoryOnDisk(dgcConfig.getDgcDirectory());
    for (String currentLanguage: dgcConfig.getSupportedLanguages()) {
      ArchiveOnDisk archiveToPublish = new ArchiveOnDisk(dgcConfig.getValuesetsFileName());
      archiveToPublish.addWritable(new FileOnDisk("export.bin",
          dgcToProtobufMapping.constructProtobufMapping().toByteArray()));
      DirectoryOnDisk languageDirectory = new DirectoryOnDisk(currentLanguage.toLowerCase());
      languageDirectory.addWritable(new DistributionArchiveSigningDecorator(
          archiveToPublish, cryptoProvider, distributionServiceConfig));
      dgcDirectory.addWritable(languageDirectory);
      logger.info("Writing digital green certificate to {}/{}/{}.", dgcDirectory.getName(), languageDirectory.getName(),
          archiveToPublish.getName());
    }

    ArchiveOnDisk onboardedCountries = new ArchiveOnDisk("onboarded-countries");
    onboardedCountries
        .addWritable(new FileOnDisk("export.bin", dgcToCborMapping.constructCountryList()));
    dgcDirectory.addWritable(new DistributionArchiveSigningDecorator(onboardedCountries, cryptoProvider,
        distributionServiceConfig));

    return dgcDirectory;
  }

  private Writable<WritableOnDisk> getOnboardedCountriesArchive() {
    ArchiveOnDisk onboardedCountries = new ArchiveOnDisk("onboarded-countries");
    onboardedCountries
        .addWritable(new FileOnDisk("export.bin", dgcToCborMapping.constructCountryList()));

    return new DistributionArchiveSigningDecorator(onboardedCountries, cryptoProvider,
        distributionServiceConfig);
  }
}
