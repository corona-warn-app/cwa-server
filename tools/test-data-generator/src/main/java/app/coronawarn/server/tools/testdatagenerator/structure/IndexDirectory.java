package app.coronawarn.server.tools.testdatagenerator.structure;

import app.coronawarn.server.tools.testdatagenerator.util.Common.FileFunction;
import app.coronawarn.server.tools.testdatagenerator.util.Common.Formatter;
import app.coronawarn.server.tools.testdatagenerator.util.Common.IndexFunction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.json.simple.JSONArray;

/**
 * A meta directory that maps its on-disk subdirectories to some list of elements. This list of
 * elements is determined by the {@link IndexDirectory#indexFunction}.
 *
 * @param <T> The type of the elements in the index (e.g. LocalDate for the /date directory)
 */
public class IndexDirectory<T> extends Directory {

  // Files to be written into every directory created through the index
  protected final Map<String, FileFunction> indexFiles = new HashMap<>();
  // Directories to be written into every directory created through the index
  protected final List<Directory> indexDirectories = new ArrayList<>();
  private final IndexFunction<T> indexFunction;
  private final Formatter<T> indexFormatter;

  /**
   * Constructor.
   *
   * @param name           The name that this directory should have on disk.
   * @param indexFunction  An {@link IndexFunction} that calculates the index of this {@link
   *                       IndexDirectory} from a {@link java.util.Stack} of parent {@link
   *                       IndexDirectory} indices. The top element of the stack is from the closest
   *                       {@link IndexDirectory} in the parent chain.
   * @param indexFormatter A {@link Formatter} used to format the directory name for each index
   *                       element returned by the {@link IndexDirectory#indexFunction}.
   */
  public IndexDirectory(String name, IndexFunction<T> indexFunction, Formatter<T> indexFormatter) {
    super(name);
    this.indexFunction = indexFunction;
    this.indexFormatter = indexFormatter;
  }

  /**
   * Constructor that defaults the {@link IndexDirectory#indexFormatter} to {@link
   * Object#toString}.
   */
  public IndexDirectory(String name, IndexFunction<T> indexFunction) {
    this(name, indexFunction, Object::toString);
  }

  public IndexFunction<T> getIndexFunction() {
    return indexFunction;
  }

  public Formatter<T> getIndexFormatter() {
    return indexFormatter;
  }

  /**
   * Adds a file under the name {@code name}, whose content is calculated by the {@code
   * fileFunction} to each one of the directories created from the index. The {@code fileFunction}
   * calculates the file content from a {@link java.util.Stack} of parent {@link IndexDirectory}
   * indices. File content calculation happens on {@link Directory#write}.
   *
   * @param name         The name of the file on disk.
   * @param fileFunction A function that can calculate the content of the file, based on
   * @return self
   */
  public Directory addFileToAll(String name, FileFunction fileFunction) {
    this.indexFiles.put(name, fileFunction);
    return this;
  }

  /**
   * Adds a {@link Directory} to each one of the directories created from the index.
   *
   * @return self
   */
  public Directory addDirectoryToAll(Directory directory) {
    this.indexDirectories.add(directory);
    directory.setParent(this);
    return this;
  }

  @Override
  protected void writeFiles(Stack<Object> indices) {
    this.addIndexFile(indices);
    super.writeFiles(indices);
  }

  @Override
  protected void writeDirectories(Stack<Object> indices) {
    super.writeDirectories(indices);
    this.writeIndexDirectories(indices);
  }

  private void writeIndexDirectories(Stack<Object> indices) {
    Stream.of(indices)
        .map(this.getIndexFunction())
        .flatMap(List::stream)
        .forEach(currentIndex -> {
          Stack<Object> newIndices = (Stack<Object>) indices.clone();
          newIndices.push(currentIndex);
          Stream.of(currentIndex)
              .map(this.getIndexFormatter())
              .map(Object::toString)
              .map(Directory::new)
              .peek(directory -> {
                directory.setParent(this);
                this.indexFiles.forEach((name, fileFunction) -> directory
                    .addFile(name, fileFunction.apply(newIndices)));
                this.indexDirectories.forEach(directory::addDirectory);
              })
              .forEach(directory -> directory.write(newIndices));
        });
  }

  /**
   * Writes a file called {@code "index"}, containing a JSON String of an array containing all
   * elements returned by the {@link IndexDirectory#indexFunction}, formatted with the {@link
   * IndexDirectory#indexFormatter}.
   */
  private void addIndexFile(Stack<Object> indices) {
    Stream.of(indices)
        .map(this.getIndexFunction())
        .forEach(index -> {
          JSONArray array = new JSONArray();
          List<?> elements = index.stream()
              .map(this.getIndexFormatter())
              .collect(Collectors.toList());
          array.addAll(elements);
          this.addFile("index", array.toJSONString().getBytes());
        });
  }
}
