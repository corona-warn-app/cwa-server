package app.coronawarn.server.services.distribution.assembly.appconfig.structure.directory.v2;

import app.coronawarn.server.services.distribution.assembly.appconfig.structure.archive.decorator.signing.AppConfigurationSigningDecorator;
import app.coronawarn.server.services.distribution.assembly.component.CryptoProvider;
import app.coronawarn.server.services.distribution.assembly.structure.archive.ArchiveOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.file.FileOnDisk;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates the directory structure {@code /version/v1/<config>} and writes the app configuration parameters
 * into a zip file.
 */
public class AppConfigurationV2Directory<T extends com.google.protobuf.GeneratedMessageV3> extends DirectoryOnDisk {

  private static final Logger logger = LoggerFactory.getLogger(AppConfigurationV2Directory.class);

  private final T applicationConfiguration;
  private final CryptoProvider cryptoProvider;
  private final DistributionServiceConfig distributionServiceConfig;

  /**
   * Creates an {@link AppConfigurationV2Directory} for the exposure configuration and risk score
   * classification.
   *
   * @param cryptoProvider The {@link CryptoProvider} whose artifacts to use for creating the
   *        signature.
   */
  public AppConfigurationV2Directory(T applicationConfiguration,
      CryptoProvider cryptoProvider,
      DistributionServiceConfig distributionServiceConfig,
      String appConfigFileName) {
    super(distributionServiceConfig.getApi().getParametersPath());
    this.applicationConfiguration = applicationConfiguration;
    this.cryptoProvider = cryptoProvider;
    this.distributionServiceConfig = distributionServiceConfig;

    addConfigurationArchiveIfValid(appConfigFileName);
  }

  /**
   * If validation of the given V2 app config (IOS or Android) succeeds, it is written into a file,
   * put into an archive with the specified name and added to the specified parent directory.
   */
  private void addConfigurationArchiveIfValid(String archiveName) {
    //TODO : Validation
    /*ConfigurationValidator validator = new ApplicationConfigurationIosValidator(applicationConfiguration);
    ValidationResult validationResult = validator.validate();

    if (validationResult.hasErrors()) {
      logger.error("App configuration file creation failed. Validation failed for {}, {}",
          archiveName, validationResult);
      return;
    }*/

    ArchiveOnDisk appConfigurationFile = new ArchiveOnDisk(archiveName);
    appConfigurationFile.addWritable(new FileOnDisk("export.bin", applicationConfiguration.toByteArray()));
    ArchiveOnDisk countryAppConfigurationFile = new ArchiveOnDisk(archiveName);
    countryAppConfigurationFile.addWritable(new FileOnDisk("export.bin", applicationConfiguration.toByteArray()));
    this.addWritable(
        new AppConfigurationSigningDecorator(countryAppConfigurationFile, cryptoProvider, distributionServiceConfig));
    this.addWritable(
        new AppConfigurationSigningDecorator(appConfigurationFile, cryptoProvider, distributionServiceConfig));
  }
}
