package app.coronawarn.server.services.distribution.structure.directory.decorator;

import app.coronawarn.server.services.distribution.structure.directory.IndexDirectory;
import app.coronawarn.server.services.distribution.structure.directory.IndexDirectoryImpl;
import app.coronawarn.server.services.distribution.structure.file.FileImpl;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;
import org.json.simple.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link DirectoryDecorator} that writes a file called {@code "index"}, containing a JSON array
 * containing all elements returned {@link IndexDirectoryImpl#getIndex}, formatted with the {@link
 * IndexDirectoryImpl#getIndexFormatter} on {@link app.coronawarn.server.services.distribution.structure.Writable#prepare}.
 */
public class IndexingDecorator<T> extends DirectoryDecorator {

  private static final String INDEX_FILE_NAME = "index";

  private static final Logger logger = LoggerFactory.getLogger(IndexingDecorator.class);
  final IndexDirectory<T> directory;

  public IndexingDecorator(IndexDirectory<T> directory) {
    super(directory);
    this.directory = directory;
  }

  /**
   * See {@link IndexingDecorator} class documentation.
   */
  @Override
  public void prepare(Stack<Object> indices) {
    logger.debug("Indexing {}", this.getFileOnDisk().getPath());
    List<T> index = this.directory.getIndex(indices);
    JSONArray array = new JSONArray();
    List<?> elements = index.stream()
        .map(this.directory.getIndexFormatter())
        .collect(Collectors.toList());
    array.addAll(elements);
    this.addFile(new FileImpl(INDEX_FILE_NAME, array.toJSONString().getBytes()));
    super.prepare(indices);
  }
}
