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

package app.coronawarn.server.services.distribution.assembly.appconfig.validation;

public class TestWithExpectedResult {

  private final String prefixPath;

  public final String file;

  public final ValidationResult result = new ValidationResult();

  public TestWithExpectedResult(String file) {
    this(file, "parameters/");
  }

  public TestWithExpectedResult(String file, String prefixPath) {
    this.file = file;
    this.prefixPath = prefixPath;
  }


  public TestWithExpectedResult with(ValidationError error) {
    this.result.add(error);
    return this;
  }

  public String path() {
    return prefixPath + file;
  }

  @Override
  public String toString() {
    return file;
  }

  public static class Builder {

    private String folder;

    public Builder(String folder) {
      this.folder = folder;
    }

    public TestWithExpectedResult build(String file) {
      return new TestWithExpectedResult(file, this.folder);
    }
  }
}