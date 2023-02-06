package app.coronawarn.server.common.persistence.domain.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = ValidSubmissionTypeValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ValidSubmissionType {

  /**
   * Error message.
   *
   * @return the error message
   */
  String message() default "Submission type must be either PCR or RAPID.";

  /**
   * Groups.
   *
   * @return interface for groups
   */
  Class<?>[] groups() default {};

  /**
   * Payload.
   *
   * @return interface for payload which inherits attributes of Payload
   */
  Class<? extends Payload>[] payload() default {};
}
