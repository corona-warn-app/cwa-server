package app.coronawarn.server.services.distribution.diagnosiskeys.structure;

import app.coronawarn.server.services.distribution.crypto.CryptoProvider;
import app.coronawarn.server.services.distribution.diagnosiskeys.structure.directory.decorator.DateAggregatingDecorator;
import app.coronawarn.server.services.distribution.structure.directory.DirectoryImpl;
import app.coronawarn.server.services.distribution.structure.directory.IndexDirectoryImpl;
import app.coronawarn.server.services.distribution.structure.directory.decorator.IndexingDecorator;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.apache.commons.math3.random.RandomGenerator;

public class DiagnosisKeysDirectoryImpl extends DirectoryImpl {

  public DiagnosisKeysDirectoryImpl(LocalDate startDate, int totalHours, int exposuresPerHour,
      String region, DateTimeFormatter formatter, RandomGenerator random,
      CryptoProvider cryptoProvider) {
    super("diagnosis-keys");
    IndexDirectoryImpl<String> country = new IndexDirectoryImpl<>("country", __ -> List.of(region),
        Object::toString);
    country.addDirectoryToAll(__ -> new DateAggregatingDecorator(new IndexingDecorator<>(
        new DateDirectoryImpl(startDate, totalHours, exposuresPerHour, formatter, random,
            cryptoProvider)), cryptoProvider));
    this.addDirectory(new IndexingDecorator<>(country));
  }
}
