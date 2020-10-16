package app.coronawarn.server.services.distribution.logging;

import app.coronawarn.server.services.distribution.Application;
import app.coronawarn.server.services.distribution.objectstore.client.ObjectStoreOperationFailedException;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

  private final ApplicationContext applicationContext;


  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  public LoggingAspect(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  @Before(value =
      "execution(* app.coronawarn.server.services.distribution.assembly.component.DiagnosisKeysStructureProvider"
          + ".getDiagnosisKeys())")
  public void beforeGetDiagnosisKeysInDiagnosisKeysStructureProvider() {
    logger.debug("Querying diagnosis keys from the database...");
  }

  @Before(value = "execution(* app.coronawarn.server.services.distribution.assembly.component.OutputDirectoryProvider"
      + ".clear())")
  public void beforeClearOutputDirectoryProvider() {
    logger.debug("Clearing output directory...");
  }

  @AfterReturning(value = "execution(* app.coronawarn.server.services.distribution.runner.Assembly.run(..))")
  public void afterReturningAssemblyRun() {
    logger.debug("Distribution data assembled successfully.");
  }


  @AfterReturning(value = "execution(* app.coronawarn.server.services.distribution.runner.S3Distribution.run(..))")
  public void afterReturningS3DistributionRun() {
    logger.info("Data pushed to Object Store successfully.");
  }

  @AfterThrowing(pointcut = "execution(* app.coronawarn.server.services.distribution.runner.Assembly.run(..))",
      throwing = "ex")
  public void afterThrowingAssemblyRun(Exception ex) {
    logger.error("Distribution data assembly failed.", ex);
    Application.killApplication(applicationContext);
  }

  @AfterThrowing(pointcut = "execution(* app.coronawarn.server.services.distribution.runner.S3Distribution.run(..))",
      throwing = "ex")
  public void afterThrowingS3DistributionRun(Exception ex) {
    logger.error("Distribution failed.", ex);
    Application.killApplication(applicationContext);
  }

  @AfterReturning(value = "execution(* app.coronawarn.server.services.distribution.runner.RetentionPolicy.run(..))")
  public void afterReturningRetentionPolicyRun() {
    logger.debug("Retention policy applied successfully.");
  }

  @AfterThrowing(pointcut = "execution(* app.coronawarn.server.services.distribution.runner.RetentionPolicy.run(..))",
      throwing = "ex")
  public void afterThrowingRetentionPolicyRun(Exception ex) {
    logger.error("Application of retention policy failed.", ex);
    Application.killApplication(applicationContext);
  }

  @Before(value =
      "execution(* app.coronawarn.server.services.distribution.objectstore.FailedObjectStoreOperationsCounter"
          + ".incrementAndCheckThreshold(..))"
          + "&& args(cause)")
  public void beforeIncrementAndCheckThreshold(ObjectStoreOperationFailedException cause) {
    logger.error("Object store operation failed.", cause);
  }
}
