package app.coronawarn.server.services.submission.validation;


import static app.coronawarn.server.services.submission.validation.ValidSubmissionOnBehalfPayload.ValidSubmissionOnBehalfPayloadValidator;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import app.coronawarn.server.common.protocols.internal.SubmissionPayload;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload.SubmissionType;
import app.coronawarn.server.common.protocols.internal.pt.CheckIn;
import app.coronawarn.server.common.protocols.internal.pt.CheckInProtectedReport;
import app.coronawarn.server.services.submission.checkins.EventCheckInProtectedReportsValidator;
import app.coronawarn.server.services.submission.checkins.EventCheckinDataValidator;
import app.coronawarn.server.services.submission.integration.DataHelpers;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ValidSubmissionOnBehalfTest {


  @InjectMocks
  ValidSubmissionOnBehalfPayloadValidator underTest;

  @Mock
  EventCheckinDataValidator eventCheckinDataValidator;

  @Mock
  EventCheckInProtectedReportsValidator protectedReportsValidator;

  @Mock
  ConstraintViolationBuilder constraintViolationBuilder;

  @Mock
  ConstraintValidatorContext constraintValidatorContext;


  @Test
  void shouldReturnTrueIfSubmissionOnBehalfPayloadIsValid() {
    byte[] locationIdHash = new byte[32];
    new Random().nextBytes(locationIdHash);
    List<CheckInProtectedReport> protectedReports = List
        .of(DataHelpers.buildDefaultEncryptedCheckIn(locationIdHash),
            DataHelpers.buildDefaultEncryptedCheckIn(locationIdHash));
    List<CheckIn> checkIns = List
        .of(DataHelpers.buildDefaultCheckIn(), DataHelpers.buildDefaultCheckIn());
    SubmissionPayload validSubmissionPayload = SubmissionPayload.newBuilder()
        .addAllKeys(Collections.emptyList())
        .addAllVisitedCountries(Collections.emptyList())
        .setConsentToFederation(false)
        .setSubmissionType(SubmissionType.SUBMISSION_TYPE_HOST_WARNING)
        .addAllCheckInProtectedReports(protectedReports)
        .addAllCheckIns(checkIns)
        .build();

    when(eventCheckinDataValidator.verifyHaveSameLocationId(validSubmissionPayload, constraintValidatorContext))
        .thenReturn(true);
    when(protectedReportsValidator.verifyHaveSameLocationIdHash(validSubmissionPayload, constraintValidatorContext))
        .thenReturn(true);

    when(eventCheckinDataValidator.verify(validSubmissionPayload, constraintValidatorContext))
        .thenReturn(true);
    when(protectedReportsValidator.verify(validSubmissionPayload, constraintValidatorContext))
        .thenReturn(true);

    final boolean valid = underTest.isValid(validSubmissionPayload, constraintValidatorContext);
    assertThat(valid).isTrue();
  }

  @Test
  void shouldReturnFalseIfProtectedCheckInsHaveDifferentLocationId() {
    List<CheckInProtectedReport> protectedReports = List
        .of(DataHelpers.buildDefaultEncryptedCheckIn(), DataHelpers.buildDefaultEncryptedCheckIn());
    List<CheckIn> checkIns = List
        .of(DataHelpers.buildDefaultCheckIn(), DataHelpers.buildDefaultCheckIn());
    SubmissionPayload validSubmissionPayload = SubmissionPayload.newBuilder()
        .addAllKeys(Collections.emptyList())
        .addAllVisitedCountries(Collections.emptyList())
        .setConsentToFederation(false)
        .setSubmissionType(SubmissionType.SUBMISSION_TYPE_HOST_WARNING)
        .addAllCheckInProtectedReports(protectedReports)
        .addAllCheckIns(checkIns)
        .build();
    when(eventCheckinDataValidator.verifyHaveSameLocationId(validSubmissionPayload, constraintValidatorContext))
        .thenReturn(true);
    when(protectedReportsValidator.verifyHaveSameLocationIdHash(validSubmissionPayload, constraintValidatorContext))
        .thenReturn(false);

    final boolean valid = underTest.isValid(validSubmissionPayload, constraintValidatorContext);
    assertThat(valid).isFalse();
  }

  @ParameterizedTest
  @EnumSource(mode = Mode.MATCH_ALL, names = "\\S*_TEST$")
  void shouldReturnFalseIfSubmissionTypeIsNotHostWarning(SubmissionType submissionType) {
    List<CheckInProtectedReport> protectedReports = List
        .of(DataHelpers.buildDefaultEncryptedCheckIn(), DataHelpers.buildDefaultEncryptedCheckIn());
    List<CheckIn> checkIns = List
        .of(DataHelpers.buildDefaultCheckIn(), DataHelpers.buildDefaultCheckIn());
    SubmissionPayload validSubmissionPayload = SubmissionPayload.newBuilder()
        .addAllKeys(Collections.emptyList())
        .addAllVisitedCountries(Collections.emptyList())
        .setConsentToFederation(false)
        .setSubmissionType(submissionType)
        .addAllCheckInProtectedReports(protectedReports)
        .addAllCheckIns(checkIns)
        .build();

    when(constraintValidatorContext.buildConstraintViolationWithTemplate(anyString()))
        .thenReturn(constraintViolationBuilder);

    final boolean valid = underTest.isValid(validSubmissionPayload, constraintValidatorContext);
    assertThat(valid).isFalse();
  }

  @Test
  void shouldReturnFalseIfCheckInsHaveNotSameLocationId() {
    byte[] locationIdHash = new byte[32];
    new Random().nextBytes(locationIdHash);

    byte[] locationId1 = new byte[]{1, 2, 3, 4};
    byte[] locationId2 = new byte[]{5, 6, 7, 8};
    List<CheckInProtectedReport> protectedReports = List
        .of(DataHelpers.buildDefaultEncryptedCheckIn(locationIdHash),
            DataHelpers.buildDefaultEncryptedCheckIn(locationIdHash));
    List<CheckIn> checkIns = List
        .of(DataHelpers.buildDefaultCheckIn(locationId1), DataHelpers.buildDefaultCheckIn(locationId2));
    SubmissionPayload validSubmissionPayload = SubmissionPayload.newBuilder()
        .addAllKeys(Collections.emptyList())
        .addAllVisitedCountries(Collections.emptyList())
        .setConsentToFederation(false)
        .setSubmissionType(SubmissionType.SUBMISSION_TYPE_HOST_WARNING)
        .addAllCheckInProtectedReports(protectedReports)
        .addAllCheckIns(checkIns)
        .build();
    when(eventCheckinDataValidator.verifyHaveSameLocationId(validSubmissionPayload, constraintValidatorContext))
        .thenReturn(false);

    final boolean valid = underTest.isValid(validSubmissionPayload, constraintValidatorContext);
    assertThat(valid).isFalse();
  }

  @Test
  void shouldReturnFalseIfProtectedCheckInsAreEmpty() {
    byte[] locationId = new byte[32];
    new Random().nextBytes(locationId);

    List<CheckInProtectedReport> protectedReports = Collections.emptyList();
    List<CheckIn> checkIns = List
        .of(DataHelpers.buildDefaultCheckIn(locationId), DataHelpers.buildDefaultCheckIn(locationId));
    SubmissionPayload validSubmissionPayload = SubmissionPayload.newBuilder()
        .addAllKeys(Collections.emptyList())
        .addAllVisitedCountries(Collections.emptyList())
        .setConsentToFederation(false)
        .setSubmissionType(SubmissionType.SUBMISSION_TYPE_HOST_WARNING)
        .addAllCheckInProtectedReports(protectedReports)
        .addAllCheckIns(checkIns)
        .build();

    when(constraintValidatorContext.buildConstraintViolationWithTemplate(anyString()))
        .thenReturn(constraintViolationBuilder);
    when(eventCheckinDataValidator.verifyHaveSameLocationId(validSubmissionPayload, constraintValidatorContext))
        .thenReturn(true);

    final boolean valid = underTest.isValid(validSubmissionPayload, constraintValidatorContext);
    assertThat(valid).isFalse();
  }

  @Test
  void shouldReturnFalseIfKeysAreNotEmpty() {
    byte[] locationId = new byte[32];
    new Random().nextBytes(locationId);

    List<CheckInProtectedReport> protectedReports = List
        .of(DataHelpers.buildDefaultEncryptedCheckIn(locationId),
            DataHelpers.buildDefaultEncryptedCheckIn(locationId));

    List<CheckIn> checkIns = List
        .of(DataHelpers.buildDefaultCheckIn(locationId), DataHelpers.buildDefaultCheckIn(locationId));
    SubmissionPayload validSubmissionPayload = SubmissionPayload.newBuilder()
        .addAllKeys(DataHelpers.createValidTemporaryExposureKeys())
        .addAllVisitedCountries(Collections.emptyList())
        .setConsentToFederation(false)
        .setSubmissionType(SubmissionType.SUBMISSION_TYPE_HOST_WARNING)
        .addAllCheckInProtectedReports(protectedReports)
        .addAllCheckIns(checkIns)
        .build();

    when(constraintValidatorContext.buildConstraintViolationWithTemplate(anyString()))
        .thenReturn(constraintViolationBuilder);

    final boolean valid = underTest.isValid(validSubmissionPayload, constraintValidatorContext);
    assertThat(valid).isFalse();
  }

  @Test
  void shouldReturnFalseIfVisitedCountriesAreNotEmpty() {
    byte[] locationId = new byte[32];
    new Random().nextBytes(locationId);

    List<CheckInProtectedReport> protectedReports = List
        .of(DataHelpers.buildDefaultEncryptedCheckIn(locationId),
            DataHelpers.buildDefaultEncryptedCheckIn(locationId));

    List<CheckIn> checkIns = List
        .of(DataHelpers.buildDefaultCheckIn(locationId), DataHelpers.buildDefaultCheckIn(locationId));
    SubmissionPayload validSubmissionPayload = SubmissionPayload.newBuilder()
        .addAllKeys(Collections.emptyList())
        .addAllVisitedCountries(List.of("DE"))
        .setConsentToFederation(false)
        .setSubmissionType(SubmissionType.SUBMISSION_TYPE_HOST_WARNING)
        .addAllCheckInProtectedReports(protectedReports)
        .addAllCheckIns(checkIns)
        .build();

    when(constraintValidatorContext.buildConstraintViolationWithTemplate(anyString()))
        .thenReturn(constraintViolationBuilder);

    final boolean valid = underTest.isValid(validSubmissionPayload, constraintValidatorContext);
    assertThat(valid).isFalse();
  }

  @Test
  void shouldReturnFalseIfConsentToFederationIsTrue() {
    byte[] locationId = new byte[32];
    new Random().nextBytes(locationId);

    List<CheckInProtectedReport> protectedReports = List
        .of(DataHelpers.buildDefaultEncryptedCheckIn(locationId),
            DataHelpers.buildDefaultEncryptedCheckIn(locationId));

    List<CheckIn> checkIns = List
        .of(DataHelpers.buildDefaultCheckIn(locationId), DataHelpers.buildDefaultCheckIn(locationId));
    SubmissionPayload validSubmissionPayload = SubmissionPayload.newBuilder()
        .addAllKeys(Collections.emptyList())
        .addAllVisitedCountries(Collections.emptyList())
        .setConsentToFederation(true)
        .setSubmissionType(SubmissionType.SUBMISSION_TYPE_HOST_WARNING)
        .addAllCheckInProtectedReports(protectedReports)
        .addAllCheckIns(checkIns)
        .build();

    when(constraintValidatorContext.buildConstraintViolationWithTemplate(anyString()))
        .thenReturn(constraintViolationBuilder);

    final boolean valid = underTest.isValid(validSubmissionPayload, constraintValidatorContext);
    assertThat(valid).isFalse();
  }

  @Test
  void shouldReturnFalseIfProtectedCheckInsAreInvalid() {
    byte[] locationId = new byte[32];
    new Random().nextBytes(locationId);

    List<CheckInProtectedReport> protectedReports = List
        .of(DataHelpers.buildDefaultEncryptedCheckIn(locationId),
            DataHelpers.buildDefaultEncryptedCheckIn(locationId));

    List<CheckIn> checkIns = List
        .of(DataHelpers.buildDefaultCheckIn(locationId), DataHelpers.buildDefaultCheckIn(locationId));
    SubmissionPayload validSubmissionPayload = SubmissionPayload.newBuilder()
        .addAllKeys(Collections.emptyList())
        .addAllVisitedCountries(Collections.emptyList())
        .setConsentToFederation(false)
        .setSubmissionType(SubmissionType.SUBMISSION_TYPE_HOST_WARNING)
        .addAllCheckInProtectedReports(protectedReports)
        .addAllCheckIns(checkIns)
        .build();

    when(eventCheckinDataValidator.verifyHaveSameLocationId(validSubmissionPayload, constraintValidatorContext))
        .thenReturn(true);
    when(protectedReportsValidator.verifyHaveSameLocationIdHash(validSubmissionPayload, constraintValidatorContext))
        .thenReturn(true);

    when(eventCheckinDataValidator.verify(validSubmissionPayload, constraintValidatorContext))
        .thenReturn(true);
    when(protectedReportsValidator.verify(validSubmissionPayload, constraintValidatorContext))
        .thenReturn(false);

    final boolean valid = underTest.isValid(validSubmissionPayload, constraintValidatorContext);
    assertThat(valid).isFalse();
  }

  @Test
  void shouldReturnFalseIfCheckInsAreInvalid() {
    byte[] locationId = new byte[32];
    new Random().nextBytes(locationId);

    List<CheckInProtectedReport> protectedReports = List
        .of(DataHelpers.buildDefaultEncryptedCheckIn(locationId),
            DataHelpers.buildDefaultEncryptedCheckIn(locationId));

    List<CheckIn> checkIns = List
        .of(DataHelpers.buildDefaultCheckIn(locationId), DataHelpers.buildDefaultCheckIn(locationId));
    SubmissionPayload validSubmissionPayload = SubmissionPayload.newBuilder()
        .addAllKeys(Collections.emptyList())
        .addAllVisitedCountries(Collections.emptyList())
        .setConsentToFederation(false)
        .setSubmissionType(SubmissionType.SUBMISSION_TYPE_HOST_WARNING)
        .addAllCheckInProtectedReports(protectedReports)
        .addAllCheckIns(checkIns)
        .build();

    when(eventCheckinDataValidator.verifyHaveSameLocationId(validSubmissionPayload, constraintValidatorContext))
        .thenReturn(true);
    when(protectedReportsValidator.verifyHaveSameLocationIdHash(validSubmissionPayload, constraintValidatorContext))
        .thenReturn(true);

    when(eventCheckinDataValidator.verify(validSubmissionPayload, constraintValidatorContext))
        .thenReturn(false);

    final boolean valid = underTest.isValid(validSubmissionPayload, constraintValidatorContext);
    assertThat(valid).isFalse();
  }
}
