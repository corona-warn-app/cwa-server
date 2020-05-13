package app.coronawarn.server.services.distribution.structure.directory;

import app.coronawarn.server.services.distribution.structure.Writable;
import app.coronawarn.server.services.distribution.structure.file.File;
import java.util.Set;

/**
 * A {@link Writable} containing {@link File files} and further {@link Directory directories}.
 */
public interface Directory extends Writable {

  /**
   * Adds a {@link File file} to the {@link DirectoryImpl#getFiles files} of this {@link
   * Directory}.
   */
  void addFile(File file);

  /**
   * Returns all {@link File files} contained in this {@link Directory}.
   */
  Set<File> getFiles();

  /**
   * Adds a {@link Directory directory} to the {@link DirectoryImpl#getDirectories directories} of
   * this {@link Directory}.
   */
  void addDirectory(Directory directory);


  /**
   * Returns all {@link Directory directories} contained in this {@link Directory}.
   */
  Set<Directory> getDirectories();
}
