package app.coronawarn.server.services.distribution.common;

import static org.junit.jupiter.api.Assertions.fail;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.exception.InvalidDiagnosisKeyException;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class Helpers {

  public static void prepareAndWrite(Directory directory) {
    directory.prepare(new ImmutableStack<>());
    directory.write();
  }

  public static DiagnosisKey buildDiagnosisKeyForSubmissionTimestamp(long submissionTimeStamp) {
    try {
      return DiagnosisKey.builder()
          .withKeyData(new byte[16])
          .withRollingStartNumber(0L)
          .withRollingPeriod(1L)
          .withTransmissionRiskLevel(2)
          .withSubmissionTimestamp(submissionTimeStamp).build();
    } catch (InvalidDiagnosisKeyException e) {
      fail("The diagnosis key is not valid.");
      return null;
    }
  }

  public static DiagnosisKey buildDiagnosisKeyForDateTime(LocalDateTime dateTime) {
    return buildDiagnosisKeyForSubmissionTimestamp(dateTime.toEpochSecond(ZoneOffset.UTC) / 3600);
  }
}
