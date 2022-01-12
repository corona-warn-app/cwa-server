package app.coronawarn.server.common.shared.functional;

import static app.coronawarn.server.common.shared.functional.CheckedFunction.uncheckedFunction;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.function.Function;
import org.junit.jupiter.api.Test;

class CheckedFunctionTest {

  public static final String TEST_STRING = "string";

  @Test
  public void checkConsumer() {
    CheckedFunction<String, String, Exception> checkedFunction = s -> {
      throw new Exception("This has to be converted into Runtime: " + s);
    };

    final Function<String, String> uncheckedFunction = uncheckedFunction(checkedFunction);
    assertThatExceptionOfType(RuntimeException.class)
        .isThrownBy(() -> uncheckedFunction.apply(TEST_STRING));
  }

}
