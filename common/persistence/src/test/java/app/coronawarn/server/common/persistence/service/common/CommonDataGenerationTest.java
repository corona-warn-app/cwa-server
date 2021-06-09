package app.coronawarn.server.common.persistence.service.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.shared.util.HashUtils;
import com.google.protobuf.ByteString;
import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationArguments;

class CommonDataGenerationTest {

  static UnitTestCommonDataGeneration t = new UnitTestCommonDataGeneration(1);

  @Test
  void testGenerateDiagnosisKeyBytesNotNull() {
    assertNotNull(HashUtils.generateSecureRandomByteArrayData(16));
  }

  @Test
  void testGenerateDiagnosisKeyNotNull() {
    assertNotNull(t.generateDiagnosisKey(1, ""));
  }

  @Test
  void testGenerateTransmissionRiskLevelNotUnspecifiedOrUnrecognized() {
    assertNotEquals(0, t.generateTransmissionRiskLevel());
    assertNotEquals(-1, t.generateTransmissionRiskLevel());
  }

  @Test
  void getRandomBetween() {
    assertEquals(0, t.getRandomBetween(0, 0));
  }

  @Test
  void testCorrectGenerateRollingStartIntervalNumber() {
    assertEquals(576, t.generateRollingStartIntervalNumber(100));
  }

  private static class UnitTestCommonDataGeneration extends CommonDataGeneration<DiagnosisKey> {

    protected UnitTestCommonDataGeneration(Integer retentionDays) {
      super(retentionDays);
    }

    @Override
    protected DiagnosisKey generateDiagnosisKey(long submissionTimestamp, String country) {
      return DiagnosisKey.builder().fromFederationDiagnosisKey(
          app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKey.newBuilder()
              .addVisitedCountries("DE")
              .setRollingStartIntervalNumber(123123)
              .setRollingPeriod(1)
              .setOrigin("DE")
              .setKeyData(ByteString.copyFrom(HashUtils.generateSecureRandomByteArrayData(16)))
              .setTransmissionRiskLevel(1)
              .build()
      )
          .build();
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

    }
  }
}
