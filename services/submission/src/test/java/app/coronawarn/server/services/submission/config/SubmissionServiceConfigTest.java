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

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * This test has a dependency on the test/application.yml values, due to limitations in general
 * testing of configuration properties in a Spring (5.x) application context (i.e. the TestPropertySource annotation
 * can't be defined at the test method level in order to easily recreate scenarios and test javax.validation constraints
 * defined for properties)
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"disable-ssl-client-verification", "disable-ssl-client-verification-verify-hostname"})
public class SubmissionServiceConfigTest {

  @Autowired
  private SubmissionServiceConfig config;


  @Test
  void testCountryAllowed() {
    Assert.assertTrue(config.isCountryAllowed("DE"));
    Assert.assertTrue(config.isCountryAllowed("de"));
    Assert.assertTrue(config.isCountryAllowed("De"));
    Assert.assertTrue(config.isCountryAllowed("dE"));
  }

  @Test
  void testCountryNotAllowed() {
    Assert.assertFalse(config.isCountryAllowed("xXy"));
    Assert.assertFalse(config.isCountryAllowed("xyZ"));
    Assert.assertFalse( config.isCountryAllowed("123"));
    Assert.assertFalse(config.isCountryAllowed("xY-"));
    Assert.assertFalse(config.isCountryAllowed("\\"));
    Assert.assertFalse(config.isCountryAllowed("dE,DS"));
    Assert.assertFalse(config.isCountryAllowed(","));
    Assert.assertFalse( config.isCountryAllowed(""));
    Assert.assertFalse(config.isCountryAllowed(" "));
    Assert.assertFalse(config.isCountryAllowed(null));
  }

  @Test
  void testCountriesAllowed() {
    Assert.assertTrue(config.areAllCountriesAllowed(List.of("DE")));
  }

  @Test
  void testCountriesNotAllowed() {
    Assert.assertFalse(config.areAllCountriesAllowed(List.of("xXy", "DE")));
    Assert.assertFalse(config.areAllCountriesAllowed(List.of("FR", "xyZ")));
    Assert.assertFalse( config.areAllCountriesAllowed(List.of("123", "DE")));
    Assert.assertFalse(config.areAllCountriesAllowed(List.of("DE", "xY-")));
    Assert.assertFalse(config.areAllCountriesAllowed(List.of("de", "fr", "\\")));
    Assert.assertFalse(config.areAllCountriesAllowed(List.of("fr", "uk", "dE,DS")));
    Assert.assertFalse(config.areAllCountriesAllowed(List.of(",", "uk")));
    Assert.assertFalse( config.areAllCountriesAllowed(List.of("", "fr")));
    Assert.assertFalse(config.areAllCountriesAllowed(List.of("de"," ")));

    List<String> includingNull = new ArrayList<String>();
    includingNull.add(null);
    includingNull.add("DE");
    Assert.assertFalse(config.areAllCountriesAllowed(includingNull));
  }

}
