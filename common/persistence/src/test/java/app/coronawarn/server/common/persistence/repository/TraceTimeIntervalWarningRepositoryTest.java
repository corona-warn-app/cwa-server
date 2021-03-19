package app.coronawarn.server.common.persistence.repository;

import app.coronawarn.server.common.persistence.domain.TraceTimeIntervalWarning;
import java.util.UUID;
import org.assertj.core.api.Assertions;
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

    Assertions.assertThat(next).isNotNull();
    Assertions.assertThat(next.getId()).isNotNull();
    Assertions.assertThat(next.getTraceLocationGuid()).isEqualTo(guid);
    Assertions.assertThat(next.getStartIntervalNumber()).isEqualTo(startIntervalNumber);
    Assertions.assertThat(next.getEndIntervalNumber()).isEqualTo(endIntervalNumber);
    Assertions.assertThat(next.getTransmissionRiskLevel()).isEqualTo(transmissionRiskLevel);
  }
}
