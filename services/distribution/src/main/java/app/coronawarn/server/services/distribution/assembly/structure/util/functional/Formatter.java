

package app.coronawarn.server.services.distribution.assembly.structure.util.functional;

import java.util.function.Function;

/**
 * A {@code Function<T, Object>}.
 *
 * @param <T> The type of the elements to format.
 */
@FunctionalInterface
public interface Formatter<T> extends Function<T, Object> {

  Object apply(T t);
}
