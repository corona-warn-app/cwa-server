package app.coronawarn.server.services.distribution.structure.functional;

import app.coronawarn.server.services.distribution.structure.directory.Directory;
import app.coronawarn.server.services.distribution.structure.util.ImmutableStack;
import java.util.function.Function;

/**
 * A {@code Function<Stack<Object>, Directory>}.
 */
@FunctionalInterface
public interface DirectoryFunction extends Function<ImmutableStack<Object>, Directory> {

  Directory apply(ImmutableStack<Object> t);
}
