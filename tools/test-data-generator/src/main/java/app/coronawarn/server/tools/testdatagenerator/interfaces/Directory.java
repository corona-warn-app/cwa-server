package app.coronawarn.server.tools.testdatagenerator.interfaces;

import java.util.List;

public interface Directory extends Writable {

  void addFile(File file);

  List<File> getFiles();

  void addDirectory(Directory directory);

  List<Directory> getDirectories();
}
