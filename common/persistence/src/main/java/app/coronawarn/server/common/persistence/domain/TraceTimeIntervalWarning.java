package app.coronawarn.server.common.persistence.domain;

import app.coronawarn.server.common.persistence.domain.validation.ValidSubmissionTimestamp;
import org.springframework.data.annotation.Id;

public class TraceTimeIntervalWarning {

  @Id
  private Long id;
  private final byte[] traceLocationGuid;
  private final Integer startIntervalNumber;
  private final Integer period;
  private final Integer transmissionRiskLevel;
  @ValidSubmissionTimestamp
  private final long submissionTimestamp;
  /**
   * Constructor for a TraceTimeIntervalWarning.
   *
   * @param traceLocationGuid     the id/guid of the corresponding TraceLocation.
   * @param startIntervalNumber   the starting interval.
   * @param period                the period interval between endIntervalNumber - startIntervalNumber.
   * @param transmissionRiskLevel the transmission risk level.
   */
  public TraceTimeIntervalWarning(byte[] traceLocationGuid, Integer startIntervalNumber, Integer period,
      Integer transmissionRiskLevel, long submissionTimestamp) {
    this.traceLocationGuid = traceLocationGuid;
    this.startIntervalNumber = startIntervalNumber;
    this.period = period;
    this.transmissionRiskLevel = transmissionRiskLevel;
    this.submissionTimestamp = submissionTimestamp;
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

  public Integer getPeriod() {
    return period;
  }

  public Integer getTransmissionRiskLevel() {
    return transmissionRiskLevel;
  }

  public long getSubmissionTimestamp() {
    return submissionTimestamp;
  }
}
