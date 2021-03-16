package app.coronawarn.server.services.eventregistration.repository;

import app.coronawarn.server.services.eventregistration.domain.TraceLocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

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
    underTest.saveOnConflictDoNothing(traceLocationGuidHash, version, createdAt);

    Optional<TraceLocation> traceLocationOptional = underTest
        .findTraceLocationByGuidHash(traceLocationGuidHash);
    assertThat(traceLocationOptional).isPresent();
    assertThat(traceLocationOptional.get().getTraceLocationGuidHash()).isEqualTo(traceLocationGuidHash);
    assertThat(traceLocationOptional.get().getVersion()).isEqualTo(version);
    assertThat(traceLocationOptional.get().getCreatedAt()).isEqualTo(createdAt);

    int versionSecond = 1;
    Long createdAtSecond = now.plus(1, ChronoUnit.HOURS).getEpochSecond();
    underTest.saveOnConflictDoNothing(traceLocationGuidHash, versionSecond, createdAtSecond);

    final Iterable<TraceLocation> all = underTest.findAll();
    for (TraceLocation tr : all) {
      assertThat(tr.getCreatedAt()).isEqualTo(createdAt);
      assertThat(tr.getVersion()).isEqualTo(version);
    }
  }


  private String buildUuidHash() throws NoSuchAlgorithmException {
    return Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-256")
        .digest(UUID
            .randomUUID()
            .toString()
            .getBytes(
                Charset.defaultCharset())));
  }
}
