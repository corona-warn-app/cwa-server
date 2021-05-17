

package app.coronawarn.server.services.distribution.assembly.structure.directory.decorator.indexing;

import app.coronawarn.server.common.shared.collection.ImmutableStack;
import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.decorator.IndexDirectoryDecorator;

public abstract class AbstractIndexingDecorator<T, W extends Writable<W>> extends
    IndexDirectoryDecorator<T, W> implements IndexingDecorator<T, W> {

  final IndexDirectory<T, W> directory;
  private final String indexFileName;

  /**
   * Creates a new AbstractIndexingDecorator.
   *
   * @param directory of generic type
   * @param indexFileName file name
   */
  protected AbstractIndexingDecorator(IndexDirectory<T, W> directory, String indexFileName) {
    super(directory);
    this.directory = directory;
    this.indexFileName = indexFileName;
  }

  /**
   * See {@link AbstractIndexingDecorator} class documentation.
   *
   * @param indices stack of objects
   */
  @Override
  public void prepare(ImmutableStack<Object> indices) {
    this.addWritable(this.getIndexFile(indexFileName, indices));
    super.prepare(indices);
  }
}
