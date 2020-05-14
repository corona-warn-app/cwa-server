package app.coronawarn.server.services.distribution.structure.functional;

import app.coronawarn.server.services.distribution.structure.util.ImmutableStack;
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
