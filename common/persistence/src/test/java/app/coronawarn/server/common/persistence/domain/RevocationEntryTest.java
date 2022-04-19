package app.coronawarn.server.common.persistence.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class RevocationEntryTest {

  final static RevocationEntryId revocationEntryId = new RevocationEntryId(new byte[]{10}, new byte[]{10},
      new byte[]{10});

  final static RevocationEntry revocationEntry = new RevocationEntry(new byte[]{10}, new byte[]{10}, new byte[]{10});

  @Test
  void testRevocationEntryIdHashCode() {
    assertNotEquals(0, revocationEntryId.hashCode());
  }

  @Test
  void testRevocationEntryIdEquals() {
    assertEquals(revocationEntryId, (new RevocationEntryId(new byte[]{10}, new byte[]{10}, new byte[]{10})));
  }

  @Test
  void testRevocationEntryHashCode() {
    assertNotEquals(0, revocationEntry.getKidTypeHashCode());
    assertNotEquals(0, revocationEntry.getYHashCode());
    assertNotEquals(0, revocationEntry.getXHashCode());
  }
}
