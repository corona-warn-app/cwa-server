package app.coronawarn.server.common.persistence.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.Charset;
import org.junit.jupiter.api.Test;

public class DiagnosisKeyTest {

  final static byte[] expKeyData = "testKey111111111".getBytes(Charset.defaultCharset());
  final static long expRollingStartNumber = 1L;
  final static long expRollingPeriod = 2L;
  final static int expTransmissionRiskLevel = 3;
  final static DiagnosisKey diagnosisKey = new DiagnosisKey(
      expKeyData, expRollingStartNumber, expRollingPeriod, expTransmissionRiskLevel);

  @Test
  public void testRollingStartNumberGetter() {
    assertEquals(expRollingStartNumber, diagnosisKey.getRollingStartNumber());
  }

  @Test
  public void testRollingPeriodGetter() {
    assertEquals(expRollingPeriod, diagnosisKey.getRollingPeriod());
  }

  @Test
  public void testTransmissionRiskLevelGetter() {
    assertEquals(expTransmissionRiskLevel, diagnosisKey.getTransmissionRiskLevel());
  }
}
