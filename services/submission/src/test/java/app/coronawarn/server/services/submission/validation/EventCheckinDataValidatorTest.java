package app.coronawarn.server.services.submission.validation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import java.util.List;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload;
import app.coronawarn.server.common.protocols.internal.evreg.CheckIn;

class EventCheckinDataValidatorTest {

  private static final int CORRECT_TRL = 1;
  private static final int CORRECT_CHECKOUT_TIME = 12;
  private static final int CORRECT_CHECKIN_TIME = 1;

  private ConstraintValidatorContext mockValidatorContext;
  private EventCheckinDataValidator validator;

  @BeforeEach
  public void setup() {
    mockValidatorContext = mock(ConstraintValidatorContext.class);
    ConstraintViolationBuilder mockConstraintViolationBuilder =
        mock(ConstraintViolationBuilder.class);
    when(mockConstraintViolationBuilder.addConstraintViolation()).thenReturn(null);
    when(mockValidatorContext.buildConstraintViolationWithTemplate(any()))
        .thenReturn(mockConstraintViolationBuilder);
    validator = new EventCheckinDataValidator();
  }

  @ParameterizedTest
  @ValueSource(ints = {0, 9, 12})
  void should_return_false_if_checkin_data_has_wrong_trl_values(int wrongTrl) {
    SubmissionPayload newPayload = SubmissionPayload.newBuilder()
        .addAllCheckIns(List.of(CheckIn.newBuilder().setCheckinTime(CORRECT_CHECKIN_TIME)
            .setCheckoutTime(CORRECT_CHECKOUT_TIME).setTrl(wrongTrl).build()))
        .build();

    boolean result = validator.verify(newPayload, mockValidatorContext);
    assertFalse(result);
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 3, 5, 8})
  void should_return_true_if_checkin_data_has_correct_trl_values(int correctTrl) {
    SubmissionPayload newPayload = SubmissionPayload.newBuilder()
        .addAllCheckIns(List.of(CheckIn.newBuilder().setCheckinTime(CORRECT_CHECKIN_TIME)
            .setCheckoutTime(CORRECT_CHECKOUT_TIME).setTrl(correctTrl).build()))
        .build();

    boolean result = validator.verify(newPayload, mockValidatorContext);
    assertTrue(result);
  }

  @ParameterizedTest
  @ValueSource(ints = {-1, 0})
  void should_return_false_if_checkin_data_has_wrong_checkin_time_values(int startTime) {
    SubmissionPayload newPayload = SubmissionPayload.newBuilder()
        .addAllCheckIns(List.of(
            CheckIn.newBuilder().setCheckinTime(startTime).setCheckoutTime(CORRECT_CHECKOUT_TIME)
                .setTrl(CORRECT_TRL).build()))
        .build();

    boolean result = validator.verify(newPayload, mockValidatorContext);
    assertFalse(result);
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 15, 30, 100})
  void should_return_true_if_checkin_data_has_correct_checkin_time_values(int startTime) {
    SubmissionPayload newPayload = SubmissionPayload.newBuilder()
        .addAllCheckIns(List.of(
            CheckIn.newBuilder().setCheckinTime(startTime).setCheckoutTime(startTime + 1)
                .setTrl(CORRECT_TRL).build()))
        .build();

    boolean result = validator.verify(newPayload, mockValidatorContext);
    assertTrue(result);
  }

  @Test
  void should_return_false_if_checkout_time_is_equal_or_before_checkin_time() {
    SubmissionPayload newPayload = SubmissionPayload.newBuilder()
        .addAllCheckIns(List.of(CheckIn.newBuilder().setCheckinTime(CORRECT_CHECKOUT_TIME)
            .setCheckoutTime(CORRECT_CHECKOUT_TIME).setTrl(CORRECT_TRL).build()))
        .build();

    boolean result = validator.verify(newPayload, mockValidatorContext);
    assertFalse(result);

    newPayload = SubmissionPayload.newBuilder()
        .addAllCheckIns(List.of(CheckIn.newBuilder().setCheckinTime(CORRECT_CHECKOUT_TIME)
            .setCheckoutTime(CORRECT_CHECKOUT_TIME - 1).setTrl(CORRECT_TRL).build()))
        .build();

    result = validator.verify(newPayload, mockValidatorContext);
    assertFalse(result);
  }

  @Test
  void should_return_true_if_checkout_time_is_after_checkin_time() {
    SubmissionPayload newPayload = SubmissionPayload.newBuilder()
        .addAllCheckIns(List.of(CheckIn.newBuilder().setCheckinTime(CORRECT_CHECKOUT_TIME)
            .setCheckoutTime(CORRECT_CHECKOUT_TIME + 1).setTrl(CORRECT_TRL).build()))
        .build();

    boolean result = validator.verify(newPayload, mockValidatorContext);
    assertTrue(result);
  }
}
