/*
 * Corona-Warn-App
 *
 * SAP SE and all other contributors /
 * copyright owners license this file to you under the Apache
 * License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package app.coronawarn.server.services.distribution.assembly.exposureconfig;

import static org.junit.jupiter.api.Assertions.assertEquals;

import app.coronawarn.server.services.distribution.assembly.exposureconfig.validation.ExposureConfigurationValidator;
import app.coronawarn.server.services.distribution.assembly.exposureconfig.validation.ValidationResult;
import org.junit.jupiter.api.Test;

/**
 * This test will verify that the provided Exposure Configuration master file is syntactically correct and according to
 * spec.
 * <p>
 * There should never be any deployment when this test is failing.
 */
public class ExposureConfigurationProviderMasterFileTest {

  private static final ValidationResult SUCCESS = new ValidationResult();

  @Test
  public void testMasterFile() throws UnableToLoadFileException {
    var config = ExposureConfigurationProvider.readMasterFile();

    var validator = new ExposureConfigurationValidator(config);
    ValidationResult result = validator.validate();

    assertEquals(SUCCESS, result);
  }
}
