package app.coronawarn.server.services.distribution.objectstore.publish;

import java.nio.file.Path;

public class LocalGenericFile extends LocalFile {

  public LocalGenericFile(Path file, Path basePath) {
    super(file, basePath);
  }
}
