package app.coronawarn.server.services.distribution.diagnosiskeys.structure;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.services.distribution.crypto.CryptoProvider;
import app.coronawarn.server.services.distribution.diagnosiskeys.util.DateTime;
import app.coronawarn.server.services.distribution.structure.directory.IndexDirectoryImpl;
import app.coronawarn.server.services.distribution.structure.directory.decorator.IndexingDecorator;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;

class DateDirectoryImpl extends IndexDirectoryImpl<LocalDate> {

  public DateDirectoryImpl(Collection<DiagnosisKey> diagnosisKeys, DateTimeFormatter formatter,
      CryptoProvider cryptoProvider) {
    super("date", __ -> DateTime.getDates(diagnosisKeys),
        formatter::format);
    this.addDirectoryToAll(__ -> new IndexingDecorator<>(
        new HourDirectoryImpl(diagnosisKeys, cryptoProvider)));
  }
}
