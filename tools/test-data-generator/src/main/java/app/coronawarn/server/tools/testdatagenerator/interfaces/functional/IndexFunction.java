package app.coronawarn.server.tools.testdatagenerator.interfaces.functional;

import java.util.List;
import java.util.Stack;
import java.util.function.Function;

/**
 * A {@link Function}.
 *
 * @param <T> The type of the index elements.
 */
@FunctionalInterface
public interface IndexFunction<T> extends Function<Stack<Object>, List<T>> {

  List<T> apply(Stack<Object> t);
}
