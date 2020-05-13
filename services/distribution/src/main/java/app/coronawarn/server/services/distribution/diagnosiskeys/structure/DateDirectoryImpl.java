package app.coronawarn.server.services.distribution.diagnosiskeys.structure;

import app.coronawarn.server.services.distribution.crypto.CryptoProvider;
import app.coronawarn.server.services.distribution.diagnosiskeys.util.DateTime;
import app.coronawarn.server.services.distribution.structure.directory.IndexDirectoryImpl;
import app.coronawarn.server.services.distribution.structure.directory.decorator.IndexingDecorator;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.apache.commons.math3.random.RandomGenerator;

class DateDirectoryImpl extends IndexDirectoryImpl<LocalDate> {

  public DateDirectoryImpl(LocalDate startDate, int totalHours, int exposuresPerHour,
      DateTimeFormatter formatter, RandomGenerator random, CryptoProvider cryptoProvider) {
    super("date", __ -> DateTime.getDates(startDate, DateTime.getNumberOfDays(totalHours)),
        formatter::format);
    this.addDirectoryToAll(__ -> new IndexingDecorator<>(
        new HourDirectoryImpl(startDate, totalHours, exposuresPerHour, random, cryptoProvider)));
  }
}
