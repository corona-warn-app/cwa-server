package app.coronawarn.server.common.persistence.domain;

import org.springframework.data.annotation.Id;

public class TraceTimeIntervalWarning {

  @Id
  private Long id;
  private final byte[] traceLocationGuid;
  private final Integer startIntervalNumber;
  private final Integer endIntervalNumber;
  private final Integer transmissionRiskLevel;

  /**
   * Constructor for a TraceTimeIntervalWarning.
   *
   * @param traceLocationGuid     the id/guid of the corresponding TraceLocation.
   * @param startIntervalNumber   the starting interval.
   * @param endIntervalNumber     the ending inteval.
   * @param transmissionRiskLevel the transmission risk level.
   */
  public TraceTimeIntervalWarning(byte[] traceLocationGuid, Integer startIntervalNumber, Integer endIntervalNumber,
      Integer transmissionRiskLevel) {
    this.traceLocationGuid = traceLocationGuid;
    this.startIntervalNumber = startIntervalNumber;
    this.endIntervalNumber = endIntervalNumber;
    this.transmissionRiskLevel = transmissionRiskLevel;
  }

  public Long getId() {
    return id;
  }


  public byte[] getTraceLocationGuid() {
    return traceLocationGuid;
  }

  public Integer getStartIntervalNumber() {
    return startIntervalNumber;
  }

  public Integer getEndIntervalNumber() {
    return endIntervalNumber;
  }

  public Integer getTransmissionRiskLevel() {
    return transmissionRiskLevel;
  }
}
