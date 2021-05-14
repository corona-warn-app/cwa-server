package app.coronawarn.server.common.shared.functional;

import app.coronawarn.server.common.shared.collection.ImmutableStack;
import java.util.Set;
import java.util.function.Function;

/**
 * A {@code Function<Stack<Object>, List<T>>}.
 *
 * @param <T> The type of the index elements.
 */
@FunctionalInterface
public interface IndexFunction<T> extends Function<ImmutableStack<Object>, Set<T>> {

  Set<T> apply(ImmutableStack<Object> t);
}
