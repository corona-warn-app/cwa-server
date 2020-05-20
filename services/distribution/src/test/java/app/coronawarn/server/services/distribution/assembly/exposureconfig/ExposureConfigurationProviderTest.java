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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import app.coronawarn.server.common.protocols.internal.RiskScoreParameters;
import org.junit.jupiter.api.Test;

public class ExposureConfigurationProviderTest {

  @Test
  public void okFile() throws UnableToLoadFileException {
    RiskScoreParameters result =
        ExposureConfigurationProvider.readFile("parameters/all_ok.yaml");

    assertNotNull(result, "File is null, indicating loading failed");
  }

  @Test
  public void wrongFile() {
    assertThrows(UnableToLoadFileException.class, () ->
        ExposureConfigurationProvider.readFile("parameters/wrong_file.yaml"));
  }

  @Test
  public void brokenSyntax() {
    assertThrows(UnableToLoadFileException.class, () ->
        ExposureConfigurationProvider.readFile("parameters/broken_syntax.yaml"));
  }

  @Test
  public void doesNotExist() {
    assertThrows(UnableToLoadFileException.class, () ->
        ExposureConfigurationProvider.readFile("file_does_not_exist_anywhere.yaml"));
  }
}
