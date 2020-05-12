package app.coronawarn.server.services.distribution.structure.directory;

import app.coronawarn.server.services.distribution.structure.functional.DirectoryFunction;
import app.coronawarn.server.services.distribution.structure.functional.FileFunction;
import app.coronawarn.server.services.distribution.structure.functional.Formatter;
import java.util.List;
import java.util.Stack;

public interface IndexDirectory<T> extends Directory {

  void addFileToAll(FileFunction fileFunction);

  void addDirectoryToAll(DirectoryFunction directoryFunction);

  List<T> getIndex(Stack<Object> indices);

  Formatter<T> getIndexFormatter();
}
