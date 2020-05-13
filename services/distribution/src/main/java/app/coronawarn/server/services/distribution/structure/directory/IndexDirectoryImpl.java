package app.coronawarn.server.services.distribution.structure.directory;

import app.coronawarn.server.services.distribution.structure.file.File;
import app.coronawarn.server.services.distribution.structure.functional.DirectoryFunction;
import app.coronawarn.server.services.distribution.structure.functional.FileFunction;
import app.coronawarn.server.services.distribution.structure.functional.Formatter;
import app.coronawarn.server.services.distribution.structure.functional.IndexFunction;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class IndexDirectoryImpl<T> extends DirectoryImpl implements IndexDirectory<T> {

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

  @Override
  public Formatter<T> getIndexFormatter() {
    return this.indexFormatter;
  }

  @Override
  public List<T> getIndex(Stack<Object> indices) {
    return this.indexFunction.apply(indices);
  }

  @Override
  public void addFileToAll(FileFunction fileFunction) {
    this.metaFiles.add(fileFunction);
  }

  @Override
  public void addDirectoryToAll(DirectoryFunction directoryFunction) {
    this.metaDirectories.add(directoryFunction);
  }

  /**
   * Creates a new subdirectory for every element of the {@link IndexDirectory#getIndex index} and
   * writes {@link IndexDirectory#addFileToAll files} and {@link IndexDirectory#addDirectory
   * directories} to those. The respective element of the index will be pushed onto the Stack for
   * subsequent {@link app.coronawarn.server.services.distribution.structure.Writable#prepare}
   * calls on those files and directories.
   *
   * @param indices A {@link Stack} of parameters from all {@link IndexDirectory IndexDirectories}
   *                further up in the hierarchy. The Stack may contain different types, depending on
   *                the types {@code T} of {@link IndexDirectory IndexDirectories} further up in the
   *                hierarchy.
   */
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
