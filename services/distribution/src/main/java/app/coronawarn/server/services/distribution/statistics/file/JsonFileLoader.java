package app.coronawarn.server.services.distribution.statistics.file;

public interface JsonFileLoader {

  /**
   * Returns the content of the file. Can be loaded from the local filesystem or a remote storage.
   * @return String encoded JSON content
   */
  String getContent();

}
