package app.coronawarn.server.services.distribution.statistics.file;

import app.coronawarn.server.services.distribution.statistics.StatisticType;
import java.util.Optional;

public interface StatisticJsonFileLoader extends JsonFileLoader {

  /**
   * Returns the content of the file. Can be loaded from the local filesystem or a remote storage.
   *
   * @param statisticType - enum for LOCAL or STANDARD statistics
   * @return String encoded JSON content
   *
   */
  JsonFile getFile(StatisticType statisticType);

  default JsonFile getFile() {
    return getFile(StatisticType.STANDARD);
  }

  /**
   * Returns the content of the file only if etag was updated. Otherwise returns Optional.empty.
   *
   * @param statisticType - enum for LOCAL or STANDARD statistics
   * @param etag - file etag
   * @return - String encoded JSON content
   */
  Optional<JsonFile> getFileIfUpdated(StatisticType statisticType, String etag);

  default Optional<JsonFile> getFileIfUpdated(String etag) {
    return getFileIfUpdated(StatisticType.STANDARD, etag);
  }


}
