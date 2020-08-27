package app.coronawarn.server.services.federation.upload.runner;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.service.FederationUploadKeyService;
import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKeyBatch;
import app.coronawarn.server.services.federation.upload.Application;
import app.coronawarn.server.services.federation.upload.DiagnosisKeyBatchAssembler;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class Upload implements ApplicationRunner {

  private static final Logger logger = LoggerFactory.getLogger(Upload.class);

  private final FederationUploadKeyService uploadKeyService;
  private final ApplicationContext applicationContext;
  private final DiagnosisKeyBatchAssembler batchAssembler;

  Upload(FederationUploadKeyService uploadKeyService, ApplicationContext applicationContext,
      DiagnosisKeyBatchAssembler batchAssembler) {
    this.uploadKeyService = uploadKeyService;
    this.applicationContext = applicationContext;
    this.batchAssembler = batchAssembler;
  }

  @Override
  public void run(ApplicationArguments args) throws Exception {
    logger.info("Running Upload Job");
    try {
      List<DiagnosisKey> pendingUploadKeys = uploadKeyService.getPendingUploadKeys();
      List<DiagnosisKeyBatch> batches = batchAssembler.assembleDiagnosisKeyBatch(pendingUploadKeys);
    } catch (Exception e) {
      logger.error("Upload diagnosis key data failed.", e);
      Application.killApplication(applicationContext);
    }
  }
}
