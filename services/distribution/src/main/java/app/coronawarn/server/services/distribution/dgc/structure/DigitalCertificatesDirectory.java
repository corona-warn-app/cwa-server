package app.coronawarn.server.services.distribution.dgc.structure;

import app.coronawarn.server.common.protocols.internal.dgc.ValueSets;
import app.coronawarn.server.common.shared.collection.ImmutableStack;
import app.coronawarn.server.services.distribution.assembly.component.CryptoProvider;
import app.coronawarn.server.services.distribution.assembly.structure.archive.ArchiveOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.archive.decorator.signing.DistributionArchiveSigningDecorator;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.file.FileOnDisk;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.DigitalGreenCertificate;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public class DigitalCertificatesDirectory extends IndexDirectoryOnDisk<String> {

  public static final String EXPORT_BIN = "export.bin";

  private final DistributionServiceConfig distributionServiceConfig;

  private final DigitalGreenCertificate dgcConfig;

  private final CryptoProvider cryptoProvider;

  private ValueSets valueSets;

  /**
   * Constructs a {@link IndexDirectoryOnDisk} instance that represents a directory, containing an index in the form of
   * sub directories.
   */
  public DigitalCertificatesDirectory(DistributionServiceConfig distributionServiceConfig,
      DigitalGreenCertificate dgcConfig, CryptoProvider cryptoProvider) {
    super(dgcConfig.getDgcDirectory(),
        ignoredValue -> Arrays.stream(dgcConfig.getSupportedLanguages())
            .map(String::toLowerCase)
            .collect(Collectors.toSet()),
        Object::toString);
    this.distributionServiceConfig = distributionServiceConfig;
    this.dgcConfig = dgcConfig;
    this.cryptoProvider = cryptoProvider;
  }

  @Override
  public void prepare(ImmutableStack<Object> indices) {
    if (valueSets != null) {
      this.addWritableToAll(ignoredValue -> {
        ArchiveOnDisk archiveToPublish = new ArchiveOnDisk(dgcConfig.getValuesetsFileName());
        archiveToPublish.addWritable(new FileOnDisk(EXPORT_BIN, valueSets.toByteArray()));
        return Optional.of(new DistributionArchiveSigningDecorator(
            archiveToPublish, cryptoProvider, distributionServiceConfig));
      });
    }
    super.prepare(indices);
  }

  public void addValueSetsToAll(ValueSets valueSets) {
    this.valueSets = valueSets;
  }
}
