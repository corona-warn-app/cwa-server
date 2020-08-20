package app.coronawarn.server.services.federation.download.download;

import java.time.LocalDate;
import java.util.Date;

public interface DiagnosisKeyBatchDownloaders {

  DiagnosisKeyBatchContainer downloadBatch(LocalDate date);

  DiagnosisKeyBatchContainer downloadBatch(LocalDate date, String batchTag);

}
