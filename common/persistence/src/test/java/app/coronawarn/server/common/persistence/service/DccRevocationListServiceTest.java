package app.coronawarn.server.common.persistence.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import app.coronawarn.server.common.persistence.domain.RevocationEntry;
import app.coronawarn.server.common.persistence.repository.DccRevocationListRepository;
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

  @Autowired
  private DccRevocationListRepository repository;

  @AfterEach
  public void tearDown() {
    repository.deleteAll();
  }

  @Test
  void testStore() {
    Collection<RevocationEntry> some = new ArrayList<>();
    some.add(new RevocationEntry("foo".getBytes(), "A".getBytes(), "bar".getBytes()));
    service.store(some);

    var actKeys = service.getRevocationListEntries();

    assertEquals(1, actKeys.size());
    assertTrue(Arrays.equals("foo".getBytes(), actKeys.iterator().next().getKid()));
  }
}
