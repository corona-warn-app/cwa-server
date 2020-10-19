package app.coronawarn.server.services.distribution.logging;

import static app.coronawarn.server.services.distribution.logging.LogMessages.*;

import app.coronawarn.server.services.distribution.Application;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.objectstore.client.ObjectStoreOperationFailedException;
import app.coronawarn.server.services.distribution.objectstore.publish.LocalFile;
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
public class DistributionLoggingAspect {

  private final ApplicationContext applicationContext;
  private final DistributionServiceConfig config;


  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  public DistributionLoggingAspect(ApplicationContext applicationContext, DistributionServiceConfig config) {
    this.applicationContext = applicationContext;
    this.config = config;
  }

  @Before(value =
      "execution(* app.coronawarn.server.services.distribution.assembly.component.DiagnosisKeysStructureProvider"
          + ".getDiagnosisKeys())")
  public void beforeGetDiagnosisKeysInDiagnosisKeysStructureProvider() {
    logger.debug(QUERYING_DIAGNOSIS_KEYS_FROM_THE_DATABASE);
  }

  @Before(value = "execution(* app.coronawarn.server.services.distribution.assembly.component.OutputDirectoryProvider"
      + ".clear())")
  public void beforeClearOutputDirectoryProvider() {
    logger.debug(CLEARING_OUTPUT_DIRECTORY);
  }

  @AfterReturning(value = "execution(* app.coronawarn.server.services.distribution.runner.Assembly.run(..))")
  public void afterReturningAssemblyRun() {
    logger.debug(DISTRIBUTION_DATA_ASSEMBLED_SUCCESSFULLY);
  }


  @AfterReturning(value = "execution(* app.coronawarn.server.services.distribution.runner.S3Distribution.run(..))")
  public void afterReturningS3DistributionRun() {
    logger.info(DATA_PUSHED_TO_OBJECT_STORE_SUCCESSFULLY);
  }

  @AfterThrowing(pointcut = "execution(* app.coronawarn.server.services.distribution.runner.Assembly.run(..))",
      throwing = "ex")
  public void afterThrowingAssemblyRun(Exception ex) {
    logger.error(DISTRIBUTION_DATA_ASSEMBLY_FAILED, ex);
    Application.killApplication(applicationContext);
  }

  @AfterThrowing(pointcut = "execution(* app.coronawarn.server.services.distribution.runner.S3Distribution.run(..))",
      throwing = "ex")
  public void afterThrowingS3DistributionRun(Exception ex) {
    logger.error(DISTRIBUTION_FAILED, ex);
    Application.killApplication(applicationContext);
  }

  @AfterReturning(value = "execution(* app.coronawarn.server.services.distribution.runner.RetentionPolicy.run(..))")
  public void afterReturningRetentionPolicyRun() {
    logger.debug(RETENTION_POLICY_APPLIED_SUCCESSFULLY);
  }

  @AfterThrowing(pointcut = "execution(* app.coronawarn.server.services.distribution.runner.RetentionPolicy.run(..))",
      throwing = "ex")
  public void afterThrowingRetentionPolicyRun(Exception ex) {
    logger.error(APPLICATION_OF_RETENTION_POLICY_FAILED, ex);
    Application.killApplication(applicationContext);
  }

  @Before(value =
      "execution(* app.coronawarn.server.services.distribution.objectstore.FailedObjectStoreOperationsCounter"
          + ".incrementAndCheckThreshold(..))"
          + "&& args(cause)")
  public void beforeIncrementAndCheckThreshold(ObjectStoreOperationFailedException cause) {
    logger.error(OBJECT_STORE_OPERATION_FAILED, cause);
  }

  @AfterThrowing(pointcut =
      "execution(* app.coronawarn.server.services.distribution.objectstore.FailedObjectStoreOperationsCounter"
          + ".incrementAndCheckThreshold(..))")
  public void afterThrowingIncrementAndCheckThreshold() {
    logger.error(NUMBER_OF_FAILED_OBJECT_STORE_OPERATIONS_EXCEEDED_THRESHOLD,
        config.getObjectStore().getMaxNumberOfFailedOperations());
  }

  @Before("execution(* app.coronawarn.server.services.distribution.Application.destroy())")
  public void beforeLog4j2Shutdown() {
    logger.info(SHUTTING_DOWN_LOG4J2_MESSAGE);
  }

  @Before("execution(* app.coronawarn.server.services.distribution.objectstore.ObjectStoreAccess.putObject(..))"
      + "&& args(localFile)")
  public void beforeObjectStorePutObject(LocalFile localFile) {
    logger.info(UPLOADING_MESSAGE, localFile.getS3Key());
  }
}
