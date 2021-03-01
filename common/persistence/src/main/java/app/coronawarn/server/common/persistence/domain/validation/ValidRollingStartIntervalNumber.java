

package app.coronawarn.server.common.persistence.domain.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Constraint(validatedBy = ValidRollingStartIntervalNumberValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ValidRollingStartIntervalNumber {

  /**
   * Error message.
   *
   * @return the error message
   */
  String message() default "Rolling start interval number must be greater 0 and cannot be in the future.";

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
