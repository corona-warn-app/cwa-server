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

package app.coronawarn.server.services.download.config;

import app.coronawarn.server.services.download.config.DownloadServiceConfig.TekFieldDerivations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {DownloadServiceConfigValidator.class})
@DirtiesContext
public class DownloadServiceConfigValidatorTest {

  @Autowired
  private DownloadServiceConfigValidator downloadServiceConfigValidator;

  private DownloadServiceConfig downloadServiceConfig;

  @BeforeEach
  void setup() {
    downloadServiceConfig = new DownloadServiceConfig();
  }

  @ParameterizedTest
  @MethodSource("validTransmissionRiskLevelFromDaysSinceOnsetOfSymptoms")
  void testWithValidTrlFromDsos(Map<Integer, Integer> transmissionRiskLevelFromDaysSinceOnsetOfSymptoms) {
    TekFieldDerivations tekFieldDerivations = new TekFieldDerivations();
    tekFieldDerivations.setTrlFromDsos(transmissionRiskLevelFromDaysSinceOnsetOfSymptoms);
    Errors errors = validateConfig(tekFieldDerivations);
    assertThat(errors.hasErrors()).isFalse();
  }

  @ParameterizedTest
  @MethodSource("invalidTransmissionRiskLevelFromDaysSinceOnsetOfSymptoms")
  void testWithInvalidTrlFromDsos(Map<Integer, Integer> transmissionRiskLevelFromDaysSinceOnsetOfSymptoms) {
    TekFieldDerivations tekFieldDerivations = new TekFieldDerivations();
    tekFieldDerivations.setTrlFromDsos(transmissionRiskLevelFromDaysSinceOnsetOfSymptoms);
    Errors errors = validateConfig(tekFieldDerivations);
    assertThat(errors.hasErrors()).isTrue();
  }

  private Errors validateConfig(TekFieldDerivations tekFieldDerivations) {
    Errors errors = new BeanPropertyBindingResult(downloadServiceConfig, "downloadServiceConfig");
    downloadServiceConfig.setTekFieldDerivations(tekFieldDerivations);
    downloadServiceConfigValidator.validate(downloadServiceConfig, errors);
    return errors;
  }

  private static Stream<Arguments> validTransmissionRiskLevelFromDaysSinceOnsetOfSymptoms() {
    Map<Integer, Integer> validMapping1 = Stream.of(new Integer[][] {
        {14, 1},
        {13, 1},
        {3, 3},
        {0, 8},
        {-1, 6},
        {-3, 3},
        {-14, 1}
    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

    Map<Integer, Integer> validMapping2 = Stream.of(new Integer[][] {
        {14, 1},
        {13, 2},
        {3, 3},
        {0, 4},
        {-1, 5},
        {-3, 6},
        {-14, 7},
        {-2, 8}
    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

    return Stream.of(
        Arguments.of(validMapping1),
        Arguments.of(validMapping2)
    );
  }

  private static Stream<Arguments> invalidTransmissionRiskLevelFromDaysSinceOnsetOfSymptoms() {
    return Stream.of(
        Arguments.of(Map.of(4001, 1)),
        Arguments.of(Map.of(14, 9)),
        Arguments.of(Map.of(14, 0)),
        Arguments.of(Map.of(-15, 1))
    );
  }
}
