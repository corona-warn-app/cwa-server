package app.coronawarn.server.common.persistence.repository;

import app.coronawarn.server.common.persistence.domain.TraceTimeIntervalWarning;
import app.coronawarn.server.common.persistence.service.utils.checkins.CheckinsDateSpecification;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload.SubmissionType;
import java.time.Instant;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;

@DataJdbcTest
class TraceTimeIntervalWarningRepositoryTest {

  @Autowired
  private TraceTimeIntervalWarningRepository underTest;

  @AfterEach
  void tearDown() {
    underTest.deleteAll();
  }

  @Test
  void shouldPersistTraceTimeIntervalWarning() {
    final byte[] guid = UUID.randomUUID().toString().getBytes();
    final int startIntervalNumber = 0;
    final int endIntervalNumber = 10;
    final int transmissionRiskLevel = 5;
    final long submissionTimestamp =
        CheckinsDateSpecification.HOUR_SINCE_EPOCH_DERIVATION.apply(Instant.now().getEpochSecond());
    TraceTimeIntervalWarning traceTimeIntervalWarning = new TraceTimeIntervalWarning(guid, startIntervalNumber,
        endIntervalNumber - startIntervalNumber, transmissionRiskLevel, submissionTimestamp,
        SubmissionType.SUBMISSION_TYPE_PCR_TEST);
    underTest.save(traceTimeIntervalWarning);

    final Iterable<TraceTimeIntervalWarning> all = underTest.findAll();
    final TraceTimeIntervalWarning next = all.iterator().next();

    Assertions.assertThat(next).isNotNull();
    Assertions.assertThat(next.getId()).isNotNull();
    Assertions.assertThat(next.getTraceLocationId()).isEqualTo(guid);
    Assertions.assertThat(next.getStartIntervalNumber()).isEqualTo(startIntervalNumber);
    Assertions.assertThat(next.getPeriod()).isEqualTo(endIntervalNumber - startIntervalNumber);
    Assertions.assertThat(next.getTransmissionRiskLevel()).isEqualTo(transmissionRiskLevel);
    Assertions.assertThat(next.getSubmissionTimestamp()).isEqualTo(submissionTimestamp);
  }
}
