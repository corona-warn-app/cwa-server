

package app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.directory;

import app.coronawarn.server.services.distribution.assembly.common.DistributionPackagesBundler;
import app.coronawarn.server.services.distribution.assembly.component.CryptoProvider;
import app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.directory.decorator.DateIndexingDecorator;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

public class DistributionCountryDirectory extends IndexDirectoryOnDisk<String> {

  private final DistributionPackagesBundler distributionPackagesBundler;
  private final CryptoProvider cryptoProvider;
  private final DistributionServiceConfig distributionServiceConfig;

  /**
   * Constructs a {@link DistributionCountryDirectory} instance that represents the {@code .../country/:country/...}
   * portion of the diagnosis key directory structure.
   *
   * @param distributionPackagesBundler A {@link DistributionPackagesBundler} containing the data to package.
   * @param cryptoProvider      The {@link CryptoProvider} used for payload signing.
   * @param distributionServiceConfig The {@link DistributionServiceConfig} config attributes
   */
  public DistributionCountryDirectory(DistributionPackagesBundler distributionPackagesBundler,
      CryptoProvider cryptoProvider, DistributionServiceConfig distributionServiceConfig) {
    super(distributionServiceConfig.getApi().getCountryPath(), ignoredValue -> Set
            .of(distributionServiceConfig.getApi().getOriginCountry(), distributionServiceConfig.getEuPackageName()),
        Object::toString);
    this.distributionPackagesBundler = distributionPackagesBundler;
    this.cryptoProvider = cryptoProvider;
    this.distributionServiceConfig = distributionServiceConfig;
  }

  @Override
  public void prepare(ImmutableStack<Object> indices) {
    this.addWritableToAll(ignoredValue -> Optional.of(decorateDateDirectory(
        new DiagnosisKeysDateDirectory(distributionPackagesBundler, cryptoProvider, distributionServiceConfig))));
    super.prepare(indices);
  }

  private IndexDirectory<LocalDate, WritableOnDisk> decorateDateDirectory(DiagnosisKeysDateDirectory dateDirectory) {
    return new DateIndexingDecorator(dateDirectory, distributionServiceConfig);
  }
}
