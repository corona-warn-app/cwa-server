package app.coronawarn.server.tools.testdatagenerator.interfaces.functional;

import app.coronawarn.server.tools.testdatagenerator.interfaces.Directory;
import java.util.Stack;
import java.util.function.Function;

/**
 * A {@link Function}.
 */
@FunctionalInterface
public interface DirectoryFunction extends Function<Stack<Object>, Directory> {

  Directory apply(Stack<Object> t);
}
