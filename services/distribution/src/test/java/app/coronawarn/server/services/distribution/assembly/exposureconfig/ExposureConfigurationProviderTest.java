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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import app.coronawarn.server.common.protocols.internal.RiskScoreParameters;
import org.junit.jupiter.api.Test;

public class ExposureConfigurationProviderTest {

  @Test
  public void okFile() throws UnableToLoadFileException {
    RiskScoreParameters result =
        ExposureConfigurationProvider.readFile("parameters/all_ok.yaml");

    assertThat(result).withFailMessage("File is null, indicating loading failed").isNotNull();
  }

  @Test
  public void wrongFile() {
    assertUnableToLoadFile("parameters/wrong_file.yaml");
  }

  @Test
  public void brokenSyntax() {
    assertUnableToLoadFile("parameters/broken_syntax.yaml");
  }

  @Test
  public void doesNotExist() {
    assertUnableToLoadFile("file_does_not_exist_anywhere.yaml");
  }

  private void assertUnableToLoadFile(String s) {
    assertThat(catchThrowable(() ->
        ExposureConfigurationProvider.readFile(s)))
        .isInstanceOf(UnableToLoadFileException.class);
  }

}
