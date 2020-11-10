package app.coronawarn.server.services.distribution.assembly.appconfig.structure.directory.v2;

import app.coronawarn.server.services.distribution.assembly.appconfig.structure.archive.decorator.signing.AppConfigurationSigningDecorator;
import app.coronawarn.server.services.distribution.assembly.component.CryptoProvider;
import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.archive.ArchiveOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.file.FileOnDisk;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;

/**
 * Validates and provides all configuration files for a specific device which is running the ENF V2.
 */
public class AppConfigurationV2StructureProvider<T extends com.google.protobuf.GeneratedMessageV3> {

  private final T applicationConfiguration;
  private final CryptoProvider cryptoProvider;
  private final DistributionServiceConfig distributionServiceConfig;
  private final String appConfigFileName;

  /**
   * Creates an {@link AppConfigurationV2StructureProvider} for the exposure configuration and risk score
   * classification.
   *
   * @param cryptoProvider The {@link CryptoProvider} whose artifacts to use for creating the signature.
   */
  public AppConfigurationV2StructureProvider(T applicationConfiguration,
      CryptoProvider cryptoProvider,
      DistributionServiceConfig distributionServiceConfig,
      String appConfigFileName) {
    this.applicationConfiguration = applicationConfiguration;
    this.cryptoProvider = cryptoProvider;
    this.distributionServiceConfig = distributionServiceConfig;
    this.appConfigFileName = appConfigFileName;
  }

  /**
   * If validation of the given V2 app config (IOS or Android) succeeds, it is written into a file, put into an archive
   * with the specified name and returned to be included in the CWA file structure.
   */
  public Writable<WritableOnDisk> getConfigurationArchive() {
    ArchiveOnDisk appConfigurationFile = new ArchiveOnDisk(appConfigFileName);
    appConfigurationFile
        .addWritable(new FileOnDisk("export.bin", applicationConfiguration.toByteArray()));
    return new AppConfigurationSigningDecorator(appConfigurationFile, cryptoProvider,
        distributionServiceConfig);
  }
}
