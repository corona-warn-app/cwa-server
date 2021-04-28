package app.coronawarn.server.services.submission.audit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Service;

@Service
@Aspect
public class ExecutionTimeAspect {

  private TrackExecutionTimeProcessor trackExecutionTimeProcessor;

  public ExecutionTimeAspect(TrackExecutionTimeProcessor trackExecutionTimeProcessor) {
    this.trackExecutionTimeProcessor = trackExecutionTimeProcessor;
  }

  /**
   * Return actual value of the called method and measures how much time takes in milliseconds.
   *
   * @param joinPoint interception point
   * @return actual value of the called method
   * @throws Throwable exception
   */
  @Around("@annotation(TrackExecutionTime)")
  public Object trackTime(ProceedingJoinPoint joinPoint) throws Throwable {
    Long startTime = System.currentTimeMillis();
    Object result = joinPoint.proceed();
    Long trackedTime = System.currentTimeMillis() - startTime;

    trackExecutionTimeProcessor.addExecutionTime(
        ((MethodSignature) joinPoint.getSignature()).getMethod().getName(), trackedTime);

    return result;
  }
}
