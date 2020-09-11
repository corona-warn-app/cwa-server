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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"disable-ssl-client-verification", "disable-ssl-client-verification-verify-hostname"})

public class CallbackControllerTest {

  @Autowired
  private RequestExecutor executor;

  private final static String batchTag = "batchTag";
  private final static String validDateString = "2020-05-05";

  @Test
  void ok() {
    ResponseEntity<Void> actResponse = executor.executeGet(batchTag, validDateString);
    assertThat(actResponse.getStatusCode()).isEqualTo(OK);
  }

  @ParameterizedTest
  @MethodSource("createTestString")
  void failsWithBadRequest(String batchTag, String dateString) {
    ResponseEntity<Void> actResponse = executor.executeGet(batchTag, dateString);
    assertThat(actResponse.getStatusCode()).isEqualTo(BAD_REQUEST);
  }

  private static Stream<Arguments> createTestString() {
    return Stream.of(
        Arguments.of(batchTag, null),
        Arguments.of(batchTag, ""),
        Arguments.of("", ""),
        Arguments.of(null, null),
        Arguments.of(null, validDateString),
        Arguments.of("", validDateString),
        Arguments.of(batchTag, "2020-20-20")
    );
  }
}
