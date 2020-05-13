package app.coronawarn.server.services.distribution.diagnosiskeys.structure.directory;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.services.distribution.crypto.CryptoProvider;
import app.coronawarn.server.services.distribution.diagnosiskeys.structure.directory.decorator.DateAggregatingDecorator;
import app.coronawarn.server.services.distribution.diagnosiskeys.structure.file.HourFileImpl;
import app.coronawarn.server.services.distribution.diagnosiskeys.util.DateTime;
import app.coronawarn.server.services.distribution.structure.directory.Directory;
import app.coronawarn.server.services.distribution.structure.directory.DirectoryImpl;
import app.coronawarn.server.services.distribution.structure.directory.IndexDirectory;
import app.coronawarn.server.services.distribution.structure.directory.IndexDirectoryImpl;
import app.coronawarn.server.services.distribution.structure.directory.decorator.IndexingDecorator;
import app.coronawarn.server.services.distribution.structure.file.File;
import app.coronawarn.server.services.distribution.structure.file.decorator.SigningDecorator;
import app.coronawarn.server.services.distribution.structure.functional.FileFunction;
import app.coronawarn.server.services.distribution.structure.functional.Formatter;
import app.coronawarn.server.services.distribution.structure.functional.IndexFunction;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;

public class DiagnosisKeysDirectoryImpl extends DirectoryImpl {

  private static final String DIAGNOSIS_KEYS_DIRECTORY = "diagnosis-keys";
  private static final String COUNTRY_DIRECTORY = "country";
  private static final String COUNTRY = "DE";
  private static final String DATE_DIRECTORY = "date";
  private static final String HOUR_DIRECTORY = "hour";
  private static final DateTimeFormatter ISO8601 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private final Collection<DiagnosisKey> diagnosisKeys;
  private final CryptoProvider cryptoProvider;

  public DiagnosisKeysDirectoryImpl(Collection<DiagnosisKey> diagnosisKeys,
      CryptoProvider cryptoProvider) {
    super(DIAGNOSIS_KEYS_DIRECTORY);
    this.diagnosisKeys = diagnosisKeys;
    this.cryptoProvider = cryptoProvider;
    this.addDirectory(createDirectoryStructure());
  }

  private Directory createDirectoryStructure() {
    IndexDirectory<String> countryDirectory = createCountryDirectory();

    IndexDirectory<LocalDate> dateDirectory = createDateDirectory();
    Directory dateDirectoryDecorated = decorateDateDirectory(dateDirectory);

    IndexDirectory<LocalDateTime> hourDirectory = createHourDirectory();
    Directory hourDirectoryDecorated = decorateHourDirectory(hourDirectory);

    countryDirectory.addDirectoryToAll(__ -> dateDirectoryDecorated);
    dateDirectory.addDirectoryToAll(__ -> hourDirectoryDecorated);

    return countryDirectory;
  }

  private IndexDirectory<String> createCountryDirectory() {
    return new IndexDirectoryImpl<>(COUNTRY_DIRECTORY, __ -> List.of(COUNTRY), Object::toString);
  }

  private IndexDirectory<LocalDate> createDateDirectory() {
    IndexFunction<LocalDate> dateDirectoryIndex = __ -> DateTime.getDates(diagnosisKeys);
    Formatter<LocalDate> dateDirectoryFormatter = ISO8601::format;
    return new IndexDirectoryImpl<>(DATE_DIRECTORY, dateDirectoryIndex, dateDirectoryFormatter);
  }

  private IndexDirectory<LocalDateTime> createHourDirectory() {
    IndexFunction<LocalDateTime> hourDirectoryIndex =
        indices -> DateTime.getHours(((LocalDate) indices.peek()), diagnosisKeys);
    Formatter<LocalDateTime> hourDirectoryFormatter = LocalDateTime::getHour;
    IndexDirectory<LocalDateTime> hourDirectory = new IndexDirectoryImpl<>(HOUR_DIRECTORY,
        hourDirectoryIndex, hourDirectoryFormatter);

    FileFunction hourFileFunction = getHourFileFunction();
    hourDirectory.addFileToAll(hourFileFunction);

    return hourDirectory;
  }

  private FileFunction getHourFileFunction() {
    return indices -> {
      LocalDateTime currentHour = (LocalDateTime) indices.pop();
      // The LocalDateTime currentHour already contains both the date and the hour information, so
      // we can throw away the LocalDate that's next in the stack from the "/date" IndexDirectory.
      indices.pop();
      String region = (String) indices.pop();

      File hourFile = new HourFileImpl(currentHour, region, diagnosisKeys);
      return new SigningDecorator(hourFile, cryptoProvider);
    };
  }

  private Directory decorateDateDirectory(IndexDirectory<LocalDate> dateDirectory) {
    Directory dateDirectoryIndexed = new IndexingDecorator<>(dateDirectory);
    return new DateAggregatingDecorator(dateDirectoryIndexed, cryptoProvider);
  }

  private Directory decorateHourDirectory(IndexDirectory<LocalDateTime> hourDirectory) {
    return new IndexingDecorator<>(hourDirectory);
  }
}
