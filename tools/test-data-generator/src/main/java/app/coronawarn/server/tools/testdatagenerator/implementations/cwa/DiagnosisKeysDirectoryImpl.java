package app.coronawarn.server.tools.testdatagenerator.implementations.cwa;

import app.coronawarn.server.services.distribution.crypto.Crypto;
import app.coronawarn.server.services.distribution.structure.directory.DirectoryImpl;
import app.coronawarn.server.services.distribution.structure.directory.IndexDirectoryImpl;
import app.coronawarn.server.services.distribution.structure.directory.decorator.IndexingDecorator;
import app.coronawarn.server.tools.testdatagenerator.decorators.directory.DateAggregatingDecorator;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.apache.commons.math3.random.RandomGenerator;

public class DiagnosisKeysDirectoryImpl extends DirectoryImpl {

  public DiagnosisKeysDirectoryImpl(LocalDate startDate, int totalHours, int exposuresPerHour,
      String region, DateTimeFormatter formatter, RandomGenerator random) {
    super("diagnosis-keys");
    IndexDirectoryImpl<String> country = new IndexDirectoryImpl<>("country", __ -> List.of(region));
    country.addDirectoryToAll(__ -> new DateAggregatingDecorator(new IndexingDecorator<>(
        new DateDirectoryImpl(startDate, totalHours, exposuresPerHour, formatter, random))));
    this.addDirectory(new IndexingDecorator<>(country));
  }
}
