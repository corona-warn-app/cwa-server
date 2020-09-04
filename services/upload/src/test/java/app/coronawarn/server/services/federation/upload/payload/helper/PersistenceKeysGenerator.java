package app.coronawarn.server.services.federation.upload.payload.helper;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import java.util.Random;

public class PersistenceKeysGenerator {

  private static final Random RANDOM = new Random();

  public static DiagnosisKey makeDiagnosisKey() {
    byte[] bytes = new byte[16];
    RANDOM.nextBytes(bytes);
    return DiagnosisKey.builder()
        .withKeyData(bytes)
        .withRollingStartIntervalNumber(144)
        .withTransmissionRiskLevel(0)
        .build();
  }

}
