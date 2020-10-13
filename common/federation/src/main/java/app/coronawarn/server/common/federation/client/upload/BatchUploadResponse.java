
package app.coronawarn.server.common.federation.client.upload;

import static java.util.Collections.emptyList;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Multi-Status response from Upload. The payload returns three properties (201, 409 and 500) each property contains
 * a list of indexes. The index refers to the key position on the ordered Diagnosis Keys from UploadPayload.
 *  201 -> Successfully added               DO NOTHING
 *  409 -> Conflict: Key was already added  DO NOTHING
 *  500 -> Server Error: Key not processed  RETRY
 */
public class BatchUploadResponse {

  @JsonProperty("409")
  private List<String> status409 = emptyList();

  @JsonProperty("500")
  private List<String> status500 = emptyList();

  @JsonProperty("201")
  private List<String> status201 = emptyList();

  /**
   * Create the BatchUploadResponse.
   */
  public BatchUploadResponse(List<String> status409, List<String> status500, List<String> status201) {
    this.status409 = status409;
    this.status500 = status500;
    this.status201 = status201;
  }

  @Override
  public String toString() {
    return "BatchUploadResponse{"
        + "status409="
        + status409
        + ", status500="
        + status500
        + ", status201="
        + status201
        + '}';
  }

  /**
   * Create an empty BatchUploadResponse.
   */
  public BatchUploadResponse() {
  }

  public List<String> getStatus409() {
    return status409;
  }

  public List<String> getStatus500() {
    return status500;
  }

  public List<String> getStatus201() {
    return status201;
  }

}
