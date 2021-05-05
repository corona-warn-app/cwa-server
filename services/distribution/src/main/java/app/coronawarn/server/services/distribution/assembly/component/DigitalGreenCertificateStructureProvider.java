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
import app.coronawarn.server.services.distribution.runner.Assembly;
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
  public DirectoryOnDisk getDigitalGreenCertificates() {
    return constructArchiveToPublish(distributionServiceConfig.getDigitalGreenCertificate(),
        dgcToProtobufMapping.constructProtobufMapping());
  }

  private <T extends com.google.protobuf.GeneratedMessageV3> DirectoryOnDisk constructArchiveToPublish(
      DigitalGreenCertificate dgcConfig, ValueSets dgcProto) {
    ArchiveOnDisk archiveToPublish = new ArchiveOnDisk(distributionServiceConfig.getOutputFileName());
    archiveToPublish.addWritable(new FileOnDisk("export.bin", dgcProto.toByteArray()));
    DirectoryOnDisk enDirectory = new DirectoryOnDisk("en");
    enDirectory.addWritable(new DistributionArchiveSigningDecorator(
        archiveToPublish, cryptoProvider, distributionServiceConfig));
    DirectoryOnDisk valuesetsDirectory = new DirectoryOnDisk(dgcConfig.getValuesetsDirectory());
    valuesetsDirectory.addWritable(enDirectory);
    DirectoryOnDisk dgcDirectory = new DirectoryOnDisk(dgcConfig.getDgcDirectory());
    dgcDirectory.addWritable(valuesetsDirectory);
    logger.info("Writing digital green certificate to {}/{}/en/{}.", dgcDirectory.getName(),
        valuesetsDirectory.getName(), archiveToPublish.getName());
    return dgcDirectory;
  }
}
