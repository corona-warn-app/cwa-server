package app.coronawarn.server.services.submission.audit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Aspect
public class ExecutionTimeAspect {

  private Logger logger = LoggerFactory.getLogger(ExecutionTimeAspect.class);

  @Autowired
  private TrackExecutionTimeProcessor trackExecutionTimeProcessor;

  /**
   * Return an object.
   *
   * @param joinPoint param
   * @return object
   * @throws Throwable exception
   */
  @Around("@annotation(TrackExecutionTime)")
  public Object trackTime(ProceedingJoinPoint joinPoint) throws Throwable {
    Long trackSystemMillis = System.currentTimeMillis();
    Object result = joinPoint.proceed();
    Long trackedTime = System.currentTimeMillis() - trackSystemMillis;

    trackExecutionTimeProcessor.addExecutionTime(
        ((MethodSignature) joinPoint.getSignature()).getMethod().getName(), trackedTime);

    return result;
  }

}
