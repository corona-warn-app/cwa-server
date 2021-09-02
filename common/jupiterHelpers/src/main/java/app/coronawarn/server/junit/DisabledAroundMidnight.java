package app.coronawarn.server.junit;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;

@Target({ METHOD, TYPE, ANNOTATION_TYPE })
@Retention(RUNTIME)
@ExtendWith(DisabledAroundMidnightCondition.class)
public @interface DisabledAroundMidnight {
  /**
   * Minutes before/after midnight the test should be disabled.
   * 
   * @return <code>60</code> as default.
   */
  int offsetInMinutes() default 60;
}
