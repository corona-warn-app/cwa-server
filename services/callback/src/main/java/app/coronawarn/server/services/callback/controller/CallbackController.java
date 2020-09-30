/*-
 * ---license-start
 * Corona-Warn-App
 * ---
 * Copyright (C) 2020 SAP SE and all other contributors
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ---license-end
 */

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
