/*
 * ---license-start
 * Corona-Warn-App
 * ---
 * Copyright (C) 2020 SAP SE and all other contributors
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import static org.assertj.core.api.Assertions.catchThrowable;

import app.coronawarn.server.common.protocols.internal.ApplicationVersionConfiguration;
import org.junit.jupiter.api.Test;

class ApplicationVersionConfigurationProviderTest {

  @Test
  void okFile() throws UnableToLoadFileException {
    ApplicationVersionConfiguration result =
        ApplicationVersionConfigurationProvider.readFile("app-version/all_ok.yaml");

    assertThat(result).withFailMessage("File is null, indicating loading failed").isNotNull();
  }

  @Test
  void wrongFile() {
    assertUnableToLoadFile("app-version/wrong_file.yaml");
  }

  @Test
  void brokenSyntax() {
    assertUnableToLoadFile("app-version/broken_syntax.yaml");
  }

  @Test
  void doesNotExist() {
    assertUnableToLoadFile("file_does_not_exist_anywhere.yaml");
  }

  public static void assertUnableToLoadFile(String s) {
    assertThat(catchThrowable(() ->
        ApplicationVersionConfigurationProvider.readFile(s)))
        .isInstanceOf(UnableToLoadFileException.class);
  }
}
