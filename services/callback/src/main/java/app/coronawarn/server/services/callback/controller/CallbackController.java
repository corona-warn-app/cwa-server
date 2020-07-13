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

import app.coronawarn.server.common.persistence.repository.FederationBatchDownloadRepository;
import io.micrometer.core.annotation.Timed;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
  private static final Logger logger = LoggerFactory.getLogger(CallbackController.class);
  private static final String dateRegex = "^\\d{4}-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])$";
  private final FederationBatchDownloadRepository federationBatchDownloadRepository;

  public CallbackController(FederationBatchDownloadRepository federationBatchDownloadRepository) {
    this.federationBatchDownloadRepository = federationBatchDownloadRepository;
  }

  /**
   * Handles diagnosis key submission requests.
   *
   * @param batchTag   The batchTag for the latest batch.
   * @param date The date of the batch.
   * @return An empty response body.
   */
  @GetMapping(value = CALLBACK_ROUTE)
  @Timed(description = "Time spent handling callback.")
  public ResponseEntity<Void> handleCallback(@RequestParam String batchTag,
      @Valid @Pattern(regexp = dateRegex) @RequestParam String date)
      throws ParseException {
    federationBatchDownloadRepository.saveDoNothingOnConflict(batchTag, parseDateString(date));
    return ResponseEntity.ok().build();
  }

  private Date parseDateString(
      @RequestParam @Valid @Pattern(regexp = dateRegex) String date)
      throws ParseException {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    return sdf.parse(date);
  }

}
