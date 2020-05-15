package app.coronawarn.server.services.distribution.assembly.structure.functional;

import app.coronawarn.server.services.distribution.assembly.structure.file.File;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import java.util.function.Function;

/**
 * A {@code Function<Stack<Object>, File>}.
 */
@FunctionalInterface
public interface FileFunction extends Function<ImmutableStack<Object>, File> {

  File apply(ImmutableStack<Object> t);
}
