package app.coronawarn.server.services.distribution.structure.functional;

import app.coronawarn.server.services.distribution.structure.file.File;
import java.util.Stack;
import java.util.function.Function;

/**
 * A {@code Function<Stack<Object>, File>}.
 */
@FunctionalInterface
public interface FileFunction extends Function<Stack<Object>, File> {

  File apply(Stack<Object> t);
}
