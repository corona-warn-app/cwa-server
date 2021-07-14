package app.coronawarn.server.common.shared.util;

import static app.coronawarn.server.common.shared.util.CwaStringUtils.emptyCharrArrayIfNull;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CwaStringTest {
  private String STRING_TEST = "test me";
  private String EMPTY_STRING = "";

  @Test
  void shouldConvertStringToCharArray() {
    assertThat(String.valueOf(emptyCharrArrayIfNull(STRING_TEST))).isEqualTo(STRING_TEST);
    assertThat(emptyCharrArrayIfNull(EMPTY_STRING)).isEmpty();
    assertThat(emptyCharrArrayIfNull(null)).isEmpty();
  }

}
