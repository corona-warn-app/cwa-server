package app.coronawarn.server.services.distribution.structure.functional;

import app.coronawarn.server.services.distribution.structure.directory.Directory;
import java.util.Stack;
import java.util.function.Function;

/**
 * A {@link Function}.
 */
@FunctionalInterface
public interface DirectoryFunction extends Function<Stack<Object>, Directory> {

  Directory apply(Stack<Object> t);
}
