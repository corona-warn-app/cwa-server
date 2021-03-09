package app.coronawarn.server.common.persistence.eventregistration.domain;

import org.springframework.data.annotation.Id;

public class TraceLocation {

  @Id
  private Long id;
  private final byte[] traceLocationGuidHash;
  private final Long createdAt;
  private final Integer version;

  /**
   * Constructor for Trace Location.
   *
   * @param traceLocationGuidHash the SHA-256 hashed guid of the trace location.
   * @param createdAt             when the TraceLocation was created.
   * @param version               the version provided by the client.
   */
  public TraceLocation(byte[] traceLocationGuidHash, Long createdAt, Integer version) {
    this.traceLocationGuidHash = traceLocationGuidHash;
    this.createdAt = createdAt;
    this.version = version;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getId() {
    return id;
  }

  public byte[] getTraceLocationGuidHash() {
    return traceLocationGuidHash;
  }

  public Long getCreatedAt() {
    return createdAt;
  }

  public Integer getVersion() {
    return version;
  }
}
