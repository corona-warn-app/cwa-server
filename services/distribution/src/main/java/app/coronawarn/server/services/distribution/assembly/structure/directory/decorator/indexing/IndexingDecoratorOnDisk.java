package app.coronawarn.server.services.distribution.assembly.structure.directory.decorator.indexing;

import app.coronawarn.server.common.shared.collection.ImmutableStack;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectory;
import app.coronawarn.server.services.distribution.assembly.structure.file.FileOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.file.FileOnDiskWithChecksum;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.json.simple.JSONArray;

public class IndexingDecoratorOnDisk<T> extends AbstractIndexingDecorator<T, WritableOnDisk>
    implements IndexingDecorator<T, WritableOnDisk> {

  public IndexingDecoratorOnDisk(IndexDirectory<T, WritableOnDisk> directory, String indexFileName) {
    super(directory, indexFileName);
  }

  @SuppressWarnings("unchecked")
  @Override
  public FileOnDisk getIndexFile(String indexFileName, ImmutableStack<Object> indices) {
    Set<T> index = this.getIndex(indices);
    JSONArray array = new JSONArray();
    List<?> elements = index.stream()
        .map(this.getIndexFormatter())
        .sorted()
        .collect(Collectors.toList());
    array.addAll(elements);
    return new FileOnDiskWithChecksum(indexFileName, array.toJSONString().getBytes(StandardCharsets.UTF_8));
  }
}
