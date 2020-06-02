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

import app.coronawarn.server.common.protocols.internal.RiskScoreParameters;

/**
 * Provides the Exposure Configuration based on a file in the file system.<br> The existing file must be a valid YAML
 * file, and must match the specification of the proto file risk_score_parameters.proto.
 */
public class ExposureConfigurationProvider {

  private ExposureConfigurationProvider() {
  }

  /**
   * The location of the exposure configuration master file.
   */
  public static final String MASTER_FILE = "master-config/exposure-config.yaml";

  /**
   * Fetches the master configuration as a RiskScoreParameters instance.
   *
   * @return the exposure configuration as RiskScoreParameters
   * @throws UnableToLoadFileException when the file/transformation did not succeed
   */
  public static RiskScoreParameters readMasterFile() throws UnableToLoadFileException {
    return readFile(MASTER_FILE);
  }

  /**
   * Fetches an exposure configuration file based on the given path. The path must be available in the classloader.
   *
   * @param path the path, e.g. folder/my-exposure-configuration.yaml
   * @return the RiskScoreParameters
   * @throws UnableToLoadFileException when the file/transformation did not succeed
   */
  public static RiskScoreParameters readFile(String path) throws UnableToLoadFileException {
    return YamlLoader.loadYamlIntoProtobufBuilder(path, RiskScoreParameters.Builder.class).build();
  }
}
