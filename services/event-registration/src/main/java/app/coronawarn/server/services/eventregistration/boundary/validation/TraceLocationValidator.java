package app.coronawarn.server.services.eventregistration.boundary.validation;


import app.coronawarn.server.common.protocols.internal.pt.TraceLocation;
import app.coronawarn.server.services.eventregistration.config.EventRegistrationConfiguration;
import java.util.ArrayList;
import java.util.List;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TraceLocationValidator implements
    ConstraintValidator<ValidTraceLocation, TraceLocation> {

  private static final Logger logger = LoggerFactory.getLogger(TraceLocationValidator.class);

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
      final String msg = "Trace Location default Check-in length must be greater than zero.";
      logger.info(msg);
      addViolation(context, msg);
      return false;
    }
    return true;
  }

  private boolean validateStartEndTimestamp(TraceLocation value, ConstraintValidatorContext context) {
    if (value.getStartTimestamp() < 0 || value.getEndTimestamp() < 0) {
      final String msg = "Trace Location Start and End Timestamp must be not negative.";
      logger.info(msg);
      addViolation(context, msg);
      return false;
    }
    if (value.getStartTimestamp() > value.getEndTimestamp()) {
      final String msg = "Trace Location Start Timestamp must not be greater or equal than End Timestamp.";
      logger.info(msg);
      addViolation(context, msg);
      return false;
    }
    return true;
  }

  private boolean validateStringNotMoreThan(String value, String placeHolder, int amount,
      ConstraintValidatorContext context) {
    if (value.trim().length() > amount) {
      final String msg = String.format("Trace Location %s may not be longer than %d characters.", placeHolder, amount);
      logger.info(msg);
      addViolation(context, msg);
      return false;
    }
    return true;
  }

  private boolean validateStringIsNotEmpty(String value, String placeHolder, ConstraintValidatorContext context) {
    if (value.isEmpty()) {
      final String msg = String.format("Trace Location %s may not be empty.", placeHolder);
      logger.info(msg);
      addViolation(context, msg);
      return false;
    }
    return true;
  }

  private boolean validateVersionIsCorrect(TraceLocation value, ConstraintValidatorContext context) {
    if (value.getVersion() != this.version) {
      final String msg = "Trace Location Version contains an invalid value.";
      logger.info(msg);
      addViolation(context, "Trace Location Version contains an invalid value.");
      return false;
    }
    return true;
  }

  private boolean validateGuidIsEmpty(TraceLocation value, ConstraintValidatorContext context) {
    if (!value.getGuid().isEmpty()) {
      final String msg = "Trace Location Guid must be empty or unset";
      logger.info(msg);
      addViolation(context, msg);
      return false;
    }
    return true;
  }

  private void addViolation(ConstraintValidatorContext validatorContext, String message) {
    validatorContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
  }
}
