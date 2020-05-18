package app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.directory;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.services.distribution.assembly.component.CryptoProvider;
import app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.file.HourFileImpl;
import app.coronawarn.server.services.distribution.assembly.diagnosiskeys.util.DateTime;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectoryImpl;
import app.coronawarn.server.services.distribution.assembly.structure.file.File;
import app.coronawarn.server.services.distribution.assembly.structure.file.decorator.SigningDecorator;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;

public class DiagnosisKeysHourDirectoryImpl extends IndexDirectoryImpl<LocalDateTime> {

  private static final String HOUR_DIRECTORY = "hour";

  private final Collection<DiagnosisKey> diagnosisKeys;
  private final LocalDate currentDate;
  private final CryptoProvider cryptoProvider;

  /**
   * Constructs a {@link DiagnosisKeysHourDirectoryImpl} instance for the specified date.
   *
   * @param diagnosisKeys  A collection of diagnosis keys. These will be filtered according to the specified current
   *                       date.
   * @param currentDate    The date that this {@link DiagnosisKeysHourDirectoryImpl} shall be associated with.
   * @param cryptoProvider The {@link CryptoProvider} used for cryptographic signing.
   */
  public DiagnosisKeysHourDirectoryImpl(Collection<DiagnosisKey> diagnosisKeys,
      LocalDate currentDate, CryptoProvider cryptoProvider) {
    super(HOUR_DIRECTORY, indices -> {
      return DateTime.getHours(((LocalDate) indices.peek()), diagnosisKeys);
    }, LocalDateTime::getHour);
    this.diagnosisKeys = diagnosisKeys;
    this.currentDate = currentDate;
    this.cryptoProvider = cryptoProvider;
  }

  @Override
  public void prepare(ImmutableStack<Object> indices) {
    this.addFileToAll(currentIndices -> {
      LocalDateTime currentHour = (LocalDateTime) currentIndices.peek();
      // The LocalDateTime currentHour already contains both the date and the hour information, so
      // we can throw away the LocalDate that's the second item on the stack from the "/date"
      // IndexDirectory.
      String region = (String) currentIndices.pop().pop().peek();
      return decorateHourFile(new HourFileImpl(currentHour, region, diagnosisKeys));
    });
    super.prepare(indices);
  }

  private File decorateHourFile(File hourFile) {
    return new SigningDecorator(hourFile, cryptoProvider);
  }
}
