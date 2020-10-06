

package app.coronawarn.server.services.callback.controller;

import app.coronawarn.server.common.persistence.domain.FederationBatchInfo;
import app.coronawarn.server.common.persistence.service.FederationBatchInfoService;
import io.micrometer.core.annotation.Timed;
import java.time.LocalDate;
import javax.validation.Valid;
import javax.validation.constraints.Pattern;
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
  private static final String DATE_REGEX = "^\\d{4}-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])$";
  private final FederationBatchInfoService federationBatchInfoService;

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
      @Valid @Pattern(regexp = DATE_REGEX) @RequestParam String date) {
    FederationBatchInfo federationBatchInfo = new FederationBatchInfo(batchTag, LocalDate.parse(date));
    federationBatchInfoService.save(federationBatchInfo);
    return ResponseEntity.ok().build();
  }
}
