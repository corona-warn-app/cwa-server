package app.coronawarn.server.tools.testdatagenerator.structure.cwa.diagnosiskeys;

import app.coronawarn.server.tools.testdatagenerator.structure.Directory;
import app.coronawarn.server.tools.testdatagenerator.structure.IndexDirectory;
import app.coronawarn.server.tools.testdatagenerator.util.Crypto;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.apache.commons.math3.random.RandomGenerator;

public class DiagnosisKeysDirectory extends Directory {

  public DiagnosisKeysDirectory(LocalDate startDate, int totalHours, int exposuresPerHour,
      String region, DateTimeFormatter formatter, Crypto crypto, RandomGenerator random) {
    super("diagnosis-keys");
    this.addDirectory(new IndexDirectory<>("country", __ -> List.of(region))
        .addDirectoryToAll(
            new DateDirectory(startDate, totalHours, exposuresPerHour, formatter, crypto, random))
    );
  }
}
