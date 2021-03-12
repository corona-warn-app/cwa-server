package app.coronawarn.server.services.eventregistration.repository;

import app.coronawarn.server.services.eventregistration.domain.TraceLocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.test.context.ActiveProfiles;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJdbcTest
@ActiveProfiles("event-registration")
public class TraceLocationRepositoryTest {

  @Autowired
  private TraceLocationRepository underTest;


  @AfterEach
  void tearDown() {
    underTest.deleteAll();
  }

  @Test
  public void saveNewTraceLocationShouldBePersisted() throws Exception {
    byte[] traceLocationGuidHash = MessageDigest.getInstance("SHA-256")
        .digest(UUID
            .randomUUID()
            .toString()
            .getBytes(
                Charset.defaultCharset()));
    Long createdAt = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
    int version = 0;
    underTest.save(traceLocationGuidHash, version, createdAt);

    Optional<TraceLocation> traceLocationOptional = underTest
        .findTraceLocationByGuidHash(traceLocationGuidHash);
    assertThat(traceLocationOptional).isPresent();
    assertThat(traceLocationOptional.get().getTraceLocationGuidHash()).isEqualTo(traceLocationGuidHash);
    assertThat(traceLocationOptional.get().getVersion()).isEqualTo(version);
    assertThat(traceLocationOptional.get().getCreatedAt()).isEqualTo(createdAt);
    assertThat(traceLocationOptional.get().getId()).isNotNull();
  }
}
