package app.coronawarn.server.services.submission.logging;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
public class LoggingAspect {


  private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

  @Before("execution(app.coronawarn.server.services.distribution.runner)")
  public void writeTestDataAdvice() {

  }

}
