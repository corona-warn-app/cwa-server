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

import java.util.ArrayDeque;
import java.util.Deque;

public class ImmutableStack<T> {

  private final Deque<T> stack;

  public ImmutableStack() {
    this.stack = new ArrayDeque<>();
  }

  /**
   * Creates a clone of the specified {@link ImmutableStack}.
   */
  public ImmutableStack(ImmutableStack<T> other) {
    this.stack = new ArrayDeque<>(other.stack);
  }

  /**
   * Returns a clone of this stack that contains the specified item at its top position.
   */
  public ImmutableStack<T> push(T item) {
    ImmutableStack<T> clone = new ImmutableStack<>(this);
    clone.stack.push(item);
    return clone;
  }

  /**
   * Returns a clone of this stack with its top element removed.
   */
  public ImmutableStack<T> pop() {
    ImmutableStack<T> clone = new ImmutableStack<>(this);
    clone.stack.pop();
    return clone;
  }

  public T peek() {
    return this.stack.peek();
  }
}
