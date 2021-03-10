package app.coronawarn.server.common.persistence.eventregistration.repository;

import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.common.persistence.eventregistration.domain.TraceTimeIntervalWarning;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;

@DataJdbcTest
public class TraceTimeIntervalWarningRepositoryTest {

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
    TraceTimeIntervalWarning traceTimeIntervalWarning = new TraceTimeIntervalWarning(
        guid, startIntervalNumber, endIntervalNumber, transmissionRiskLevel);
    underTest.save(traceTimeIntervalWarning);

    final Iterable<TraceTimeIntervalWarning> all = underTest.findAll();
    final TraceTimeIntervalWarning next = all.iterator().next();

    assertThat(next).isNotNull();
    assertThat(next.getId()).isNotNull();
    assertThat(next.getTraceLocationGuid()).isEqualTo(guid);
    assertThat(next.getStartIntervalNumber()).isEqualTo(startIntervalNumber);
    assertThat(next.getEndIntervalNumber()).isEqualTo(endIntervalNumber);
    assertThat(next.getTransmissionRiskLevel()).isEqualTo(transmissionRiskLevel);
  }
}
