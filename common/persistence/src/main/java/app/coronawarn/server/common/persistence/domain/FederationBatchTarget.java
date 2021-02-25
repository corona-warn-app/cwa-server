package app.coronawarn.server.common.persistence.domain;

public enum FederationBatchTarget {
  EFGS("efgs"),
  SGS("sgs");

  private final String targetSystem;

  FederationBatchTarget(String targetSystem) {
    this.targetSystem = targetSystem;
  }
}
