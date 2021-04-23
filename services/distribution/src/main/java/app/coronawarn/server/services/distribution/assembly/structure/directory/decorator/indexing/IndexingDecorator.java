

package app.coronawarn.server.services.distribution.assembly.structure.directory.decorator.indexing;

import app.coronawarn.server.common.shared.collection.ImmutableStack;
import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.decorator.IndexDirectoryDecorator;
import app.coronawarn.server.services.distribution.assembly.structure.file.File;

/**
 * An {@link IndexDirectoryDecorator} that writes a file called {@code "index"}, containing a JSON array containing all
 * elements returned by {@link IndexDirectoryOnDisk#getIndex}, formatted with the {@link
 * IndexDirectoryOnDisk#getIndexFormatter} on {@link Writable#prepare}.
 */
public interface IndexingDecorator<T, W extends Writable<W>> extends IndexDirectory<T, W> {

  /**
   * Returns the file containing the index.
   *
   * @param indexFileName index name of the file
   * @param indices stack of indices objects
   * @return File generic type
   */
  File<W> getIndexFile(String indexFileName, ImmutableStack<Object> indices);

}
