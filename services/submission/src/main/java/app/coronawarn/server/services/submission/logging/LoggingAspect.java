package app.coronawarn.server.services.submission.logging;

import app.coronawarn.server.common.Logger;
import app.coronawarn.server.common.LoggerFactory;
import app.coronawarn.server.common.persistence.service.common.LogMessages;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

  private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

  @Around(
      "execution(* app.coronawarn.server.services.submission.controller."
          + "SubmissionController.submitDiagnosisKey(SubmissionPayload exposureKeys, ..)) ")
  public void submitDiagnosisKeyAdvice(ProceedingJoinPoint proceedingJoinPoint)
      throws Throwable {
    logger.info(LogMessages.KEYS_PICKED_FROM_UPLOAD_TABLE);
    proceedingJoinPoint.proceed();
  }

  @AfterThrowing("execution(* app.coronawarn.server.services.submission.verification.TanVerifier.verifyTan(String tanString))")
  public void verifyTanAdvice(ProceedingJoinPoint proceedingJoinPoint) {
    // TODO FR logger
  }
}
