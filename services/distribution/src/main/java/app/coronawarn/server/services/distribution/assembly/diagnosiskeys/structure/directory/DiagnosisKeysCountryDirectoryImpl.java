package app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.directory;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.services.distribution.assembly.component.CryptoProvider;
import app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.directory.decorator.DateAggregatingDecorator;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectoryImpl;
import app.coronawarn.server.services.distribution.assembly.structure.directory.decorator.IndexingDecorator;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;

public class DiagnosisKeysCountryDirectoryImpl extends IndexDirectoryImpl<String> {

  private static final String COUNTRY_DIRECTORY = "country";
  private static final String COUNTRY = "DE";

  private final Collection<DiagnosisKey> diagnosisKeys;
  private final CryptoProvider cryptoProvider;

  /**
   * Constructs a {@link DiagnosisKeysCountryDirectoryImpl} instance that represents the {@code
   * .../country/:country/...} portion of the diagnosis key directory structure.
   *
   * @param diagnosisKeys  The diagnosis keys processed in the contained sub directories.
   * @param cryptoProvider The {@link CryptoProvider} used for payload signing.
   */
  public DiagnosisKeysCountryDirectoryImpl(Collection<DiagnosisKey> diagnosisKeys,
      CryptoProvider cryptoProvider) {
    super(COUNTRY_DIRECTORY, __ -> Set.of(COUNTRY), Object::toString);
    this.diagnosisKeys = diagnosisKeys;
    this.cryptoProvider = cryptoProvider;
  }

  @Override
  public void prepare(ImmutableStack<Object> indices) {
    this.addDirectoryToAll(__ -> {
      IndexDirectory<LocalDate> dateDirectory = new DiagnosisKeysDateDirectoryImpl(diagnosisKeys,
          cryptoProvider);
      return decorateDateDirectory(dateDirectory);
    });
    super.prepare(indices);
  }

  private Directory decorateDateDirectory(IndexDirectory<LocalDate> dateDirectory) {
    return new DateAggregatingDecorator(new IndexingDecorator<>(dateDirectory), cryptoProvider);
  }
}
