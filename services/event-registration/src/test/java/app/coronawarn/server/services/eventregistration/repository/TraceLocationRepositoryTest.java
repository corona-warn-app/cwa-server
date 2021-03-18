package app.coronawarn.server.services.eventregistration.repository;

import static app.coronawarn.server.services.eventregistration.service.UuidHashGenerator.buildUuidHash;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import app.coronawarn.server.services.eventregistration.domain.TraceLocation;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.dao.DuplicateKeyException;

@DataJdbcTest
public class TraceLocationRepositoryTest {

  @Autowired
  private TraceLocationRepository underTest;


  @AfterEach
  void tearDown() {
    underTest.deleteAll();
  }

  @Test
  public void saveNewTraceLocationShouldBePersisted() throws Exception {
    String traceLocationGuidHash = buildUuidHash();
    final Instant now = Instant.now();
    Long createdAt = now.getEpochSecond();
    int version = 0;
    underTest.save(traceLocationGuidHash, version, createdAt);

    Optional<TraceLocation> traceLocationOptional = underTest
        .findTraceLocationByGuidHash(traceLocationGuidHash);
    assertThat(traceLocationOptional).isPresent();
    assertThat(traceLocationOptional.get().getTraceLocationGuidHash()).isEqualTo(traceLocationGuidHash);
    assertThat(traceLocationOptional.get().getVersion()).isEqualTo(version);
    assertThat(traceLocationOptional.get().getCreatedAt()).isEqualTo(createdAt);

    final Iterable<TraceLocation> all = underTest.findAll();
    for (TraceLocation tr : all) {
      assertThat(tr.getCreatedAt()).isEqualTo(createdAt);
      assertThat(tr.getVersion()).isEqualTo(version);
    }
  }

  //todo : fails delete statement
  /*@Test
  public void saveThrowExceptionForTraceLocationDuplicateGuid() throws Exception {
    String traceLocationGuidHash = buildUuidHash();
    final Instant now = Instant.now();
    Long createdAt = now.getEpochSecond();
    int version = 0;
    underTest.save(traceLocationGuidHash, version, createdAt);

    assertThrows(DuplicateKeyException.class, () -> {
      underTest.save(traceLocationGuidHash, version, createdAt);
    });
  }*/
}
