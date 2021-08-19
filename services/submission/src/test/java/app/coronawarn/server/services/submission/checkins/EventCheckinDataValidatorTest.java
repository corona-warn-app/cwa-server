package app.coronawarn.server.services.submission.checkins;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import app.coronawarn.server.common.protocols.internal.SubmissionPayload;
import app.coronawarn.server.common.protocols.internal.pt.CheckIn;
import app.coronawarn.server.services.submission.integration.DataHelpers;
import com.google.protobuf.ByteString;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Random;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventCheckinDataValidatorTest {

  private static final int CORRECT_TRL = 1;
  private static final int CORRECT_CHECKOUT_TIME = 12;
  private static final int CORRECT_CHECKIN_TIME = 1;
  public static final ByteString CORRECT_LOCATION_ID = generateCorrectLocationId();

  static final Logger logger = LoggerFactory.getLogger(EventCheckinDataValidatorTest.class);

  private static ByteString generateCorrectLocationId() {
    try {
      return ByteString.copyFrom(
          MessageDigest.getInstance("SHA-256").digest("my valid sha-256 hash".getBytes(StandardCharsets.UTF_8)));
    } catch (final NoSuchAlgorithmException e) {
      logger.debug(e.getLocalizedMessage(), e);
    }
    return null;
  }

  private ConstraintValidatorContext mockValidatorContext;
  private EventCheckinDataValidator validator;

  @BeforeEach
  public void setup() {
    mockValidatorContext = mock(ConstraintValidatorContext.class);
    final ConstraintViolationBuilder mockConstraintViolationBuilder = mock(ConstraintViolationBuilder.class);
    when(mockConstraintViolationBuilder.addConstraintViolation()).thenReturn(null);
    when(mockValidatorContext.buildConstraintViolationWithTemplate(any())).thenReturn(mockConstraintViolationBuilder);
    validator = new EventCheckinDataValidator() {
      @Override
      void addViolation(final ConstraintValidatorContext validatorContext, final String message) {
        logger.debug(message);
        super.addViolation(validatorContext, message);
      }
    };
  }

  @ParameterizedTest
  @ValueSource(ints = {-1, 0})
  void should_return_false_if_checkin_data_has_wrong_checkin_time_values(final int startTime) {
    final SubmissionPayload newPayload = SubmissionPayload.newBuilder()
        .addAllCheckIns(List.of(CheckIn.newBuilder().setStartIntervalNumber(startTime)
            .setEndIntervalNumber(CORRECT_CHECKOUT_TIME).setTransmissionRiskLevel(CORRECT_TRL).build()))
        .build();

    final boolean result = validator.verify(newPayload, mockValidatorContext);
    assertFalse(result);
  }

  @ParameterizedTest
  @ValueSource(ints = {0, 9, 12})
  void should_return_false_if_checkin_data_has_wrong_trl_values(final int wrongTrl) {
    final SubmissionPayload newPayload = SubmissionPayload.newBuilder()
        .addAllCheckIns(List.of(CheckIn.newBuilder().setStartIntervalNumber(CORRECT_CHECKIN_TIME)
            .setEndIntervalNumber(CORRECT_CHECKOUT_TIME).setTransmissionRiskLevel(wrongTrl).build()))
        .build();

    final boolean result = validator.verify(newPayload, mockValidatorContext);
    assertFalse(result);
  }

  @Test
  void should_return_false_if_checkout_time_is_equal_or_before_checkin_time() {
    SubmissionPayload newPayload = SubmissionPayload.newBuilder()
        .addAllCheckIns(List.of(CheckIn.newBuilder().setStartIntervalNumber(CORRECT_CHECKOUT_TIME)
            .setEndIntervalNumber(CORRECT_CHECKOUT_TIME).setTransmissionRiskLevel(CORRECT_TRL).build()))
        .build();

    boolean result = validator.verify(newPayload, mockValidatorContext);
    assertFalse(result);

    newPayload = SubmissionPayload.newBuilder()
        .addAllCheckIns(List.of(CheckIn.newBuilder().setStartIntervalNumber(CORRECT_CHECKOUT_TIME)
            .setEndIntervalNumber(CORRECT_CHECKOUT_TIME - 1).setTransmissionRiskLevel(CORRECT_TRL).build()))
        .build();

    result = validator.verify(newPayload, mockValidatorContext);
    assertFalse(result);
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 15, 30, 100})
  void should_return_true_if_checkin_data_has_correct_checkin_time_values(final int startTime) {
    final SubmissionPayload newPayload = SubmissionPayload.newBuilder()
        .addAllCheckIns(
            List.of(CheckIn.newBuilder().setStartIntervalNumber(startTime).setEndIntervalNumber(startTime + 1)
                .setTransmissionRiskLevel(CORRECT_TRL).setLocationId(CORRECT_LOCATION_ID).build()))
        .build();

    final boolean result = validator.verify(newPayload, mockValidatorContext);
    assertTrue(result);
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 3, 5, 8})
  void should_return_true_if_checkin_data_has_correct_trl_values(final int correctTrl) {
    final SubmissionPayload newPayload = SubmissionPayload.newBuilder()
        .addAllCheckIns(List.of(CheckIn.newBuilder().setStartIntervalNumber(CORRECT_CHECKIN_TIME)
            .setEndIntervalNumber(CORRECT_CHECKOUT_TIME).setTransmissionRiskLevel(correctTrl)
            .setLocationId(CORRECT_LOCATION_ID).build()))
        .build();

    final boolean result = validator.verify(newPayload, mockValidatorContext);
    assertTrue(result);
  }

  @Test
  void should_return_true_if_checkout_time_is_after_checkin_time() {
    final SubmissionPayload newPayload = SubmissionPayload.newBuilder()
        .addAllCheckIns(List.of(CheckIn.newBuilder().setStartIntervalNumber(CORRECT_CHECKOUT_TIME)
            .setEndIntervalNumber(CORRECT_CHECKOUT_TIME + 1).setTransmissionRiskLevel(CORRECT_TRL)
            .setLocationId(CORRECT_LOCATION_ID).build()))
        .build();

    final boolean result = validator.verify(newPayload, mockValidatorContext);
    assertTrue(result);
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "aac31", "09c488aac3104d556531ab79f10f98eb0a9bdc2",
      "09c488aac3104d556531ab79f10f98eb0a9bdc209c488aac3104d556531ab79f10f98eb0a9bdc2"})
  void verifyLocationIdFalseLengthTest1(final String locationId) {
    final CheckIn checkIn = CheckIn.newBuilder()
        .setLocationId(ByteString.copyFrom(locationId.getBytes(StandardCharsets.UTF_8))).build();
    final boolean result = validator.verifyLocationIdLength(checkIn, mockValidatorContext);
    assertFalse(result);
  }

  @Test
  void verifyLocationIdFalseLengthTest2() {
    final CheckIn checkIn = CheckIn.newBuilder().setLocationId(ByteString.EMPTY).build();
    assertFalse(validator.verifyLocationIdLength(checkIn, mockValidatorContext));
  }

  @Test
  void verifyLocationIdTrueLengthTest() throws Exception {
    final CheckIn checkIn = CheckIn.newBuilder().setLocationId(CORRECT_LOCATION_ID).build();
    assertTrue(validator.verifyLocationIdLength(checkIn, mockValidatorContext));
  }

  @Test
  void shouldReturnTrueIfCheckInsHaveSameLocationId() {
    byte[] locationId = new byte[32];
    new Random().nextBytes(locationId);

    List<CheckIn> checkIns = List
        .of(DataHelpers.buildDefaultCheckIn(locationId), DataHelpers.buildDefaultCheckIn(locationId));

    SubmissionPayload submissionPayload = SubmissionPayload.newBuilder()
        .addAllCheckIns(checkIns)
        .build();
    final boolean isValid = validator.verifyHaveSameLocationId(submissionPayload, mockValidatorContext);
    assertThat(isValid).isTrue();
  }

  @Test
  void shouldReturnFalseIfCheckInsHaveDifferentLocationId() {
    byte[] locationId1 = new byte[]{1,2,3};
    byte[] locationId2 = new byte[]{10,20,30};
    List<CheckIn> checkIns = List
        .of(DataHelpers.buildDefaultCheckIn(locationId1), DataHelpers.buildDefaultCheckIn(locationId2));

    SubmissionPayload submissionPayload = SubmissionPayload.newBuilder()
        .addAllCheckIns(checkIns)
        .build();
    final boolean isValid = validator.verifyHaveSameLocationId(submissionPayload, mockValidatorContext);
    assertThat(isValid).isFalse();
  }
}
