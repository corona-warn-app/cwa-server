package app.coronawarn.server.services.distribution.assembly.structure.util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

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
    assertThatExceptionOfType(NoSuchElementException.class)
        .isThrownBy(() -> new ImmutableStack<>().pop());
  }
}
