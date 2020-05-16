package app.coronawarn.server.common.persistence.domain;

import static java.time.ZoneOffset.UTC;

import app.coronawarn.server.common.persistence.exception.InvalidDiagnosisKeyException;
import java.time.Instant;
import java.time.LocalDateTime;

public class DiagnosisKeyValidator {

  private static final Integer MIN_RISK_LEVEL = 0;
  private static final Integer MAX_RISK_LEVEL = 8;

  public static void validateTransmissionLevel(int transmissionRiskLevel) throws InvalidDiagnosisKeyException {
    if (transmissionRiskLevel < MIN_RISK_LEVEL || transmissionRiskLevel > MAX_RISK_LEVEL) {
      throw new InvalidDiagnosisKeyException(
          String.format("Risk level %s is not allowed. Must be between %s and %s.", MIN_RISK_LEVEL, MAX_RISK_LEVEL, transmissionRiskLevel));
    }
  }

  public static void validateRollingStartNumber(long rollingStartNumber) throws InvalidDiagnosisKeyException {
    if (rollingStartNumber % (60 * 10) != 0) {
      throw new InvalidDiagnosisKeyException("Rolling start number must be start of rolling period, i.e. an increment of 60 * 10.");
    }

    long currentInstant = LocalDateTime.ofInstant(Instant.now(), UTC).toEpochSecond(UTC);
    if (rollingStartNumber > currentInstant) {
      throw new InvalidDiagnosisKeyException("Rolling start cannot be in the future.");
    }
  }

  public static void validateRollingPeriod(long rollingPeriod) throws InvalidDiagnosisKeyException {
    if (rollingPeriod < 0) {
      throw new InvalidDiagnosisKeyException("Rolling period cannot be 0.");
    }
  }

  public static void validateKeyData(byte[] keyData) throws InvalidDiagnosisKeyException {

  }
}
