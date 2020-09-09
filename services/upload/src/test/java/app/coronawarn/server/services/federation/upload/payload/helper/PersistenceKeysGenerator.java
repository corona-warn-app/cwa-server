package app.coronawarn.server.services.federation.upload.payload.helper;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.domain.FederationUploadKey;

import java.util.Random;

public class PersistenceKeysGenerator {

  private static final Random RANDOM = new Random();

  public static FederationUploadKey makeDiagnosisKey() {
    byte[] bytes = new byte[16];
    RANDOM.nextBytes(bytes);
    return FederationUploadKey.from(
        DiagnosisKey.builder()
        .withKeyData(bytes)
        .withRollingStartIntervalNumber(144)
        .withTransmissionRiskLevel(0)
        .build());
  }

}
