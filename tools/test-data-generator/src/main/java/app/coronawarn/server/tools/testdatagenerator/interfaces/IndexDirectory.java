package app.coronawarn.server.tools.testdatagenerator.interfaces;

import app.coronawarn.server.tools.testdatagenerator.interfaces.functional.DirectoryFunction;
import app.coronawarn.server.tools.testdatagenerator.interfaces.functional.FileFunction;
import app.coronawarn.server.tools.testdatagenerator.interfaces.functional.Formatter;
import java.util.List;
import java.util.Stack;

public interface IndexDirectory<T> extends Directory {

  void addFileToAll(FileFunction fileFunction);

  void addDirectoryToAll(DirectoryFunction directoryFunction);

  List<T> getIndex(Stack<Object> indices);

  Formatter<T> getIndexFormatter();
}
