package app.coronawarn.server.services.distribution.assembly.structure.directory.decorator.indexing;

import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.file.FileOnDisk;

public class IndexingDecoratorOnDisk<T> extends AbstractIndexingDecorator<T, WritableOnDisk>
    implements IndexingDecorator<T, WritableOnDisk> {

  public IndexingDecoratorOnDisk(IndexDirectoryOnDisk<T> directory) {
    super(directory);
  }

  @Override
  public FileOnDisk getIndexFile(String indexFileName) {
    // TODO
    return null;
  }
}
