package app.coronawarn.server.services.distribution.structure.functional;

import java.util.function.Function;

/**
 * A {@link Function}.
 *
 * @param <T> The type of the elements to format.
 */
@FunctionalInterface
public interface Formatter<T> extends Function<T, Object> {

  Object apply(T t);
}
