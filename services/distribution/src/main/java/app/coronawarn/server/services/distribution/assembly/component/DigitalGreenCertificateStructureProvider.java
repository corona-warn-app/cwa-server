package app.coronawarn.server.services.distribution.assembly.component;

import app.coronawarn.server.common.protocols.internal.dgc.ValueSets;
import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.archive.ArchiveOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.archive.decorator.signing.DistributionArchiveSigningDecorator;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.file.FileOnDisk;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.DigitalGreenCertificate;
import app.coronawarn.server.services.distribution.dgc.DigitalGreenCertificateToProtobufMapping;
import org.springframework.stereotype.Component;

/**
 * Reads configuration parameters from the respective files in the class path and build a {@link
 * DigitalGreenCertificateStructureProvider} with them.
 */
@Component
public class DigitalGreenCertificateStructureProvider {

  private final DistributionServiceConfig distributionServiceConfig;
  private final CryptoProvider cryptoProvider;
  private final DigitalGreenCertificateToProtobufMapping dgcToProtobufMapping;

  /**
   * Create an instance.
   */
  public DigitalGreenCertificateStructureProvider(DistributionServiceConfig distributionServiceConfig,
      CryptoProvider cryptoProvider, DigitalGreenCertificateToProtobufMapping dgcToProtobufMapping) {
    this.distributionServiceConfig = distributionServiceConfig;
    this.cryptoProvider = cryptoProvider;
    this.dgcToProtobufMapping = dgcToProtobufMapping;
  }

  /**
   * Returns the publishable archive with the Digital Green Certificates protobuf structure for mobile clients.
   */
  public Writable<WritableOnDisk> getDigitalGreenCertificates() {
    return constructArchiveToPublish(distributionServiceConfig.getDigitalGreenCertificate(),
        dgcToProtobufMapping.constructProtobufMapping());
  }

  private <T extends com.google.protobuf.GeneratedMessageV3> Writable<WritableOnDisk> constructArchiveToPublish(
      DigitalGreenCertificate dgcConfig, ValueSets dgcProto) {
    ArchiveOnDisk archiveToPublish = new ArchiveOnDisk(distributionServiceConfig.getOutputFileName());
    archiveToPublish.addWritable(new FileOnDisk("export.bin", dgcProto.toByteArray()));
    DirectoryOnDisk valuesetsDirectory = new DirectoryOnDisk(dgcConfig.getValuesetsDirectory());
    valuesetsDirectory.addWritable(new DistributionArchiveSigningDecorator(
        archiveToPublish, cryptoProvider, distributionServiceConfig));
    DirectoryOnDisk dgcDirectory = new DirectoryOnDisk(dgcConfig.getDgcDirectory());
    dgcDirectory.addWritable(valuesetsDirectory);
    return dgcDirectory;
  }
}
