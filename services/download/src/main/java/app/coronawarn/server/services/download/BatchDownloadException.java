package app.coronawarn.server.services.download;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;

import java.time.LocalDate;

public class BatchDownloadException extends Exception {

  private static final long serialVersionUID = 1L;

  public BatchDownloadException(LocalDate date, Throwable cause) {
    this(null, date, cause);
  }

  public BatchDownloadException(String batchTag, LocalDate date, Throwable cause) {
    super("Downloading batch " + (batchTag != null ? batchTag : "") + " for date " + date.format(ISO_LOCAL_DATE)
        + " failed. Reason: " + (cause != null ? cause.getMessage() : "none given"), cause);
  }
}
