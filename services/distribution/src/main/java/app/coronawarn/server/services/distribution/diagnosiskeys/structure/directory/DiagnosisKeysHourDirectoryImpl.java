package app.coronawarn.server.services.distribution.diagnosiskeys.structure.directory;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.services.distribution.crypto.CryptoProvider;
import app.coronawarn.server.services.distribution.diagnosiskeys.structure.file.HourFileImpl;
import app.coronawarn.server.services.distribution.diagnosiskeys.util.DateTime;
import app.coronawarn.server.services.distribution.structure.directory.IndexDirectoryImpl;
import app.coronawarn.server.services.distribution.structure.file.File;
import app.coronawarn.server.services.distribution.structure.file.decorator.SigningDecorator;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Stack;

public class DiagnosisKeysHourDirectoryImpl extends IndexDirectoryImpl<LocalDateTime> {

  private static final String HOUR_DIRECTORY = "hour";

  private Collection<DiagnosisKey> diagnosisKeys;
  private LocalDate currentDate;
  private CryptoProvider cryptoProvider;

  /**
   * TODO Doc.
   */
  public DiagnosisKeysHourDirectoryImpl(Collection<DiagnosisKey> diagnosisKeys,
      LocalDate currentDate, CryptoProvider cryptoProvider) {
    super(HOUR_DIRECTORY, indices -> DateTime.getHours(((LocalDate) indices.peek()), diagnosisKeys),
        LocalDateTime::getHour);
    this.diagnosisKeys = diagnosisKeys;
    this.currentDate = currentDate;
    this.cryptoProvider = cryptoProvider;
  }

  @Override
  public void prepare(Stack<Object> indices) {
    this.addFileToAll(__ -> {
      LocalDateTime currentHour = (LocalDateTime) indices.pop();
      // The LocalDateTime currentHour already contains both the date and the hour information, so
      // we can throw away the LocalDate that's next in the stack from the "/date" IndexDirectory.
      indices.pop();
      String region = (String) indices.pop();
      return decorateHourFile(new HourFileImpl(currentHour, region, diagnosisKeys));
    });
    super.prepare(indices);
  }

  private File decorateHourFile(File hourFile) {
    return new SigningDecorator(hourFile, cryptoProvider);
  }
}
