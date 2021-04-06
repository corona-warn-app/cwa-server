package app.coronawarn.server.services.distribution.assembly.appconfig.parsing.v2;

import java.util.List;

public class DeserializedRevokedTraceLocationVersions {
  private List<Integer> revokedTraceLocationVersions;

  public List<Integer> getRevokedTraceLocationVersions() {
    return revokedTraceLocationVersions;
  }

  public void setRevokedTraceLocationVersions(List<Integer> revokedTraceLocationVersions) {
    this.revokedTraceLocationVersions = revokedTraceLocationVersions;
  }
}
