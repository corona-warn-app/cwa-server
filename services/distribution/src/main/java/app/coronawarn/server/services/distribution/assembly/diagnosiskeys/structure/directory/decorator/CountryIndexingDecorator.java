package app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.directory.decorator;

import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.decorator.IndexDirectoryDecorator;
import app.coronawarn.server.services.distribution.assembly.structure.file.FileOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import org.json.simple.JSONArray;

public class CountryIndexingDecorator<T> extends IndexDirectoryDecorator<T, WritableOnDisk> {


  public CountryIndexingDecorator(IndexDirectory<T, WritableOnDisk> directory) {
    super(directory);
  }

  @Override
  public void prepare(ImmutableStack<Object> indices) {

    Collection<DirectoryOnDisk> directories = this.getWritables().stream()
        .filter(Writable::isDirectory)
        .map(directory -> (DirectoryOnDisk) directory)
        .collect(Collectors.toSet());

    directories.forEach(this::writeIndexFile);

    super.prepare(indices);
  }

  public void writeIndexFile(DirectoryOnDisk directory) {

    Collection<String> paths = this.getWritablesInDirectory(directory);

    JSONArray array = new JSONArray();
    array.addAll(paths);

    directory.addWritable(new FileOnDisk("index", array.toJSONString().getBytes()));
  }

  private Set<String> getWritablesInDirectory(Directory<WritableOnDisk> rootDirectory) {

    Collection<DirectoryOnDisk> directories = this.getWritables().stream()
        .filter(Writable::isDirectory)
        .map(directory -> (DirectoryOnDisk) directory)
        .collect(Collectors.toSet());

    if (directories.isEmpty()) {
      return Set.of(this.getName());
    } else {
      return directories.stream()
          .map(this::getWritablesInDirectory)
          .flatMap(Set::stream)
          .map(childName -> this.getName() + "/" + childName)
          .collect(Collectors.toSet());
    }
  }
}
