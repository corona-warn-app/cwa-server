package app.coronawarn.server.common.persistence.service;

import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.common.persistence.domain.RevocationEntry;
import app.coronawarn.server.common.persistence.repository.DccRevocationListRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"fake-dcc-revocation"})
@DataJdbcTest
class DccRevocationStorageTest {

  @Autowired
  DccRevocationListService dccRevocationListService;

  @Autowired
  DccRevocationListRepository dccRevocationListRepository;

  @Test
  void testStorageWithDifferentEntries() {

    List<RevocationEntry> testData = new ArrayList<>();

    RevocationEntry revocationEntry1 = new RevocationEntry(new byte[]{1, 2, 3, 4}, new byte[]{1, 2, 3, 4},
        new byte[]{1, 2, 3, 4});
    RevocationEntry revocationEntry2 = new RevocationEntry(new byte[]{2, 2, 2, 2}, new byte[]{1, 2, 3, 4},
        new byte[]{1, 2, 3, 4});
    RevocationEntry revocationEntry3 = new RevocationEntry(new byte[]{3, 3, 3, 3}, new byte[]{1, 2, 3, 4},
        new byte[]{1, 2, 3, 4});

    testData.addAll(List.of(revocationEntry1, revocationEntry2, revocationEntry3));
    dccRevocationListService.store(testData);

    List<RevocationEntry> actualRevocationEntriesStored = (List<RevocationEntry>) dccRevocationListService
        .getRevocationListEntries();

    assertThat(actualRevocationEntriesStored).containsAll(testData);
  }

  @Test
  void testStorageWithTwoSameEntries() {

    List<RevocationEntry> testData = new ArrayList<>();

    RevocationEntry revocationEntry1 = new RevocationEntry(new byte[]{1, 2, 3, 4}, new byte[]{1, 2, 3, 4},
        new byte[]{1, 2, 3, 4});
    RevocationEntry revocationEntry2 = new RevocationEntry(new byte[]{1, 2, 3, 4}, new byte[]{1, 2, 3, 4},
        new byte[]{1, 2, 3, 4});
    RevocationEntry revocationEntry3 = new RevocationEntry(new byte[]{1, 2, 3, 4}, new byte[]{1, 2, 3, 4},
        new byte[]{1, 2, 3, 4});

    testData.addAll(List.of(revocationEntry1, revocationEntry2, revocationEntry3));
    dccRevocationListService.store(testData);

    List<RevocationEntry> actualRevocationEntriesStored = (List<RevocationEntry>) dccRevocationListService
        .getRevocationListEntries();

    assertThat(actualRevocationEntriesStored.size()).isNotEqualTo(testData.size());
  }
}
