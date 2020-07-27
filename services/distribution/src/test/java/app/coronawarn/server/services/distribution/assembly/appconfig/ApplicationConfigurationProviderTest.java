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

package app.coronawarn.server.services.distribution.assembly.appconfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ApplicationConfigurationProviderTest {

  @Test
  void okFile() throws UnableToLoadFileException {
    var result = ApplicationConfigurationProvider.readFile("configtests/app-config_ok.yaml");
    assertThat(result).withFailMessage("File is null, indicating loading failed").isNotNull();
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "configtests/app-config_empty.yaml",
      "configtests/wrong_file.yaml",
      "configtests/broken_syntax.yaml",
      "file_does_not_exist_anywhere.yaml"
  })
  void throwsLoadFailure(String fileName) {
    assertThatExceptionOfType(UnableToLoadFileException.class)
        .isThrownBy(() -> ApplicationConfigurationProvider.readFile(fileName));
  }
}
