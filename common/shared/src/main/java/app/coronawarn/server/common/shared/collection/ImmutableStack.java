

package app.coronawarn.server.common.shared.collection;

import java.util.ArrayDeque;
import java.util.Deque;

public class ImmutableStack<T> {

  private final Deque<T> stack;

  public ImmutableStack() {
    this.stack = new ArrayDeque<>();
  }

  /**
   * Creates a clone of the specified {@link ImmutableStack}.
   *
   * @param other ImmutableStack generic type clone
   */
  public ImmutableStack(ImmutableStack<T> other) {
    this.stack = new ArrayDeque<>(other.stack);
  }

  /**
   * Returns a clone of this stack that contains the specified item at its top position.
   *
   * @param item item to be places a top of cloned stack
   * @return stack with specified item at top
   */
  public ImmutableStack<T> push(T item) {
    ImmutableStack<T> clone = new ImmutableStack<>(this);
    clone.stack.push(item);
    return clone;
  }

  /**
   * Returns a clone of this stack with its top element removed.
   *
   * @return the top item of the stack
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
