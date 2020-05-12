package app.coronawarn.server.services.distribution.structure.directory;

import app.coronawarn.server.services.distribution.structure.file.File;
import app.coronawarn.server.services.distribution.structure.Writable;
import java.util.List;

public interface Directory extends Writable {

  void addFile(File file);

  List<File> getFiles();

  void addDirectory(Directory directory);

  List<Directory> getDirectories();
}
