package app.coronawarn.server.common.persistence.domain;

import static java.time.ZoneOffset.UTC;

import app.coronawarn.server.common.persistence.exception.InvalidDiagnosisKeyException;
import java.time.Instant;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validates Keys sent to Submission endpoint as follows.
 *
 * <p><ul>
 * <li>Risk level must be between 0 and 8
 * <li>Rolling start number must be greater than 0
 * <li>Rolling start number cannot be in the future
 * <li>Rolling period must be positive number
 * <li>Key data must be byte array of length 16
 * </ul>
 */
public class DiagnosisKeyValidator {

  private static final Logger logger = LoggerFactory.getLogger(DiagnosisKeyValidator.class);

  private static final Integer MIN_RISK_LEVEL = 0;
  private static final Integer MAX_RISK_LEVEL = 8;

  private DiagnosisKeyValidator() {
  }

  public static void validateDiagnosisKey(DiagnosisKey diagnosisKey) {
    validateKeyData(diagnosisKey.getKeyData());
    validateRollingPeriod(diagnosisKey.getRollingPeriod());
    validateRollingStartNumber(diagnosisKey.getRollingStartNumber());
    validateTransmissionRiskLevel(diagnosisKey.getTransmissionRiskLevel());
  }

  public static void validateTransmissionRiskLevel(int transmissionRiskLevel) {
    if (transmissionRiskLevel < MIN_RISK_LEVEL || transmissionRiskLevel > MAX_RISK_LEVEL) {
      logger.debug("Risk level {} is not allowed. Must be between {} and {}.",
          transmissionRiskLevel, MIN_RISK_LEVEL, MAX_RISK_LEVEL);
      throw new InvalidDiagnosisKeyException(
          String.format("Risk level %s is not allowed. Must be between %s and %s.",
              transmissionRiskLevel, MIN_RISK_LEVEL, MAX_RISK_LEVEL));
    }
  }

  public static void validateRollingStartNumber(long rollingStartNumber) {
    if (rollingStartNumber < 1) {
      logger.debug("Rolling start number must be greater than 0.");
      throw new InvalidDiagnosisKeyException("Rolling start number must be greater than 0.");
    }

    long currentInstant = LocalDateTime.ofInstant(Instant.now(), UTC).toEpochSecond(UTC);
    if (rollingStartNumber > currentInstant) {
      logger.debug("Rolling start cannot be in the future.");
      throw new InvalidDiagnosisKeyException("Rolling start cannot be in the future.");
    }
  }

  public static void validateRollingPeriod(long rollingPeriod) {
    if (rollingPeriod < 1) {
      logger.debug("Rolling period must be positive number, but is {}.", rollingPeriod);
      throw new InvalidDiagnosisKeyException(
          String.format("Rolling period must be positive number, but is %s.", rollingPeriod));
    }
  }

  public static void validateKeyData(byte[] keyData) {
    if (keyData.length != 16) {
      logger.debug("Key data must be byte array of length 16, but is {}.", keyData.length);
      throw new InvalidDiagnosisKeyException(
          String.format("Key data must be byte array of length 16, but is %s.", keyData.length));
    }
  }
}
