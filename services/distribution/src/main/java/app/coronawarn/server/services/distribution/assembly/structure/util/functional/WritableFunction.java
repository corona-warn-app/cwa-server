package app.coronawarn.server.services.distribution.assembly.structure.util.functional;

import app.coronawarn.server.common.shared.collection.ImmutableStack;
import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import java.util.Optional;
import java.util.function.Function;

/**
 * A {@code Function<Stack<Object>, Writable>}.
 *
 * @param <T> The specific type of {@link Writable} that this {@link WritableFunction} can create.
 */
@FunctionalInterface
public interface WritableFunction<T extends Writable<T>> extends
    Function<ImmutableStack<Object>, Optional<Writable<T>>> {

  Optional<Writable<T>> apply(ImmutableStack<Object> t);
}
