

package app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.directory;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.shared.collection.ImmutableStack;
import app.coronawarn.server.services.distribution.assembly.component.CryptoProvider;
import app.coronawarn.server.services.distribution.assembly.diagnosiskeys.DiagnosisKeyBundler;
import app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.directory.decorator.DateIndexingDecorator;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectoryOnDisk;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

public class DiagnosisKeysCountryDirectory extends IndexDirectoryOnDisk<String> {

  private final DiagnosisKeyBundler diagnosisKeyBundler;
  private final CryptoProvider cryptoProvider;
  private final DistributionServiceConfig distributionServiceConfig;

  /**
   * Constructs a {@link DiagnosisKeysCountryDirectory} instance that represents the {@code .../country/:country/...}
   * portion of the diagnosis key directory structure.
   *
   * @param diagnosisKeyBundler A {@link DiagnosisKeyBundler} containing the {@link DiagnosisKey DiagnosisKeys}.
   * @param cryptoProvider      The {@link CryptoProvider} used for payload signing.
   * @param distributionServiceConfig The {@link DistributionServiceConfig} config attributes
   */
  public DiagnosisKeysCountryDirectory(DiagnosisKeyBundler diagnosisKeyBundler,
      CryptoProvider cryptoProvider, DistributionServiceConfig distributionServiceConfig) {
    super(distributionServiceConfig.getApi().getCountryPath(), ignoredValue -> Set
            .of(distributionServiceConfig.getApi().getOriginCountry(), distributionServiceConfig.getEuPackageName()),
        Object::toString);
    this.diagnosisKeyBundler = diagnosisKeyBundler;
    this.cryptoProvider = cryptoProvider;
    this.distributionServiceConfig = distributionServiceConfig;
  }

  @Override
  public void prepare(ImmutableStack<Object> indices) {
    this.addWritableToAll(ignoredValue -> Optional.of(decorateDateDirectory(
        new DiagnosisKeysDateDirectory(diagnosisKeyBundler, cryptoProvider, distributionServiceConfig))));
    super.prepare(indices);
  }

  private IndexDirectory<LocalDate, WritableOnDisk> decorateDateDirectory(DiagnosisKeysDateDirectory dateDirectory) {
    return new DateIndexingDecorator(dateDirectory, distributionServiceConfig);
  }
}
