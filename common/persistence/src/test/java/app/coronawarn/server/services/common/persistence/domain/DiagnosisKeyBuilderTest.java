package app.coronawarn.server.services.common.persistence.domain;

import app.coronawarn.server.common.protocols.generated.ExposureKeys.TemporaryExposureKey;
import com.google.protobuf.ByteString;
import java.nio.charset.Charset;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DiagnosisKeyBuilderTest {
  private final byte[] expKeyData = "myByteArr".getBytes(Charset.defaultCharset());
  private final long expRollingStartNumber = 123;
  private final int expTransmissionRiskLevel = 1;

  @Test
  public void buildFromProtoBufObj() {
    TemporaryExposureKey protoBufObj = TemporaryExposureKey
        .newBuilder()
        .setKeyData(ByteString.copyFrom(this.expKeyData))
        .setRollingStartNumber(this.expRollingStartNumber)
        .setRiskLevelValue(this.expTransmissionRiskLevel)
        .build();

    DiagnosisKey actDiagnosisKey = DiagnosisKey.builder().fromProtoBuf(protoBufObj).build();

    assertArrayEquals(this.expKeyData, actDiagnosisKey.getKeyData());
    assertEquals(this.expRollingStartNumber, actDiagnosisKey.getRollingStartNumber());
    assertEquals(this.expTransmissionRiskLevel, actDiagnosisKey.getTransmissionRiskLevel());
  }

  @Test
  public void buildSuccessively() {
    DiagnosisKey actDiagnosisKey = DiagnosisKey.builder().keyData(this.expKeyData)
        .rollingStartNumber(this.expRollingStartNumber)
        .transmissionRiskLevel(this.expTransmissionRiskLevel).build();

    assertArrayEquals(this.expKeyData, actDiagnosisKey.getKeyData());
    assertEquals(this.expRollingStartNumber, actDiagnosisKey.getRollingStartNumber());
    assertEquals(this.expTransmissionRiskLevel, actDiagnosisKey.getTransmissionRiskLevel());
  }
}
