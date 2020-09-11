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


import app.coronawarn.server.services.submission.config.SubmissionServiceConfig.Payload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
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

  private SubmissionServiceConfig submissionServiceConfig;

  @BeforeEach
  void setup() {
    submissionServiceConfig = new SubmissionServiceConfig();
  }

  @ParameterizedTest
  @MethodSource("validRequestDataSizes")
  void ok(DataSize dataSize) {
    Errors errors = validateConfig(dataSize, "DE");
    assertThat(errors.hasErrors()).isFalse();
  }

  @ParameterizedTest
  @MethodSource("invalidRequestDataSizes")
  void fail(DataSize dataSize) {
    Errors errors = validateConfig(dataSize, "DE");
    assertThat(errors.hasErrors()).isTrue();
  }

  @ParameterizedTest
  @MethodSource("setInvalidSupportedCountries")
  void fail(String supportedCountries) {
    Errors errors = validateConfig(SubmissionServiceConfigValidator.MAX_MAXIMUM_REQUEST_SIZE, supportedCountries);
    assertThat(errors.hasErrors()).isTrue();
  }

  @ParameterizedTest
  @ValueSource(strings = {"DE", "DE,FR"})
  void ok(String supportedCountries) {
    Errors errors = validateConfig(SubmissionServiceConfigValidator.MAX_MAXIMUM_REQUEST_SIZE, supportedCountries);
    assertThat(errors.hasErrors()).isFalse();
  }

  private Errors validateConfig(DataSize dataSize, String supportedCountries) {
    String[] supportedCountriesList = supportedCountries.split(",");
    Errors errors = new BeanPropertyBindingResult(submissionServiceConfig, "submissionServiceConfig");
    submissionServiceConfig.setMaximumRequestSize(dataSize);
    submissionServiceConfig.setPayload(new Payload());
    submissionServiceConfig.setSupportedCountries(supportedCountriesList);
    submissionServiceConfigValidator.validate(submissionServiceConfig, errors);
    return errors;
  }

  private static Stream<Arguments> validRequestDataSizes() {
    return Stream.of(
        SubmissionServiceConfigValidator.MAX_MAXIMUM_REQUEST_SIZE,
        SubmissionServiceConfigValidator.MIN_MAXIMUM_REQUEST_SIZE
    ).map(Arguments::of);
  }

  private static Stream<Arguments> invalidRequestDataSizes() {
    return Stream.of(
        DataSize.ofBytes(SubmissionServiceConfigValidator.MAX_MAXIMUM_REQUEST_SIZE.toBytes() + 1),
        DataSize.ofBytes(SubmissionServiceConfigValidator.MIN_MAXIMUM_REQUEST_SIZE.toBytes() - 1)
    ).map(Arguments::of);
  }

  private static Stream<Arguments> setInvalidSupportedCountries() {
    return Stream.of(
        Arguments.of("DE,FRE"),
        Arguments.of("DE, "),
        Arguments.of("de"),
        Arguments.of("dE"),
        Arguments.of("De"),
        Arguments.of(" "),
        Arguments.of(""),
        Arguments.of("\\")
    );
  }
}

