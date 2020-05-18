package app.coronawarn.server.services.distribution.assembly.structure.util;

import java.util.Stack;

public class ImmutableStack<T> {

  private final Stack<T> stack;

  public ImmutableStack() {
    this.stack = new Stack<>();
  }

  /**
   * Creates a clone of the specified {@link ImmutableStack}.
   */
  public ImmutableStack(ImmutableStack<T> other) {
    this.stack = (Stack<T>) other.stack.clone();
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
