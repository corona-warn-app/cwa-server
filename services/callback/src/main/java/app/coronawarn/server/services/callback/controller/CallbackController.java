package app.coronawarn.server.services.callback.controller;

import app.coronawarn.server.common.persistence.domain.FederationBatchInfo;
import app.coronawarn.server.common.persistence.service.FederationBatchInfoService;
import app.coronawarn.server.services.callback.config.CallbackServiceConfig;
import io.micrometer.core.annotation.Timed;
import java.time.LocalDate;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/version/v1")
@Validated
public class CallbackController {

  /**
   * The route to the callback endpoint (version agnostic).
   */
  public static final String CALLBACK_ROUTE = "/callback";
  private final FederationBatchInfoService federationBatchInfoService;
  private final CallbackServiceConfig config;
  private static final Logger logger = LoggerFactory.getLogger(CallbackController.class);

  public CallbackController(FederationBatchInfoService federationBatchInfoService, CallbackServiceConfig config) {
    this.federationBatchInfoService = federationBatchInfoService;
    this.config = config;
  }

  /**
   * Handles Callback GET requests from Federation Gateway.
   *
   * @param batchTag The batchTag for the latest batch.
   * @param date     The date of the batch.
   * @return An empty response body.
   */
  @GetMapping(value = CALLBACK_ROUTE, params = {"batchTag!="})
  @Timed(description = "Time spent handling callback.")
  public ResponseEntity<Void> handleCallback(@RequestParam String batchTag,
      @NotNull @DateTimeFormat(iso = ISO.DATE) @RequestParam LocalDate date) {
    logger.info("BatchInfo with tag {} and date {} received from federation gateway {}.",
        batchTag, date, config.getSourceSystem());
    FederationBatchInfo federationBatchInfo = new FederationBatchInfo(batchTag, date, config.getSourceSystem());
    boolean savedSuccessfully = federationBatchInfoService.save(federationBatchInfo);
    if (savedSuccessfully) {
      logger.info("BatchInfo with tag {} and date {} was persisted successfully with status {} in system {}.",
          federationBatchInfo.getBatchTag(), federationBatchInfo.getDate(), federationBatchInfo.getStatus(),
          config.getSourceSystem());
    } else {
      logger.warn("BatchInfo with tag {} already existed in system {} and was not persisted.",
          federationBatchInfo.getBatchTag(), config.getSourceSystem());
    }
    return ResponseEntity.ok().build();
  }
}
