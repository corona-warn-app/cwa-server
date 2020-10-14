package app.coronawarn.server.services.submission.logging;

import app.coronawarn.server.common.persistence.repository.DiagnosisKeyRepository;
import app.coronawarn.server.common.protocols.external.exposurenotification.TemporaryExposureKey;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload;
import java.util.List;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

  @Autowired
  DiagnosisKeyRepository keyRepository;


  private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

  @Around(
      "execution(* app.coronawarn.server.services.submission.controller."
          + "SubmissionController.submitDiagnosisKey(..)) ")
  public void submitDiagnosisKeyAdvice(ProceedingJoinPoint proceedingJoinPoint)
      throws Throwable {
    logger.info("test test");
    proceedingJoinPoint.proceed();
  }
}
