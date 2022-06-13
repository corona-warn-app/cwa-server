package app.coronawarn.server.common.persistence.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Random;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class RevocationEntryTest {

  final static RevocationEntryId revocationEntryId = new RevocationEntryId(new byte[] { 10 }, new byte[] { 10 },
      new byte[] { 10 });

  final static RevocationEntry revocationEntry = new RevocationEntry(new byte[]{10}, new byte[]{10}, new byte[]{10});

  /**
   * {@link MethodSource} for {@link #testToString(byte[])}.
   * 
   * @return {@link Stream} of {@link Arguments}
   */
  static Stream<Arguments> randomByteArrays() {
    final Random random = new Random();

    return Stream.generate(() -> {
      final byte[] buffer = new byte[8];
      random.nextBytes(buffer);
      return Arguments.of(buffer);
    }).limit(300);
  }

  @Test
  void testRevocationEntryIdHashCode() {
    assertNotEquals(0, revocationEntryId.hashCode());
  }

  @Test
  void testRevocationEntryIdEquals() {
    assertEquals(revocationEntryId, new RevocationEntryId(new byte[] { 10 }, new byte[] { 10 }, new byte[] { 10 }));
  }

  @Test
  void testRevocationEntryHashCode() {
    assertNotEquals(0, revocationEntry.getKidTypeHashCode());
    assertNotEquals(0, revocationEntry.getYHashCode());
    assertNotEquals(0, revocationEntry.getXHashCode());
  }

  @Test
  void testToString() {
    final byte[] kid = "\t\t\t\t\t\t\t\t".getBytes();
    RevocationEntry fixture = new RevocationEntry(kid, new byte[] { 10 }, null);
    assertEquals("0a", fixture.toString().substring(16));
    fixture = new RevocationEntry(kid, new byte[] { 11 }, null);
    assertEquals("0b", fixture.toString().substring(16));
    fixture = new RevocationEntry(kid, new byte[] { 12 }, null);
    assertEquals("0c", fixture.toString().substring(16));

    assertEquals("000000000000000000",
        new RevocationEntry(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 }, new byte[] { 0 }, null).toString());
  }

  @ParameterizedTest
  @MethodSource("randomByteArrays")
  void testToString(final byte[] input) {
    final RevocationEntry fixture = new RevocationEntry(input, new byte[] { 10 }, null);
    if (18 != fixture.toString().length()) {
      System.err.println(fixture);
    }
    assertEquals(18, fixture.toString().length());
    assertEquals("0a", fixture.toString().substring(16));
  }
}
