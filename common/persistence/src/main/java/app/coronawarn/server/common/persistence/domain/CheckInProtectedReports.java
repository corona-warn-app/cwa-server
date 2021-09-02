package app.coronawarn.server.common.persistence.domain;

import app.coronawarn.server.common.persistence.domain.validation.ValidSubmissionTimestamp;
import org.springframework.data.annotation.Id;

public class CheckInProtectedReports {

  @Id
  private Long id;
  private final byte[] traceLocationIdHash;
  private final byte[] initializationVector;
  private final byte[] encryptedCheckInRecord;
  private final byte[] mac;

  @ValidSubmissionTimestamp
  private final long submissionTimestamp;

  /**
   * Constructs db model for CheckInProtectedReports encrypted checkins.
   * @param traceLocationIdHash The id/guid of the corresponding TraceLocation
   * @param initializationVector The received initialization vector
   * @param encryptedCheckInRecord The received encrypted CheckInRecord
   * @param submissionTimestamp The time when the trace warning was stored on the server
   */
  public CheckInProtectedReports(byte[] traceLocationIdHash, byte[] initializationVector, byte[] encryptedCheckInRecord,
      byte[] mac, long submissionTimestamp) {
    this.traceLocationIdHash = traceLocationIdHash;
    this.initializationVector = initializationVector;
    this.encryptedCheckInRecord = encryptedCheckInRecord;
    this.mac = mac;
    this.submissionTimestamp = submissionTimestamp;
  }

  public Long getId() {
    return id;
  }

  public byte[] getTraceLocationIdHash() {
    return traceLocationIdHash;
  }

  public byte[] getInitializationVector() {
    return initializationVector;
  }

  public byte[] getEncryptedCheckInRecord() {
    return encryptedCheckInRecord;
  }

  public long getSubmissionTimestamp() {
    return submissionTimestamp;
  }

  public byte[] getMac() {
    return mac;
  }
}
