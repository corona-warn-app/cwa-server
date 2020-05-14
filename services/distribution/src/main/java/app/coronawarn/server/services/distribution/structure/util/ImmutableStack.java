package app.coronawarn.server.services.distribution.structure.util;

import java.util.Stack;

public class ImmutableStack<T> {

  private Stack<T> stack;

  public ImmutableStack() {
    this.stack = new Stack<>();
  }

  public ImmutableStack(ImmutableStack<T> other) {
    this.stack = (Stack<T>)other.stack.clone();
  }

  public ImmutableStack<T> push(T item) {
    ImmutableStack<T> clone = new ImmutableStack<>(this);
    clone.stack.push(item);
    return clone;
  }

  public ImmutableStack<T> pop() {
    ImmutableStack<T> clone = new ImmutableStack<>(this);
    clone.stack.pop();
    return clone;
  }

  public T peek() {
    return this.stack.peek();
  }
}
