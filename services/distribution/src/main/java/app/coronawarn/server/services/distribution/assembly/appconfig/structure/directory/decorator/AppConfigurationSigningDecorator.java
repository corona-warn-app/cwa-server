package app.coronawarn.server.services.distribution.assembly.appconfig.structure.directory.decorator;

import app.coronawarn.server.services.distribution.assembly.component.CryptoProvider;
import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.archive.Archive;
import app.coronawarn.server.services.distribution.assembly.structure.archive.decorator.signing.SigningDecoratorOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.file.FileOnDisk;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;

public class AppConfigurationSigningDecorator extends SigningDecoratorOnDisk {

  public AppConfigurationSigningDecorator(Archive<WritableOnDisk> archive, CryptoProvider cryptoProvider,
      DistributionServiceConfig distributionServiceConfig) {
    super(archive, cryptoProvider, distributionServiceConfig);
  }

  @Override
  public byte[] getBytesToSign() {
    Writable<?> archiveContent = this.getWritables().stream().findFirst().orElseThrow(
        () -> new RuntimeException("Archive must contain exactly one file for signing"));
    FileOnDisk fileToSign = (FileOnDisk) archiveContent;
    return fileToSign.getBytes();
  }

  @Override
  public int getBatchNum() {
    return 1;
  }

  @Override
  public int getBatchSize() {
    return 1;
  }
}
