package app.coronawarn.server.services.callback;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class HashingUtilsTest {

  @Test
  void testHashHasExpectedLength() {
    String hash = HashingUtils.computeHash("string");
    assertThat(hash).hasSize(32);
  }

}
