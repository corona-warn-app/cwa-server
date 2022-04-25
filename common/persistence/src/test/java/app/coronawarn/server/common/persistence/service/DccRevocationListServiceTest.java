package app.coronawarn.server.common.persistence.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import app.coronawarn.server.common.persistence.domain.RevocationEntry;
import app.coronawarn.server.common.persistence.domain.RevocationEtag;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;

@DataJdbcTest
class DccRevocationListServiceTest {

  @Autowired
  private DccRevocationListService service;

  @AfterEach
  public void tearDown() {
    service.truncate();
  }

  @Test
  void testStore() {
    final Collection<RevocationEntry> some = new ArrayList<>();
    some.add(new RevocationEntry("foo".getBytes(), "A".getBytes(), "bar".getBytes()));
    service.store(some);

    final var actKeys = service.getRevocationListEntries();

    assertEquals(1, actKeys.size());
    assertTrue(Arrays.equals("foo".getBytes(), actKeys.iterator().next().getKid()));
  }

  @Test
  void testStoreEtag() {
    final RevocationEtag some = new RevocationEtag();
    some.setEtag("foo");
    some.setPath("bar");

    service.store(some);
    service.store(some);

    assertTrue(service.etagExists("foo"));
    assertFalse(service.etagExists("42"));
  }

  @Test
  void testEtagExists() {
    assertFalse(service.etagExists(null));
  }
}
