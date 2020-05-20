package app.coronawarn.server.services.distribution.assembly.structure.directory.decorator.indexing;

import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectory;
import app.coronawarn.server.services.distribution.assembly.structure.file.FileOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.json.simple.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndexingDecoratorOnDisk<T> extends AbstractIndexingDecorator<T, WritableOnDisk>
    implements IndexingDecorator<T, WritableOnDisk> {

  private static final Logger logger = LoggerFactory.getLogger(IndexingDecoratorOnDisk.class);

  public IndexingDecoratorOnDisk(IndexDirectory<T, WritableOnDisk> directory) {
    super(directory);
  }

  @Override
  public FileOnDisk getIndexFile(String indexFileName, ImmutableStack<Object> indices) {
    Set<T> index = this.directory.getIndex(indices);
    JSONArray array = new JSONArray();
    List<?> elements = index.stream()
        .map(this.directory.getIndexFormatter())
        .collect(Collectors.toList());
    array.addAll(elements);
    return new FileOnDisk(indexFileName, array.toJSONString().getBytes());
  }
}
