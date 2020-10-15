package app.coronawarn.server.services.submission.logging;

import app.coronawarn.server.common.Logger;
import app.coronawarn.server.common.LoggerFactory;
import app.coronawarn.server.services.submission.verification.Tan;
import feign.FeignException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * Responsible for intercepting methods to increase logging opportunities.
 */
@Aspect
@Component
public class LoggingAspect {

  private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

  /**
   * Advice for capturing whether the {@link app.coronawarn.server.services.submission.verification.TanVerifier}
   * verifies the tan correctly.
   *
   * @param joinPoint Point of execution
   * @param tanString The provided tan that will be checked
   * @return A boolean indicating whether the tan is acceptable or not
   * @throws Throwable Exception that is thrown when the tan verification failed
   */
  @Around("execution(* app.coronawarn.server.services."
      + "submission.verification.TanVerifier.verifyTan(..)) && args(tanString)")
  public boolean verifyTanAdvice(ProceedingJoinPoint joinPoint, String tanString) throws Throwable {
    try {
      return (boolean) joinPoint.proceed();
    } catch (IllegalArgumentException exception) {
      logger.error(SubmissionLogMessages.TAN_VERIFICATION_FAILED_MESSAGE,
          tanString.substring(0, Math.min(36, tanString.length())), tanString.length());
      return false;
    }
  }

  /**
   * Advice for {@link app.coronawarn.server.services.submission.verification.VerificationServerClient#verifyTan(Tan)}
   * to log that tan verification is called and finished.
   *
   * @param proceedingJoinPoint Point of execution
   * @throws Throwable Exception that is thrown when the tan cannot be verified
   */
  @Around("execution(* app.coronawarn.server.services."
      + "submission.verification.VerificationServerClient.verifyTan(..))")
  public void verifyTanWithVerificationServerAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
    logger.info(SubmissionLogMessages.TAN_VERIFICATION_SERVICE_CALLED_MESSAGE);
    proceedingJoinPoint.proceed();
    logger.info(SubmissionLogMessages.TAN_VERIFICATION_RESPONSE_RECEIVED);
  }

  /**
   * Advice for {@link app.coronawarn.server.services.submission.verification.VerificationServerClient} to log after an
   * {@link FeignException.NotFound} was thrown.
   *
   * @param notFoundException The exception that was thrown
   */
  @AfterThrowing(pointcut = "execution(* app.coronawarn.server.services."
      + "submission.verification.VerificationServerClient.verifyTan(..))", throwing = "notFoundException")
  public void afterUnverifiedTanAdvice(FeignException.NotFound notFoundException) {
    logger.info(SubmissionLogMessages.UNVERIFIED_TAN_MESSAGE);
  }
}
