package app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.directory;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.services.distribution.assembly.component.CryptoProvider;
import app.coronawarn.server.services.distribution.assembly.diagnosiskeys.util.DateTime;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectoryImpl;
import app.coronawarn.server.services.distribution.assembly.structure.directory.decorator.IndexingDecorator;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;

public class DiagnosisKeysDateDirectoryImpl extends IndexDirectoryImpl<LocalDate> {

  private static final String DATE_DIRECTORY = "date";
  private static final DateTimeFormatter ISO8601 = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  private final Collection<DiagnosisKey> diagnosisKeys;
  private final CryptoProvider cryptoProvider;

  public DiagnosisKeysDateDirectoryImpl(Collection<DiagnosisKey> diagnosisKeys,
      CryptoProvider cryptoProvider) {
    super(DATE_DIRECTORY, __ -> DateTime.getDates(diagnosisKeys), ISO8601::format);
    this.cryptoProvider = cryptoProvider;
    this.diagnosisKeys = diagnosisKeys;
  }

  @Override
  public void prepare(ImmutableStack<Object> indices) {
    this.addDirectoryToAll(currentIndices -> {
      LocalDate currentDate = (LocalDate) currentIndices.peek();
      IndexDirectory<LocalDateTime> hourDirectory = new DiagnosisKeysHourDirectoryImpl(
          diagnosisKeys, currentDate, cryptoProvider);
      return decorateHourDirectory(hourDirectory);
    });
    super.prepare(indices);
  }

  private Directory decorateHourDirectory(IndexDirectory<LocalDateTime> hourDirectory) {
    return new IndexingDecorator<>(hourDirectory);
  }
}
