package app.coronawarn.server.tools.testdatagenerator.implementations.cwa;

import app.coronawarn.server.tools.testdatagenerator.decorators.directory.IndexingDecorator;
import app.coronawarn.server.tools.testdatagenerator.implementations.IndexDirectoryImpl;
import app.coronawarn.server.tools.testdatagenerator.util.Crypto;
import app.coronawarn.server.tools.testdatagenerator.util.Maths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.apache.commons.math3.random.RandomGenerator;

class DateDirectoryImpl extends IndexDirectoryImpl<LocalDate> {

  public DateDirectoryImpl(LocalDate startDate, int totalHours, int exposuresPerHour,
      DateTimeFormatter formatter, Crypto crypto, RandomGenerator random) {
    super("date", __ -> Maths.getDates(startDate, Maths.getNumberOfDays(totalHours)),
        formatter::format);

    this.addDirectoryToAll(__ -> new IndexingDecorator<>(
        new HourDirectoryImpl(startDate, totalHours, exposuresPerHour, crypto, random)));
  }
}
