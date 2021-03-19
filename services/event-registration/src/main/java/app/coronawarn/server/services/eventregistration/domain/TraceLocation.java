package app.coronawarn.server.services.eventregistration.domain;

import org.springframework.data.annotation.Id;

public class TraceLocation {

  @Id
  private final String traceLocationGuidHash;
  private final Long createdAt;
  private final Integer version;

  /**
   * Constructor for Trace Location.
   *
   * @param traceLocationGuidHash the SHA-256 hashed guid of the trace location.
   * @param createdAt             when the TraceLocation was created.
   * @param version               the version provided by the client.
   */
  public TraceLocation(String traceLocationGuidHash, Long createdAt, Integer version) {
    this.traceLocationGuidHash = traceLocationGuidHash;
    this.createdAt = createdAt;
    this.version = version;
  }


  public String getTraceLocationGuidHash() {
    return traceLocationGuidHash;
  }

  public Long getCreatedAt() {
    return createdAt;
  }

  public Integer getVersion() {
    return version;
  }
}
