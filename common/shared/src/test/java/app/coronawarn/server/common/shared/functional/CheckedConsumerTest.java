package app.coronawarn.server.common.shared.functional;

import static app.coronawarn.server.common.shared.functional.CheckedConsumer.uncheckedConsumer;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;
import java.util.function.Consumer;

class CheckedConsumerTest {

  public static final String TEST_STRING = "string";

  @Test
  public void checkConsumer() {
    CheckedConsumer<String, Exception> checkedConsumer = s -> {
      throw new Exception("This has to be converted into Runtime: " + s);
    };

    Consumer<String> consumer = uncheckedConsumer(checkedConsumer);

    assertThatExceptionOfType(RuntimeException.class)
        .isThrownBy(() -> consumer.accept(TEST_STRING));
  }

}
