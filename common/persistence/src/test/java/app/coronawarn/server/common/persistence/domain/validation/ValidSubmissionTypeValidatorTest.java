package app.coronawarn.server.common.persistence.domain.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;

import app.coronawarn.server.common.protocols.internal.SubmissionPayload.SubmissionType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class ValidSubmissionTypeValidatorTest {

  @ParameterizedTest
  @EnumSource(value = SubmissionType.class, names = { "SUBMISSION_TYPE_HOST_WARNING" })
  final void isInvalidTest(final SubmissionType type) {
    final ValidSubmissionTypeValidator validator = new ValidSubmissionTypeValidator();
    // host warnings, aren't stored in DiagnosisKey table!
    assertFalse(validator.isValid(type, null), type + " is valid!?!");
  }

  @ParameterizedTest
  @EnumSource(value = SubmissionType.class, mode = EXCLUDE, names = { "SUBMISSION_TYPE_HOST_WARNING" })
  final void isValidTest(final SubmissionType type) {
    final ValidSubmissionTypeValidator validator = new ValidSubmissionTypeValidator();
    assertTrue(validator.isValid(type, null), type + " failed!?!");
  }
}
