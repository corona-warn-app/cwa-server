

package app.coronawarn.server.services.distribution.assembly.structure.directory.decorator.indexing;

import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.decorator.IndexDirectoryDecorator;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;

public abstract class AbstractIndexingDecorator<T, W extends Writable<W>> extends
    IndexDirectoryDecorator<T, W> implements IndexingDecorator<T, W> {

  final IndexDirectory<T, W> directory;
  private final String indexFileName;

  /**
   * Creates a new AbstractIndexingDecorator.
   */
  protected AbstractIndexingDecorator(IndexDirectory<T, W> directory, String indexFileName) {
    super(directory);
    this.directory = directory;
    this.indexFileName = indexFileName;
  }

  /**
   * See {@link AbstractIndexingDecorator} class documentation.
   */
  @Override
  public void prepare(ImmutableStack<Object> indices) {
    this.addWritable(this.getIndexFile(indexFileName, indices));
    super.prepare(indices);
  }
}
