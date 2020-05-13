package app.coronawarn.server.services.distribution.diagnosiskeys.structure;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.services.distribution.crypto.CryptoProvider;
import app.coronawarn.server.services.distribution.diagnosiskeys.structure.directory.decorator.DateAggregatingDecorator;
import app.coronawarn.server.services.distribution.structure.directory.DirectoryImpl;
import app.coronawarn.server.services.distribution.structure.directory.IndexDirectoryImpl;
import app.coronawarn.server.services.distribution.structure.directory.decorator.IndexingDecorator;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;

public class DiagnosisKeysDirectoryImpl extends DirectoryImpl {

  private static final String DIAGNOSIS_KEYS_DIRECTORY = "diagnosis-keys";

  private static final String COUNTRY_DIRECTORY = "country";

  public DiagnosisKeysDirectoryImpl(Collection<DiagnosisKey> diagnosisKeys, String region,
      DateTimeFormatter formatter, CryptoProvider cryptoProvider) {
    super(DIAGNOSIS_KEYS_DIRECTORY);
    IndexDirectoryImpl<String> country = new IndexDirectoryImpl<>(COUNTRY_DIRECTORY, __ -> List.of(region),
        Object::toString);
    country.addDirectoryToAll(__ -> new DateAggregatingDecorator(new IndexingDecorator<>(
        new DateDirectoryImpl(diagnosisKeys, formatter, cryptoProvider)),
        cryptoProvider));
    this.addDirectory(new IndexingDecorator<>(country));
  }
}
