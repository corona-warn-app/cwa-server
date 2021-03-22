package app.coronawarn.server.services.eventregistration.repository;

import app.coronawarn.server.services.eventregistration.domain.TraceLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.jdbc.Sql;
import java.time.Instant;
import java.util.Optional;

import static app.coronawarn.server.services.eventregistration.service.UuidHashGenerator.buildUuidHash;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJdbcTest
public class TraceLocationRepositoryTest {

  @Autowired
  private TraceLocationRepository underTest;

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

  }


  @Test
  @Sql(scripts = "/scripts/insert_into_tracelocation.sql")
  public void saveThrowExceptionForTraceLocationDuplicateGuid() {
    String traceLocationGuidHash = "FN6lKcknRdWOaAp5GLL0qa+jnIvwFXqY5MkS52iRa78=";
    final Instant now = Instant.now();
    Long createdAt = now.getEpochSecond();
    int version = 0;

    assertThatThrownBy(() -> {
      underTest.save(traceLocationGuidHash, version, createdAt);
    }).isInstanceOf(DuplicateKeyException.class);
  }
}
