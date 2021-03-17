package app.coronawarn.server.services.eventregistration.boundary.validation;


import app.coronawarn.server.common.protocols.internal.evreg.TraceLocation;
import app.coronawarn.server.services.eventregistration.config.EventRegistrationConfiguration;
import java.util.ArrayList;
import java.util.List;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

@Component
public class TraceLocationValidator implements
    ConstraintValidator<ValidTraceLocation, TraceLocation> {

  private Integer version;
  private final EventRegistrationConfiguration eventRegistrationConfiguration;


  public TraceLocationValidator(EventRegistrationConfiguration eventRegistrationConfiguration) {
    this.eventRegistrationConfiguration = eventRegistrationConfiguration;
    this.version = this.eventRegistrationConfiguration.getVersion();
  }

  @Override
  public boolean isValid(TraceLocation value, ConstraintValidatorContext context) {
    List<Boolean> validations = new ArrayList<>();
    validations.add(validateGuidIsEmpty(value, context));
    validations.add(validateVersionIsCorrect(value, context));
    validations.add(validateStringIsNotEmpty(value.getDescription(), "Description", context));
    validations.add(validateStringNotMoreThan(value.getDescription(), "Description", 100, context));
    validations.add(validateStringIsNotEmpty(value.getAddress(), "Address", context));
    validations.add(validateStringNotMoreThan(value.getAddress(), "Address", 100, context));

    validations.add(validateStartEndTimestamp(value, context));
    validations.add(validateDefaultCheckInLengthMustBePositive(value, context));
    return validations.stream().noneMatch(b -> b.equals(Boolean.FALSE));
  }

  private boolean validateDefaultCheckInLengthMustBePositive(TraceLocation value, ConstraintValidatorContext context) {
    if (!(value.getDefaultCheckInLengthInMinutes() > 0)) {
      addViolation(context, "Trace Location default Check-in length must be greater than zero.");
      return false;
    }
    return true;
  }

  private boolean validateStartEndTimestamp(TraceLocation value, ConstraintValidatorContext context) {
    if (value.getStartTimestamp() < 0 || value.getEndTimestamp() < 0) {
      addViolation(context, "Trace Location Start and End Timestamp must be not negative.");
      return false;
    }
    if (value.getStartTimestamp() > value.getEndTimestamp()) {
      addViolation(context, "Trace Location Start Timestamp must not be greater or equal than End Timestamp.");
      return false;
    }
    return true;
  }

  private boolean validateStringNotMoreThan(String value, String placeHolder, int amount,
      ConstraintValidatorContext context) {
    if (value.trim().length() > amount) {
      addViolation(context, String.format("Trace Location %s may not be empty.", placeHolder));
      return false;
    }
    return true;
  }

  private boolean validateStringIsNotEmpty(String value, String placeHolder, ConstraintValidatorContext context) {
    if (value.isEmpty()) {
      addViolation(context, String.format("Trace Location %s may not be empty.", placeHolder));
      return false;
    }
    return true;
  }

  private boolean validateVersionIsCorrect(TraceLocation value, ConstraintValidatorContext context) {
    if (value.getVersion() != this.version) {
      addViolation(context, "Trace Location Version contains an invalid value.");
      return false;
    }
    return true;
  }

  private boolean validateGuidIsEmpty(TraceLocation value, ConstraintValidatorContext context) {
    if (!value.getGuid().isEmpty()) {
      addViolation(context, "Trace Location Guid must be empty or unset");
      return false;
    }
    return true;
  }

  private void addViolation(ConstraintValidatorContext validatorContext, String message) {
    validatorContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
  }
}
