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

package app.coronawarn.server.services.submission.config;


import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.unit.DataSize;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"disable-ssl-client-verification", "disable-ssl-client-verification-verify-hostname"})
class SubmissionServiceConfigValidatorTest {

  @Autowired
  private SubmissionServiceConfigValidator submissionServiceConfigValidator;

  @ParameterizedTest
  @MethodSource("validRequestSizes")
  void ok(DataSize dataSize) {
    SubmissionServiceConfig submissionServiceConfig = new SubmissionServiceConfig();
    submissionServiceConfig.setMaximumRequestSize(dataSize);
    Errors errors = new BeanPropertyBindingResult(submissionServiceConfig, "validSubmissionServiceConfig");

    submissionServiceConfigValidator.validate(submissionServiceConfig, errors);

    assertThat(errors.hasErrors()).isFalse();

  }

  @ParameterizedTest
  @MethodSource("invalidRequestSizes")
  void fail(DataSize dataSize) {
    SubmissionServiceConfig submissionServiceConfig = new SubmissionServiceConfig();
    submissionServiceConfig.setMaximumRequestSize(dataSize);
    Errors errors = new BeanPropertyBindingResult(submissionServiceConfig, "invalidSubmissionServiceConfig");

    submissionServiceConfigValidator.validate(submissionServiceConfig, errors);

    assertThat(errors.hasErrors()).isTrue();

  }

  private static Stream<Arguments> validRequestSizes() {
    return Stream.of(
        SubmissionServiceConfigValidator.MAX_MAXIMUM_REQUEST_SIZE,
        SubmissionServiceConfigValidator.MIN_MAXIMUM_REQUEST_SIZE
    ).map(Arguments::of);
  }

  private static Stream<Arguments> invalidRequestSizes() {
    return Stream.of(
        DataSize.ofKilobytes(SubmissionServiceConfigValidator.MAX_MAXIMUM_REQUEST_SIZE.toKilobytes() + 1),
        DataSize.ofBytes(SubmissionServiceConfigValidator.MIN_MAXIMUM_REQUEST_SIZE.toBytes() - 1)
    ).map(Arguments::of);
  }
}
