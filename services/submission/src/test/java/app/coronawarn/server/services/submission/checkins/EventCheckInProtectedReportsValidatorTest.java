package app.coronawarn.server.services.submission.checkins;

import static app.coronawarn.server.common.shared.util.HashUtils.generateSecureRandomByteArrayData;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import app.coronawarn.server.common.protocols.internal.pt.CheckInProtectedReport;
import com.google.protobuf.ByteString;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
public class EventCheckInProtectedReportsValidatorTest {

  @Mock
  private ConstraintValidatorContext mockValidatorContext;
  @Mock
  private ConstraintViolationBuilder constraintViolationBuilder;
  @InjectMocks
  private EventCheckInProtectedReportsValidator underTest;

  @Test
  void verifyEncryptedCheckInRecordLengthIsTrue() {
    CheckInProtectedReport checkInProtectedReport = CheckInProtectedReport.newBuilder().setEncryptedCheckInRecord(
        ByteString.copyFrom(generateSecureRandomByteArrayData(16))).build();

    boolean result = underTest.verifyEncryptedCheckInRecordLength(checkInProtectedReport,mockValidatorContext);
    assertThat(result).isTrue();
  }

  @ParameterizedTest
  @MethodSource("generateWrongLengthByteStrings")
  void verifyEncryptedCheckInRecordLengthIsFalse(ByteString e) {
    CheckInProtectedReport checkInProtectedReport = CheckInProtectedReport.newBuilder().setEncryptedCheckInRecord(e).build();

    when(constraintViolationBuilder.addConstraintViolation()).thenReturn(null);
    when(mockValidatorContext.buildConstraintViolationWithTemplate(any())).thenReturn(constraintViolationBuilder);

    boolean result = underTest.verifyEncryptedCheckInRecordLength(checkInProtectedReport,mockValidatorContext);
    assertThat(result).isFalse();
  }

  private static Stream<Arguments> generateWrongLengthByteStrings() {
    return Stream.of(
        Arguments.of(ByteString.copyFrom(generateSecureRandomByteArrayData(100))),
        Arguments.of(ByteString.EMPTY));
  }

  @Test
  void verifyIvLengthIsTrue() {
    CheckInProtectedReport checkInProtectedReport = CheckInProtectedReport.newBuilder().setIv(
        ByteString.copyFrom(generateSecureRandomByteArrayData(32))).build();

    boolean result = underTest.verifyIvLength(checkInProtectedReport,mockValidatorContext);
    assertThat(result).isTrue();
  }

  @ParameterizedTest
  @MethodSource("generateWrongLengthByteStrings")
  void verifyIvLengthIsFalse(ByteString e) {
    CheckInProtectedReport checkInProtectedReport = CheckInProtectedReport.newBuilder().setIv(e).build();

    when(constraintViolationBuilder.addConstraintViolation()).thenReturn(null);
    when(mockValidatorContext.buildConstraintViolationWithTemplate(any())).thenReturn(constraintViolationBuilder);

    boolean result = underTest.verifyIvLength(checkInProtectedReport,mockValidatorContext);
    assertThat(result).isFalse();
  }

  @Test
  void verifyLocationIdHashLengthIsTrue() {
    CheckInProtectedReport checkInProtectedReport = CheckInProtectedReport.newBuilder().setLocationIdHash(
        ByteString.copyFrom(generateSecureRandomByteArrayData(32))).build();

    boolean result = underTest.verifyLocationIdHashLength(checkInProtectedReport,mockValidatorContext);
    assertThat(result).isTrue();
  }

  @ParameterizedTest
  @MethodSource("generateWrongLengthByteStrings")
  void verifyLocationIdHashLengthIsFalse(ByteString e) {
    CheckInProtectedReport checkInProtectedReport = CheckInProtectedReport.newBuilder().setLocationIdHash(e).build();

    when(constraintViolationBuilder.addConstraintViolation()).thenReturn(null);
    when(mockValidatorContext.buildConstraintViolationWithTemplate(any())).thenReturn(constraintViolationBuilder);

    boolean result = underTest.verifyLocationIdHashLength(checkInProtectedReport,mockValidatorContext);
    assertThat(result).isFalse();
  }
}
