package app.coronawarn.server.common.persistence.domain;

public enum FederationBatchStatus {
  UNPROCESSED,
  PROCESSED,
  ERROR,
  ERROR_WONT_RETRY
}
