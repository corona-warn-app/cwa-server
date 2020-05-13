package app.coronawarn.server.services.distribution.structure.functional;

import java.util.Set;
import java.util.Stack;
import java.util.function.Function;

/**
 * A {@code Function<Stack<Object>, List<T>>}.
 *
 * @param <T> The type of the index elements.
 */
@FunctionalInterface
public interface IndexFunction<T> extends Function<Stack<Object>, Set<T>> {

  Set<T> apply(Stack<Object> t);
}
