package app.coronawarn.server.services.download;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class BatchDownloadExceptionTest {

  @Test
  void testMessageCreatedCorrectly() {
    LocalDate now = LocalDate.now();
    assertThat(new BatchDownloadException(now, null).getMessage())
        .isEqualTo("Downloading batch  for date " + now.format(ISO_LOCAL_DATE) + " failed. Reason: none given");
    assertThat(new BatchDownloadException("TAG", now, new IllegalStateException("Something wrong")).getMessage())
        .isEqualTo("Downloading batch TAG for date " + now.format(ISO_LOCAL_DATE) + " failed. Reason: Something wrong");
  }

}
