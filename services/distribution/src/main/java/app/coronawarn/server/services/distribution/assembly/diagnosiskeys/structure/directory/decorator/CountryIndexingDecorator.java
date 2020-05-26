package app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.directory.decorator;

import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.decorator.indexing.IndexingDecoratorOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.file.FileOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import org.json.simple.JSONArray;

public class CountryIndexingDecorator<T> extends IndexingDecoratorOnDisk<T> {

  public CountryIndexingDecorator(IndexDirectory<T, WritableOnDisk> directory, String indexFileName) {
    super(directory, indexFileName);
  }

  @Override
  public FileOnDisk getIndexFile(String indexFileName, ImmutableStack<Object> indices) {

    Collection<String> paths = this.getWritablesInDirectory(this).stream()
        .map(Writable::getName)
        .collect(Collectors.toSet());

    JSONArray array = new JSONArray();
    array.addAll(paths);

    return new FileOnDisk(indexFileName, array.toJSONString().getBytes());
  }

  private Set<Writable<WritableOnDisk>> getWritablesInDirectory(Directory<WritableOnDisk> rootDirectory) {

    Collection<DirectoryOnDisk> directories = this.getWritables().stream()
        .filter(Writable::isDirectory)
        .map(directory -> (DirectoryOnDisk) directory)
        .collect(Collectors.toSet());

    if (directories.isEmpty()) {
      return Set.of(this);
    } else {
      return directories.stream()
          .map(this::getWritablesInDirectory)
          .flatMap(Set::stream)
          .collect(Collectors.toSet());
    }
  }
}
