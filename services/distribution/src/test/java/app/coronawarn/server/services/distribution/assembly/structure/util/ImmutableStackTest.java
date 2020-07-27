/*-
 * ---license-start
 * Corona-Warn-App
 * ---
 * Copyright (C) 2020 SAP SE and all other contributors
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ---license-end
 */

package app.coronawarn.server.services.distribution.assembly.structure.util;

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
    assertThatExceptionOfType(NoSuchElementException.class)
        .isThrownBy(() -> new ImmutableStack<>().pop());
  }
}
