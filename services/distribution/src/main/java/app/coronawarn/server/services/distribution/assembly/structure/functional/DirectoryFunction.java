package app.coronawarn.server.services.distribution.assembly.structure.functional;

import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import java.util.function.Function;

/**
 * A {@code Function<Stack<Object>, Directory>}.
 */
@FunctionalInterface
public interface DirectoryFunction extends Function<ImmutableStack<Object>, Directory> {

  Directory apply(ImmutableStack<Object> t);
}
