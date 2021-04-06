package app.coronawarn.server.services.distribution.assembly.appconfig.parsing.v2;

import java.util.List;

public class DeserializedPlausibleDeniabilityParameters {
  private List<Integer>  checkInSizesInBytes;

  public List<Integer> getCheckInSizesInBytes() {
    return checkInSizesInBytes;
  }

  public void setCheckInSizesInBytes(List<Integer> checkInSizesInBytes) {
    this.checkInSizesInBytes = checkInSizesInBytes;
  }
}
