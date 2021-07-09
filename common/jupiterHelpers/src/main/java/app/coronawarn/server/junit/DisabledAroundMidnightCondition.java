package app.coronawarn.server.junit;

import static java.time.temporal.ChronoUnit.MINUTES;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.disabled;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.enabled;
import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

public class DisabledAroundMidnightCondition implements ExecutionCondition {

  @Override
  public ConditionEvaluationResult evaluateExecutionCondition(final ExtensionContext context) {
    final Optional<DisabledAroundMidnight> optional = findAnnotation(context.getElement(),
        DisabledAroundMidnight.class);
    if (optional.isEmpty()) {
      return enabled("@DisabledAroundMidnight is not present");
    }

    final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
    final LocalDateTime tonight = LocalDateTime.of(LocalDate.now(ZoneOffset.UTC), LocalTime.MIDNIGHT);
    final LocalDateTime nextnight = LocalDateTime.of(LocalDate.now(ZoneOffset.UTC).plusDays(1), LocalTime.MIDNIGHT);
    final int minutes = optional.get().offsetInMinutes();

    if (MINUTES.between(tonight, now) <= minutes || MINUTES.between(now, nextnight) <= minutes) {
      return disabled(now + " is closer than " + minutes + " minutes to midnight that's why we skip the test.");
    }
    return enabled(now + " is more than " + minutes + " minutes off by midnight that's why we execute the test.");
  }
}
