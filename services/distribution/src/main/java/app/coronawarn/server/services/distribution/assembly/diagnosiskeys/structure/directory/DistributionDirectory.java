

package app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.directory;

import app.coronawarn.server.services.distribution.assembly.common.DistributionPackagesBundler;
import app.coronawarn.server.services.distribution.assembly.component.CryptoProvider;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.decorator.indexing.IndexingDecoratorOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;

/**
 * A {@link Directory} containing the file and directory structure that mirrors the API defined in the OpenAPI
 * definition {@code /services/distribution/api_v1.json}. Available countries (endpoint {@code
 * /version/v1/diagnosis-keys/country}) are statically set to only {@code "DE"}. The dates and respective hours
 * (endpoint {@code /version/v1/diagnosis-keys/country/DE/date}) will be created based on the actual data
 * given to the {@link DistributionDirectory#DistributionDirectory constructor}.
 */
public class DistributionDirectory extends DirectoryOnDisk {

  private final DistributionPackagesBundler distributionPackagesBundler;
  private final CryptoProvider cryptoProvider;
  private final DistributionServiceConfig distributionServiceConfig;

  /**
   * Constructs a {@link DistributionPackagesBundler} based on the specified data collection. Cryptographic
   * signing is performed using the specified {@link CryptoProvider}.
   *
   * @param distributionPackagesBundler A {@link DistributionPackagesBundler} containing the data.
   * @param cryptoProvider      The {@link CryptoProvider} used for payload signing.
   * @param distributionServiceConfig The {@link DistributionServiceConfig} config attributes
   */
  public DistributionDirectory(DistributionPackagesBundler distributionPackagesBundler, CryptoProvider cryptoProvider,
      DistributionServiceConfig distributionServiceConfig) {
    super(distributionPackagesBundler.getPath());
    this.distributionPackagesBundler = distributionPackagesBundler;
    this.cryptoProvider = cryptoProvider;
    this.distributionServiceConfig = distributionServiceConfig;
  }

  @Override
  public void prepare(ImmutableStack<Object> indices) {
    this.addWritable(decorateCountryDirectory(
        new DistributionCountryDirectory(distributionPackagesBundler, cryptoProvider, distributionServiceConfig)));
    super.prepare(indices);
  }

  private IndexDirectory<String, WritableOnDisk> decorateCountryDirectory(
      IndexDirectoryOnDisk<String> countryDirectory) {
    return new IndexingDecoratorOnDisk<>(countryDirectory, distributionServiceConfig.getOutputFileName());
  }
}
