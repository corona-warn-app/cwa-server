package app.coronawarn.server.common.persistence.domain;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import app.coronawarn.server.common.persistence.exception.InvalidDiagnosisKeyException;
import app.coronawarn.server.common.protocols.external.exposurenotification.Key;
import com.google.protobuf.ByteString;
import java.nio.charset.Charset;
import java.time.Instant;
import org.junit.jupiter.api.Test;

public class DiagnosisKeyBuilderTest {

  private final byte[] expKeyData = "16-bytelongarray".getBytes(Charset.defaultCharset());
  private final long expRollingStartNumber = 73800;
  private final long expRollingPeriod = 144;
  private final int expTransmissionRiskLevel = 1;
  private final long expSubmissionTimestamp = 2L;

  @Test
  public void buildFromProtoBufObjWithSubmissionTimestamp() {
    Key protoBufObj = Key
        .newBuilder()
        .setKeyData(ByteString.copyFrom(this.expKeyData))
        .setRollingStartNumber(Long.valueOf(this.expRollingStartNumber).intValue())
        .setRollingPeriod(Long.valueOf(this.expRollingPeriod).intValue())
        .setTransmissionRiskLevel(this.expTransmissionRiskLevel)
        .build();

    DiagnosisKey actDiagnosisKey = null;
    actDiagnosisKey = DiagnosisKey.builder()
        .fromProtoBuf(protoBufObj)
        .withSubmissionTimestamp(this.expSubmissionTimestamp)
        .build();

    assertDiagnosisKeyEquals(actDiagnosisKey, this.expSubmissionTimestamp);
  }

  @Test
  public void buildFromProtoBufObjWithoutSubmissionTimestamp() {
    Key protoBufObj = Key
        .newBuilder()
        .setKeyData(ByteString.copyFrom(this.expKeyData))
        .setRollingStartNumber(Long.valueOf(this.expRollingStartNumber).intValue())
        .setRollingPeriod(Long.valueOf(this.expRollingPeriod).intValue())
        .setTransmissionRiskLevel(this.expTransmissionRiskLevel)
        .build();

    DiagnosisKey actDiagnosisKey = DiagnosisKey.builder().fromProtoBuf(protoBufObj).build();

    assertDiagnosisKeyEquals(actDiagnosisKey);
  }

  @Test
  public void buildSuccessivelyWithSubmissionTimestamp() {
    DiagnosisKey actDiagnosisKey = DiagnosisKey.builder()
        .withKeyData(this.expKeyData)
        .withRollingStartNumber(this.expRollingStartNumber)
        .withRollingPeriod(this.expRollingPeriod)
        .withTransmissionRiskLevel(this.expTransmissionRiskLevel)
        .withSubmissionTimestamp(this.expSubmissionTimestamp).build();

    assertDiagnosisKeyEquals(actDiagnosisKey, this.expSubmissionTimestamp);
  }

  @Test
  public void buildSuccessivelyWithoutSubmissionTimestamp() {
    DiagnosisKey actDiagnosisKey = DiagnosisKey.builder()
        .withKeyData(this.expKeyData)
        .withRollingStartNumber(this.expRollingStartNumber)
        .withRollingPeriod(this.expRollingPeriod)
        .withTransmissionRiskLevel(this.expTransmissionRiskLevel).build();

    assertDiagnosisKeyEquals(actDiagnosisKey);
  }

  @Test
  public void failsForInvalidKeyData() {
    assertThrows(
        InvalidDiagnosisKeyException.class, () ->
            DiagnosisKey.builder()
                .withKeyData("17--bytelongarray".getBytes(Charset.defaultCharset()))
                .withRollingStartNumber(this.expRollingStartNumber)
                .withRollingPeriod(this.expRollingPeriod)
                .withTransmissionRiskLevel(this.expTransmissionRiskLevel).build()
    );
  }

  @Test
  public void failsForInvalidRollingStartNumber() {
    assertThrows(
        InvalidDiagnosisKeyException.class, () ->
            DiagnosisKey.builder()
                .withKeyData(this.expKeyData)
                .withRollingStartNumber(0)
                .withRollingPeriod(this.expRollingPeriod)
                .withTransmissionRiskLevel(this.expTransmissionRiskLevel).build()
    );
  }

  @Test
  public void failsForInvalidRollingPeriod() {
    assertThrows(
        InvalidDiagnosisKeyException.class, () ->
            DiagnosisKey.builder()
                .withKeyData(this.expKeyData)
                .withRollingStartNumber(this.expRollingStartNumber)
                .withRollingPeriod(0)
                .withTransmissionRiskLevel(this.expTransmissionRiskLevel).build()
    );
  }

  @Test
  public void failsForInvalidTransmissionRiskLevel() {
    assertThrows(
        InvalidDiagnosisKeyException.class, () ->
            DiagnosisKey.builder()
                .withKeyData(this.expKeyData)
                .withRollingStartNumber(this.expRollingStartNumber)
                .withRollingPeriod(this.expRollingPeriod)
                .withTransmissionRiskLevel(10).build()
    );
  }

  private void assertDiagnosisKeyEquals(DiagnosisKey actDiagnosisKey) {
    assertDiagnosisKeyEquals(actDiagnosisKey, getCurrentHoursSinceEpoch());
  }

  private long getCurrentHoursSinceEpoch() {
    return Instant.now().getEpochSecond() / 3600L;
  }

  private void assertDiagnosisKeyEquals(DiagnosisKey actDiagnosisKey, long expSubmissionTimestamp) {
    assertEquals(expSubmissionTimestamp, actDiagnosisKey.getSubmissionTimestamp());
    assertArrayEquals(this.expKeyData, actDiagnosisKey.getKeyData());
    assertEquals(this.expRollingStartNumber, actDiagnosisKey.getRollingStartNumber());
    assertEquals(this.expRollingPeriod, actDiagnosisKey.getRollingPeriod());
    assertEquals(this.expTransmissionRiskLevel, actDiagnosisKey.getTransmissionRiskLevel());
  }

}
