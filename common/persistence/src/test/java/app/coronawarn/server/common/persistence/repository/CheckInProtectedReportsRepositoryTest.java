package app.coronawarn.server.common.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.common.persistence.domain.CheckInProtectedReports;
import app.coronawarn.server.common.persistence.service.utils.checkins.CheckinsDateSpecification;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;

@DataJdbcTest
class CheckInProtectedReportsRepositoryTest {

  @Autowired
  private CheckInProtectedReportsRepository underTest;

  @Test
  void shouldPersistEncryptedCheckin() {
    final byte[] guid = UUID.randomUUID().toString().getBytes();
    final long submissionTimestamp =
        CheckinsDateSpecification.HOUR_SINCE_EPOCH_DERIVATION.apply(Instant.now().getEpochSecond());
    CheckInProtectedReports checkInProtectedReports = new CheckInProtectedReports(guid, guid, guid, guid,
        submissionTimestamp);
    underTest.save(checkInProtectedReports);

    final Iterable<CheckInProtectedReports> all = underTest.findAll();
    final CheckInProtectedReports next = all.iterator().next();

    assertThat(next).isNotNull();
    assertThat(next.getId()).isNotNull();
    assertThat(next.getTraceLocationIdHash()).isEqualTo(guid);
    assertThat(next.getInitializationVector()).isEqualTo(guid);
    assertThat(next.getEncryptedCheckInRecord()).isEqualTo(guid);
    assertThat(next.getMac()).isEqualTo(guid);
    assertThat(next.getSubmissionTimestamp()).isEqualTo(submissionTimestamp);
  }

}
