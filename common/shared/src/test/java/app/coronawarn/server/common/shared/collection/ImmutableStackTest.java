

package app.coronawarn.server.common.shared.collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;

class ImmutableStackTest {

  private final ImmutableStack<String> stack =
      new ImmutableStack<String>().push("Joker").push("Queen").push("King");

  @Test
  void checkPushes() {
    var newStack = stack.push("Ace");

    assertThat(newStack).isNotSameAs(stack);
    assertThat(newStack.peek()).isEqualTo("Ace");
    assertThat(stack.peek()).isEqualTo("King");
  }

  @Test
  void checkPops() {
    var newStack = stack.pop();

    assertThat(newStack).isNotSameAs(stack);
    assertThat(newStack.peek()).isEqualTo("Queen");
    assertThat(stack.peek()).isEqualTo("King");
  }

  @Test
  void checkPeeks() {
    assertThat(stack.peek()).isEqualTo("King");
  }

  @Test
  void checksEmptyStackHasNoting() {
    assertThat(new ImmutableStack<>().peek()).isNull();
  }

  @Test
  void throwsExceptionWhenPopsFromEmptyStack() {
    ImmutableStack<Object> stack = new ImmutableStack<>();
    assertThatExceptionOfType(NoSuchElementException.class).isThrownBy(() -> stack.pop());
  }
}
