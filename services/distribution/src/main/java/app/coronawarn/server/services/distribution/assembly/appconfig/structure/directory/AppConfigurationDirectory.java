

package app.coronawarn.server.services.distribution.assembly.appconfig.structure.directory;

import app.coronawarn.server.common.protocols.internal.ApplicationConfiguration;
import app.coronawarn.server.services.distribution.assembly.appconfig.structure.archive.decorator.signing.AppConfigurationSigningDecorator;
import app.coronawarn.server.services.distribution.assembly.appconfig.validation.ApplicationConfigurationValidator;
import app.coronawarn.server.services.distribution.assembly.appconfig.validation.ConfigurationValidator;
import app.coronawarn.server.services.distribution.assembly.appconfig.validation.ValidationResult;
import app.coronawarn.server.services.distribution.assembly.component.CryptoProvider;
import app.coronawarn.server.services.distribution.assembly.structure.archive.ArchiveOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.decorator.indexing.IndexingDecoratorOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.file.FileOnDisk;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates the directory structure {@code /configuration/country/:country} and writes the app configuration parameters
 * into a zip file.
 */
public class AppConfigurationDirectory extends DirectoryOnDisk {

  private static final Logger logger = LoggerFactory.getLogger(AppConfigurationDirectory.class);

  private final IndexDirectoryOnDisk<String> countryDirectory;
  private final ApplicationConfiguration applicationConfiguration;
  private final CryptoProvider cryptoProvider;
  private final DistributionServiceConfig distributionServiceConfig;

  /**
   * Creates an {@link AppConfigurationDirectory} for the exposure configuration and risk score classification.
   *
   * @param cryptoProvider The {@link CryptoProvider} whose artifacts to use for creating the signature.
   */
  public AppConfigurationDirectory(ApplicationConfiguration applicationConfiguration, CryptoProvider cryptoProvider,
      DistributionServiceConfig distributionServiceConfig) {
    super(distributionServiceConfig.getApi().getParametersPath());
    this.applicationConfiguration = applicationConfiguration;
    this.cryptoProvider = cryptoProvider;
    this.distributionServiceConfig = distributionServiceConfig;

    countryDirectory = new IndexDirectoryOnDisk<>(distributionServiceConfig.getApi().getCountryPath(),
        ignoredValue -> Set.of(distributionServiceConfig.getApi().getOriginCountry()), Object::toString);

    addConfigurationArchiveIfValid(distributionServiceConfig.getApi().getAppConfigFileName());

    this.addWritable(new IndexingDecoratorOnDisk<>(countryDirectory, distributionServiceConfig.getOutputFileName()));
  }

  /**
   * If validation of the {@link ApplicationConfiguration} succeeds, it is written into a file, put into an archive with
   * the specified name and added to the specified parent directory.
   */
  private void addConfigurationArchiveIfValid(String archiveName) {
    ConfigurationValidator validator = new ApplicationConfigurationValidator(applicationConfiguration);
    ValidationResult validationResult = validator.validate();

    if (validationResult.hasErrors()) {
      logger.error("App configuration file creation failed. Validation failed for {}, {}",
          archiveName, validationResult);
      return;
    }

    ArchiveOnDisk appConfigurationFile = new ArchiveOnDisk(archiveName);
    appConfigurationFile.addWritable(new FileOnDisk("export.bin", applicationConfiguration.toByteArray()));
    countryDirectory.addWritableToAll(ignoredValue ->
        Optional
            .of(new AppConfigurationSigningDecorator(appConfigurationFile, cryptoProvider, distributionServiceConfig)));
  }
}
