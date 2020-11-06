

package app.coronawarn.server.services.callback.controller;

import app.coronawarn.server.common.persistence.domain.FederationBatchInfo;
import app.coronawarn.server.common.persistence.service.FederationBatchInfoService;
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
  private static final Logger logger = LoggerFactory.getLogger(CallbackController.class);

  public CallbackController(FederationBatchInfoService federationBatchInfoService) {
    this.federationBatchInfoService = federationBatchInfoService;
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
  public ResponseEntity<Void> handleCallback(@RequestParam(required = true) String batchTag,
      @NotNull @DateTimeFormat(iso = ISO.DATE) @RequestParam LocalDate date) {
    logger.info("BatchTag {} with date {} received from EFGS.", batchTag, date);
    FederationBatchInfo federationBatchInfo = new FederationBatchInfo(batchTag, date);
    boolean savedSuccessfully = federationBatchInfoService.save(federationBatchInfo);
    if (savedSuccessfully) {
      logger.info("Updated status to {} for batchInfo {}.", federationBatchInfo.getStatus(), batchTag);
      logger.info("BatchInfo {} was persisted successfully.", batchTag);
    } else {
      logger.warn("BatchInfo {} already existed and was not persisted.", batchTag);
    }
    return ResponseEntity.ok().build();
  }
}
