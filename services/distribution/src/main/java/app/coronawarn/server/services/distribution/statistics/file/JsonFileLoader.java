package app.coronawarn.server.services.distribution.statistics.file;

import java.util.Optional;

public interface JsonFileLoader {

  /**
   * Returns the content of the file. Can be loaded from the local filesystem or a remote storage.
   * @return String encoded JSON content
   */
  JsonFile getFile();

  /**
   * Returns the content of the file only if eTag was updated.
   * @return String encoded JSON content
   */
  Optional<JsonFile> getFileIfUpdated(String eTag);

}
