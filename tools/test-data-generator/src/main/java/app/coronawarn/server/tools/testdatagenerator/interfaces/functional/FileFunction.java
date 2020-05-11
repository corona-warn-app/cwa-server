package app.coronawarn.server.tools.testdatagenerator.interfaces.functional;

import app.coronawarn.server.tools.testdatagenerator.interfaces.File;
import java.util.Stack;
import java.util.function.Function;

/**
 * A {@link Function}.
 */
@FunctionalInterface
public interface FileFunction extends Function<Stack<Object>, File> {

  File apply(Stack<Object> t);
}
