package app.coronawarn.server.services.eventregistration.boundary.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;

@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TraceLocationValidator.class)
public @interface ValidTraceLocation {

  /**
   * Message in case validation fails.
   *
   * @return the error message.
   */
  String message() default "Invalid trace location payload";

  /**
   * Validation groups.
   */
  Class<?>[] groups() default {};

  /**
   * Validation payload.
   */
  Class<? extends Object>[] payload() default {};

}
