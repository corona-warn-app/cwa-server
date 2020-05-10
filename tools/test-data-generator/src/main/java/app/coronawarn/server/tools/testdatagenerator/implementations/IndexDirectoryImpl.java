package app.coronawarn.server.tools.testdatagenerator.implementations;

import app.coronawarn.server.tools.testdatagenerator.interfaces.Directory;
import app.coronawarn.server.tools.testdatagenerator.interfaces.File;
import app.coronawarn.server.tools.testdatagenerator.interfaces.functional.DirectoryFunction;
import app.coronawarn.server.tools.testdatagenerator.interfaces.functional.FileFunction;
import app.coronawarn.server.tools.testdatagenerator.interfaces.functional.Formatter;
import app.coronawarn.server.tools.testdatagenerator.interfaces.functional.IndexFunction;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * A meta directory that maps its on-disk subdirectories to some list of elements. This list of
 * elements is determined by the {@link IndexDirectoryImpl#indexFunction}.
 *
 * @param <T> The type of the elements in the index (e.g. LocalDate for the /date directory)
 */
public class IndexDirectoryImpl<T> extends DirectoryImpl implements
    app.coronawarn.server.tools.testdatagenerator.interfaces.IndexDirectory<T> {

  // Files to be written into every directory created through the index
  private final List<FileFunction> metaFiles = new ArrayList<>();
  // Directories to be written into every directory created through the index
  private final List<DirectoryFunction> metaDirectories = new ArrayList<>();
  private final IndexFunction<T> indexFunction;
  private final Formatter<T> indexFormatter;

  /**
   * Constructor.
   *
   * @param name           The name that this directory should have on disk.
   * @param indexFunction  An {@link IndexFunction} that calculates the index of this {@link
   *                       IndexDirectoryImpl} from a {@link java.util.Stack} of parent {@link
   *                       IndexDirectoryImpl} indices. The top element of the stack is from the
   *                       closest {@link IndexDirectoryImpl} in the parent chain.
   * @param indexFormatter A {@link Formatter} used to format the directory name for each index
   *                       element returned by the {@link IndexDirectoryImpl#indexFunction}.
   */
  public IndexDirectoryImpl(String name, IndexFunction<T> indexFunction,
      Formatter<T> indexFormatter) {
    super(name);
    this.indexFunction = indexFunction;
    this.indexFormatter = indexFormatter;
  }

  /**
   * Constructor that defaults the {@link IndexDirectoryImpl#indexFormatter} to {@link
   * Object#toString}.
   */
  public IndexDirectoryImpl(String name, IndexFunction<T> indexFunction) {
    this(name, indexFunction, Object::toString);
  }

  @Override
  public Formatter<T> getIndexFormatter() {
    return this.indexFormatter;
  }

  @Override
  public List<T> getIndex(Stack<Object> indices) {
    return this.indexFunction.apply(indices);
  }

  /**
   * Adds a file under the name {@code name}, whose content is calculated by the {@code
   * fileFunction} to each one of the directories created from the index. The {@code fileFunction}
   * calculates the file content from a {@link java.util.Stack} of parent {@link IndexDirectoryImpl}
   * indices. File content calculation happens on {@link DirectoryImpl#write}.
   *
   * @param fileFunction A function that can calculate the content of the file, based on
   */
  @Override
  public void addFileToAll(FileFunction fileFunction) {
    this.metaFiles.add(fileFunction);
  }

  /**
   * Adds a {@link DirectoryImpl} to each one of the directories created from the index.
   */
  @Override
  public void addDirectoryToAll(DirectoryFunction directoryFunction) {
    this.metaDirectories.add(directoryFunction);
  }

  @Override
  public void prepare(Stack<Object> indices) {
    super.prepare(indices);
    this.prepareIndex(indices);
  }

  private void prepareIndex(Stack<Object> indices) {
    this.getIndex(indices).forEach(currentIndex -> {
      Stack<Object> newIndices = cloneStackAndAdd(indices, currentIndex);
      Directory subDirectory = makeSubDirectory(currentIndex);
      prepareMetaFiles(newIndices, subDirectory);
      prepareMetaDirectories(newIndices, subDirectory);
    });
  }

  private static Stack<Object> cloneStackAndAdd(Stack<Object> stack, Object element) {
    Stack<Object> newStack = (Stack<Object>) stack.clone();
    newStack.push(element);
    return newStack;
  }

  private Directory makeSubDirectory(T index) {
    Directory subDirectory = new DirectoryImpl(this.indexFormatter.apply(index).toString());
    this.addDirectory(subDirectory);
    return subDirectory;
  }

  private void prepareMetaFiles(Stack<Object> indices, Directory target) {
    this.metaFiles.forEach(metaFileFunction -> {
      File newFile = metaFileFunction.apply(indices);
      target.addFile(newFile);
      newFile.prepare(indices);
    });
  }

  private void prepareMetaDirectories(Stack<Object> indices, Directory target) {
    this.metaDirectories.forEach(metaDirectoryFunction -> {
      Directory newDirectory = metaDirectoryFunction.apply(indices);
      target.addDirectory(newDirectory);
      newDirectory.prepare(indices);
    });
  }
}
