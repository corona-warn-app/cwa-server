package app.coronawarn.server.services.distribution.structure.directory;

import app.coronawarn.server.services.distribution.structure.functional.DirectoryFunction;
import app.coronawarn.server.services.distribution.structure.functional.FileFunction;
import app.coronawarn.server.services.distribution.structure.functional.Formatter;
import java.util.List;
import java.util.Stack;

/**
 * A meta directory that maps its on-disk subdirectories to some list of elements. This list of
 * elements is determined by a {@link FileFunction}.
 *
 * @param <T> The type of the elements in the index.
 */
public interface IndexDirectory<T> extends Directory {

  /**
   * Adds a file under the name {@code name}, whose content is calculated by the {@code
   * fileFunction} to each one of the directories created from the index. The {@code fileFunction}
   * calculates the file content from a {@link java.util.Stack} of parent {@link IndexDirectoryImpl}
   * indices. File content calculation happens on {@link DirectoryImpl#write}.
   *
   * @param fileFunction A function that can calculate the content of the file, based on
   */
  void addFileToAll(FileFunction fileFunction);

  /**
   * Adds a {@link DirectoryImpl} to each one of the directories created from the index. Analogous
   * to {@link IndexDirectory#addFileToAll}.
   */
  void addDirectoryToAll(DirectoryFunction directoryFunction);

  /**
   * Calls the {@link app.coronawarn.server.services.distribution.structure.functional.IndexFunction}
   * with the {@code indices} to calculate and return the elements of the index of this {@link
   * IndexDirectory}.
   *
   * @param indices A {@link Stack} of parameters from all {@link IndexDirectory IndexDirectories}
   *                further up in the hierarchy. The Stack may contain different types, depending on
   *                the types {@code T} of {@link IndexDirectory IndexDirectories} further up in the
   *                hierarchy.
   */
  List<T> getIndex(Stack<Object> indices);

  /**
   * Returns the function used to format elements of the index (e.g. for writing to disk).
   */
  Formatter<T> getIndexFormatter();
}
