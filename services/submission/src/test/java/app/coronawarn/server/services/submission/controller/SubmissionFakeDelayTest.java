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

package app.coronawarn.server.services.submission.controller;

import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.services.submission.config.SubmissionServiceConfig;
import app.coronawarn.server.services.submission.verification.TanVerifier;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SubmissionFakeDelayTest {

  private SubmissionController submissionController;
  private final List<Integer> delays =
      List.of(
          10, 11, 30, 31, 20, 18, 21, 22, 11, 17, 20, 28, 15, 16, 18, 21, 19, 20, 21, 15, 19, 17,
          19);

  @BeforeEach
  void setUp() {
    var config = mock(SubmissionServiceConfig.class);
    when(config.getInitialFakeDelayMilliseconds()).thenReturn(10.0);
    when(config.getFakeDelayMovingAverageSamples()).thenReturn(5.0);

    submissionController =
        new SubmissionController(mock(DiagnosisKeyService.class), mock(TanVerifier.class), config);
  }

  @Test
  void calculatesFakeDelay() {
    for (Integer delay : delays) {
      submissionController.updateFakeDelay(delay);
    }

    assertThat(submissionController.getFakeDelay()).isCloseTo(18.469, Offset.offset(0.01));
  }

  @Test
  void calculatesFakeDelayConcurrently() throws InterruptedException {
    var latch = new CountDownLatch(delays.size());
    var executorService = Executors.newFixedThreadPool(4);
    for (Integer delay : delays) {
      executorService.execute(
          () -> {
            submissionController.updateFakeDelay(delay);
            latch.countDown();
          });
    }
    latch.await(5, TimeUnit.SECONDS);
    assertThat(latch.getCount()).isEqualTo(0);
    executorService.shutdown();

    // Fake delay doesn't break when calculated concurrently
    assertThat(submissionController.getFakeDelay())
        .isBetween(Collections.min(delays).doubleValue(), Collections.max(delays).doubleValue());
  }
}
