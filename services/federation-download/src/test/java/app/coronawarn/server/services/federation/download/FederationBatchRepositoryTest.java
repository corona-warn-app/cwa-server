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

package app.coronawarn.server.services.federation.download;


import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.common.persistence.domain.FederationBatch;
import app.coronawarn.server.common.persistence.domain.FederationBatchStatus;
import app.coronawarn.server.common.persistence.repository.FederationBatchRepository;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest
@ExtendWith({SpringExtension.class})
@AutoConfigureWebTestClient
public class FederationBatchRepositoryTest {

  @Autowired
  private FederationBatchRepository federationBatchRepository;

  private static final String firstBatchTag = "11111";
  private static final String secondBatchTag = "22222";
  private static final String thirdBatchTag = "33333";
  private static final String fourthBatchTag = "44444";

  private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
  private static final Date firstDate = getDateFromString("2020-08-15");
  private static final Date secondDate = getDateFromString("2020-08-16");
  private static final Date thirdDate = getDateFromString("2020-08-17");
  private static final Date fourthDate = getDateFromString("2020-08-18");

  private static final FederationBatchStatus error = FederationBatchStatus.ERROR;
  private static final FederationBatchStatus processed = FederationBatchStatus.PROCESSED;
  private static final FederationBatchStatus nullStatus = null;

  private static Stream<Arguments> getUnprocessedBatchArgumentsSortedByDateDescending() {
    return Stream.of(
        Arguments.of(fourthBatchTag, fourthDate, error),
        Arguments.of(thirdBatchTag, thirdDate, nullStatus),
        Arguments.of(secondBatchTag, secondDate, error),
        Arguments.of(firstBatchTag, firstDate, error)
    );
  }

  private static Date getDateFromString(String date) {
    try {
      return sdf.parse(date);
    } catch (ParseException ex) {
      return null;
    }
  }

  @AfterEach
  public void tearDown() {
    federationBatchRepository.deleteAll();
  }

  @Test
  public void testReturnsNullIfNoUnprocessedBatch() {
    federationBatchRepository.saveDoNothingOnConflict(firstBatchTag, firstDate, processed);
    assertThat(federationBatchRepository.findOldestUnprocessedFederationBatch() == null);
  }

  @ParameterizedTest
  @MethodSource("getUnprocessedBatchArgumentsSortedByDateDescending")
  public void testOnlyOldestBatchIsReturned(String batchTag, Date date, FederationBatchStatus status) {
    federationBatchRepository.saveDoNothingOnConflict(batchTag, date, status);
    assertThat(validateBatchPropertiesOfOldestUnprocessedBatch(batchTag, date, status));
  }

  @Test
  public void testProcessedBatchDoesNotOverwriteUnprocessedBatch() {
    federationBatchRepository.saveDoNothingOnConflict(secondBatchTag, secondDate, error);
    federationBatchRepository.saveDoNothingOnConflict(thirdBatchTag, thirdDate, error);
    assertThat(validateBatchPropertiesOfOldestUnprocessedBatch(secondBatchTag, secondDate, error));
    federationBatchRepository.saveDoNothingOnConflict(firstBatchTag, firstDate, processed);
    assertThat(validateBatchPropertiesOfOldestUnprocessedBatch(secondBatchTag, secondDate, error));
  }

  @Test
  public void testDoesNothingOnConflict() {
    federationBatchRepository.saveDoNothingOnConflict(secondBatchTag, secondDate, error);
    federationBatchRepository.saveDoNothingOnConflict(secondBatchTag, firstDate, error);

    assertThat(validateBatchPropertiesOfOldestUnprocessedBatch(secondBatchTag, secondDate, error));
  }

  private boolean validateBatchPropertiesOfOldestUnprocessedBatch(String batchTag, Date date,
      FederationBatchStatus status) {
    FederationBatch federationBatch = federationBatchRepository.findOldestUnprocessedFederationBatch();
    return Objects.equals(federationBatch.getBatchTag(), batchTag)
        && Objects.equals(federationBatch.getDate(), date)
        && Objects.equals(federationBatch.getStatus(), status);
  }
}
