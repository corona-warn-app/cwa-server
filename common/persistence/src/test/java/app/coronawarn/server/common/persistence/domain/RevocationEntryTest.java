package app.coronawarn.server.common.persistence.domain;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RevocationEntryTest {

  final static RevocationEntryId revocationEntryId = new RevocationEntryId(new byte[]{10}, new byte[]{10},
      new byte[]{10});

  final static RevocationEntry revocationEntry = new RevocationEntry(new byte[]{10}, new byte[]{10}, new byte[]{10});

  @Test
  void testRevocationEntryIdHashCode() {
    assertTrue(0 != revocationEntryId.hashCode());
  }

  @Test
  void testRevocationEntryIdEquals() {
    assertTrue(revocationEntryId.equals(new RevocationEntryId(new byte[]{10}, new byte[]{10}, new byte[]{10})));
  }

  @Test
  void testRevocationEntryHashCode() {
    assertTrue(0 != revocationEntry.getKidTypeHashCode());
    assertTrue(0 != revocationEntry.getYHashCode());
    assertTrue(0 != revocationEntry.getXHashCode());
  }
}
