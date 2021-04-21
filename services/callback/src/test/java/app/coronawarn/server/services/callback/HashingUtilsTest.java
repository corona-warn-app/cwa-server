package app.coronawarn.server.services.callback;

import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.common.persistence.utils.hash.HashUtils;
import org.junit.jupiter.api.Test;

class HashingUtilsTest {

  @Test
  void testHashHasExpectedLength() {
    String hash = HashUtils.computeHash("string");
    assertThat(hash).hasSize(32);
  }

}
