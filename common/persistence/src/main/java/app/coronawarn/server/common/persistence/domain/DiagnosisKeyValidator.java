package app.coronawarn.server.common.persistence.domain;

import static java.time.ZoneOffset.UTC;

import app.coronawarn.server.common.persistence.exception.InvalidDiagnosisKeyException;
import java.time.Instant;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiagnosisKeyValidator {
  private static final Logger logger = LoggerFactory.getLogger(DiagnosisKeyValidator.class);

  private static final Integer MIN_RISK_LEVEL = 0;
  private static final Integer MAX_RISK_LEVEL = 8;

  public static void validateTransmissionLevel(int transmissionRiskLevel) throws InvalidDiagnosisKeyException {
    if (transmissionRiskLevel < MIN_RISK_LEVEL || transmissionRiskLevel > MAX_RISK_LEVEL) {
      logger.debug("Risk level {} is not allowed. Must be between {} and {}.", MIN_RISK_LEVEL, MAX_RISK_LEVEL, transmissionRiskLevel);
      throw new InvalidDiagnosisKeyException(
          String.format("Risk level %s is not allowed. Must be between %s and %s.", MIN_RISK_LEVEL, MAX_RISK_LEVEL, transmissionRiskLevel));
    }
  }

  public static void validateRollingStartNumber(long rollingStartNumber) throws InvalidDiagnosisKeyException {
    if (rollingStartNumber % (60 * 10) != 0) {
      logger.debug("Rolling start number must be start of rolling period, i.e. an increment of 60 * 10.");
      throw new InvalidDiagnosisKeyException("Rolling start number must be start of rolling period, i.e. an increment of 60 * 10.");
    }

    long currentInstant = LocalDateTime.ofInstant(Instant.now(), UTC).toEpochSecond(UTC);
    if (rollingStartNumber > currentInstant) {
      logger.debug("Rolling start cannot be in the future.");
      throw new InvalidDiagnosisKeyException("Rolling start cannot be in the future.");
    }
  }

  public static void validateRollingPeriod(long rollingPeriod) throws InvalidDiagnosisKeyException {
    if (rollingPeriod < 0) {
      logger.debug("Rolling period cannot be 0.");
      throw new InvalidDiagnosisKeyException("Rolling period cannot be 0.");
    }
  }

  public static void validateKeyData(byte[] keyData) throws InvalidDiagnosisKeyException {

  }
}
