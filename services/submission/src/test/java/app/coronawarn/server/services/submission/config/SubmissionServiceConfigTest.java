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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.list;
import static org.mockito.Mockito.mock;

import javax.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.util.stream.Stream;

/**
 * This test has a dependency on the test/application.yml values, due to limitations in general testing of configuration
 * properties in a Spring (5.x) application context (i.e. the TestPropertySource annotation can't be defined at the test
 * method level in order to easily recreate scenarios and test javax.validation constraints defined for properties)
 */

@ActiveProfiles({"disable-ssl-client-verification", "disable-ssl-client-verification-verify-hostname"})
@EnableConfigurationProperties(value = SubmissionServiceConfig.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SubmissionServiceConfig.class})
@SpringBootTest(classes = SubmissionServiceConfigTest.class)

public class SubmissionServiceConfigTest {

  @Autowired
  private SupportedCountriesValidator supportedCountriesValidator;
  @Autowired
  private SubmissionServiceConfig submissionServiceConfig;

  @ParameterizedTest
  @MethodSource("setInvalidSupportedCountries")
  void testInvalidSupportedCountries(String supportedCountries) {
    ConstraintValidatorContext constraintValidatorContext = mock(ConstraintValidatorContext.class);

    submissionServiceConfig.setSupportedCountries(supportedCountries);

    list(submissionServiceConfig.getSupportedCountries()).forEach(country -> {
      boolean test = supportedCountriesValidator.isValid(
          country, constraintValidatorContext);
      assertThat(test).isFalse();
    });

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
/*
  @Test
  void testCountryNotAllowed() {
    Assert.assertFalse(submissionServiceConfig.isCountryAllowed("xXy"));
    Assert.assertFalse(submissionServiceConfig.isCountryAllowed("xyZ"));
    Assert.assertFalse( submissionServiceConfig.isCountryAllowed("123"));
    Assert.assertFalse(submissionServiceConfig.isCountryAllowed("xY-"));
    Assert.assertFalse(submissionServiceConfig.isCountryAllowed("\\"));
    Assert.assertFalse(submissionServiceConfig.isCountryAllowed("dE,DS"));
    Assert.assertFalse(submissionServiceConfig.isCountryAllowed(","));
    Assert.assertFalse( submissionServiceConfig.isCountryAllowed(""));
    Assert.assertFalse(submissionServiceConfig.isCountryAllowed(" "));
    Assert.assertFalse(submissionServiceConfig.isCountryAllowed(null));
  }

  @Test
  void testCountriesAllowed() {
    Assert.assertTrue(submissionServiceConfig.areAllCountriesAllowed(List.of("DE", "uK", "Fr", "dk")));
  }

  @Test
  void testCountriesNotAllowed() {
    Assert.assertFalse(submissionServiceConfig.areAllCountriesAllowed(List.of("xXy", "DE")));
    Assert.assertFalse(submissionServiceConfig.areAllCountriesAllowed(List.of("FR", "xyZ")));
    Assert.assertFalse( submissionServiceConfig.areAllCountriesAllowed(List.of("123", "DE")));
    Assert.assertFalse(submissionServiceConfig.areAllCountriesAllowed(List.of("DE", "xY-")));
    Assert.assertFalse(submissionServiceConfig.areAllCountriesAllowed(List.of("de", "fr", "\\")));
    Assert.assertFalse(submissionServiceConfig.areAllCountriesAllowed(List.of("fr", "uk", "dE,DS")));
    Assert.assertFalse(submissionServiceConfig.areAllCountriesAllowed(List.of(",", "uk")));
    Assert.assertFalse( submissionServiceConfig.areAllCountriesAllowed(List.of("", "fr")));
    Assert.assertFalse(submissionServiceConfig.areAllCountriesAllowed(List.of("de"," ")));

    List<String> includingNull = new ArrayList<String>();
    includingNull.add(null);
    includingNull.add("DE");
    Assert.assertFalse(submissionServiceConfig.areAllCountriesAllowed(includingNull));
  }


  @ParameterizedTest
  @MethodSource("setValidSupportedCountries")
  void successOnValidSupportedCountries(String supportedCountries) throws UnableToLoadFileException {
    distributionServiceConfig.setSupportedCountries(supportedCountries);

    var validator = buildApplicationConfigurationValidator(distributionServiceConfig);

    assertThat(validator.validate()).isEqualTo(SUCCESS);

  }
  private static Stream<Arguments> setValidSupportedCountries() {
    return Stream.of(
        Arguments.of("DE,FR"),
        Arguments.of("DE")
    );
  }
  public static ValidationResult buildExpectedResult(ValidationError... errors) {
    var validationResult = new ValidationResult();
    Arrays.stream(errors).forEach(validationResult::add);
    return validationResult;
  }

 */
