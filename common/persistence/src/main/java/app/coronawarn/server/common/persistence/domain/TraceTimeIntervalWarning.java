package app.coronawarn.server.common.persistence.domain;

import app.coronawarn.server.common.persistence.domain.validation.ValidSubmissionTimestamp;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload.SubmissionType;
import javax.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;

public class TraceTimeIntervalWarning {

  @Id
  private Long id;
  private final byte[] traceLocationId;
  private final Integer startIntervalNumber;
  private final Integer period;
  private final Integer transmissionRiskLevel;
  @NotNull
  private final SubmissionType submissionType;

  @ValidSubmissionTimestamp
  private final long submissionTimestamp;

  /**
   * Constructor for a TraceTimeIntervalWarning.
   *  @param traceLocationId     the id/guid of the corresponding TraceLocation.
   * @param startIntervalNumber   the starting interval.
   * @param period                the period interval between endIntervalNumber - startIntervalNumber.
   * @param transmissionRiskLevel the transmission risk level.
   * @param submissionTimestamp   The time when the trace warning was stored on the server in the format of
   * @param submissionType        The submission type
   */
  public TraceTimeIntervalWarning(byte[] traceLocationId, Integer startIntervalNumber,
      Integer period, Integer transmissionRiskLevel, long submissionTimestamp,
      SubmissionType submissionType) {
    this.traceLocationId = traceLocationId;
    this.startIntervalNumber = startIntervalNumber;
    this.period = period;
    this.transmissionRiskLevel = transmissionRiskLevel;
    this.submissionTimestamp = submissionTimestamp;
    this.submissionType = submissionType;
  }

  public Long getId() {
    return id;
  }

  public byte[] getTraceLocationId() {
    return traceLocationId;
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

  public SubmissionType getSubmissionType() {
    return submissionType;
  }
}
