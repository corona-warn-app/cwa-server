package app.coronawarn.server.services.distribution.assembly.structure.directory.decorator.indexing;

import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectory;
import app.coronawarn.server.services.distribution.assembly.structure.file.File;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;

public interface IndexingDecorator<T, W extends Writable<W>> extends IndexDirectory<T, W> {

  File<W> getIndexFile(String indexFileName, ImmutableStack<Object> indices);

}
