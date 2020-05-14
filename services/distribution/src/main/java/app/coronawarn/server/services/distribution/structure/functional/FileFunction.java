package app.coronawarn.server.services.distribution.structure.functional;

import app.coronawarn.server.services.distribution.structure.file.File;
import app.coronawarn.server.services.distribution.structure.util.ImmutableStack;
import java.util.function.Function;

/**
 * A {@code Function<Stack<Object>, File>}.
 */
@FunctionalInterface
public interface FileFunction extends Function<ImmutableStack<Object>, File> {

  File apply(ImmutableStack<Object> t);
}
