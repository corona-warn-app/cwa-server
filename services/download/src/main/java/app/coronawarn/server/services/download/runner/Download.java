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

package app.coronawarn.server.services.download.runner;

import app.coronawarn.server.services.download.download.DiagnosisKeyBatchProcessor;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneOffset;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * This runner retrieves diagnosis key batches.
 */
@Component
@Order(1)
public class Download implements ApplicationRunner {
  private final DiagnosisKeyBatchProcessor batchProcessor;

  Download(DiagnosisKeyBatchProcessor batchProcessor) {
    this.batchProcessor = batchProcessor;
  }

  @Override
  public void run(ApplicationArguments args) {
    LocalDate yesterday = LocalDate.now(ZoneOffset.UTC).minus(Period.ofDays(1));
    batchProcessor.saveFirstBatchInfoForDate(yesterday);
    batchProcessor.processErrorFederationBatches();
    batchProcessor.processUnprocessedFederationBatches();
  }
}
