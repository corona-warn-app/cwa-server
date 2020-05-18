package app.coronawarn.server.common.persistence.domain;

import static java.time.ZoneOffset.UTC;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import app.coronawarn.server.common.persistence.exception.InvalidDiagnosisKeyException;
import java.nio.charset.Charset;
import java.time.Instant;
import java.time.LocalDate;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class DiagnosisKeyValidatorTest {

  @Test
  public void transmissionRiskLevelMustBeInRange() {
    checkMessage(assertThrows(
        InvalidDiagnosisKeyException.class, () ->
            DiagnosisKeyValidator.validateTransmissionRiskLevel(9)),
        "Risk level 9 is not allowed. Must be between 0 and 8.");

    checkMessage(assertThrows(
        InvalidDiagnosisKeyException.class, () ->
            DiagnosisKeyValidator.validateTransmissionRiskLevel(-1)),
        "Risk level -1 is not allowed. Must be between 0 and 8.");
  }

  @Test
  public void transmissionRiskLevelDoesNotThrowForValid() {
    Assertions.assertThatCode(
        () -> DiagnosisKeyValidator.validateTransmissionRiskLevel(0))
        .doesNotThrowAnyException();

    Assertions.assertThatCode(
        () -> DiagnosisKeyValidator.validateTransmissionRiskLevel(8))
        .doesNotThrowAnyException();
  }

  @Test
  public void rollingStartNumberDoesNotThrowForValid() {
    Assertions.assertThatCode(
        () -> DiagnosisKeyValidator.validateRollingStartNumber(4200L))
        .doesNotThrowAnyException();

    // Timestamp: 05/16/2020 @ 00:00
    Assertions.assertThatCode(
        () -> DiagnosisKeyValidator.validateRollingStartNumber(1589587200L))
        .doesNotThrowAnyException();
  }

  @Test
  public void rollingStartNumberCannotBeInFuture() {
    checkMessage(assertThrows(
        InvalidDiagnosisKeyException.class, () ->
            DiagnosisKeyValidator.validateRollingStartNumber(1904169600L)),
        "Rolling start cannot be in the future.");

    long tomorrow = LocalDate
        .ofInstant(Instant.now(), UTC)
        .plusDays(1).atStartOfDay()
        .toEpochSecond(UTC);

    checkMessage(assertThrows(
        InvalidDiagnosisKeyException.class, () ->
            DiagnosisKeyValidator.validateRollingStartNumber(tomorrow)),
        "Rolling start cannot be in the future.");

  }

  @Test
  public void rollingPeriodMustBeLargerThanZero() {
    checkMessage(assertThrows(
        InvalidDiagnosisKeyException.class, () ->
            DiagnosisKeyValidator.validateRollingPeriod(0)),
        "Rolling period must be positive number, but is 0.");

    checkMessage(assertThrows(
        InvalidDiagnosisKeyException.class, () ->
            DiagnosisKeyValidator.validateRollingPeriod(-3L)),
        "Rolling period must be positive number, but is -3.");
  }

  @Test
  public void rollingPeriodDoesNotThrowForValid() {
    Assertions.assertThatCode(
        () -> DiagnosisKeyValidator.validateRollingPeriod(144L))
        .doesNotThrowAnyException();
  }

  @Test
  public void keyDataMustHaveValidLength() {
    checkMessage(assertThrows(
        InvalidDiagnosisKeyException.class, () ->
            DiagnosisKeyValidator.validateKeyData(
                "17--bytelongarray".getBytes(Charset.defaultCharset()))),
        "Key data must be byte array of length 16, but is 17.");

    checkMessage(assertThrows(
        InvalidDiagnosisKeyException.class, () ->
            DiagnosisKeyValidator.validateKeyData(
                "".getBytes(Charset.defaultCharset()))),
        "Key data must be byte array of length 16, but is 0.");

    checkMessage(assertThrows(
        InvalidDiagnosisKeyException.class, () ->
            DiagnosisKeyValidator.validateKeyData(
                "1".getBytes(Charset.defaultCharset()))),
        "Key data must be byte array of length 16, but is 1.");
  }

  @Test
  public void keyDataDoesNotThrowOnValid() {
    Assertions.assertThatCode(
        () -> DiagnosisKeyValidator.validateKeyData(
            "16-bytelongarray".getBytes(Charset.defaultCharset())))
        .doesNotThrowAnyException();
  }

  private void checkMessage(InvalidDiagnosisKeyException ex, String message) {
    assertEquals(ex.getMessage(), message);
  }

}
