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

import app.coronawarn.server.common.protocols.internal.RiskScoreClassification;

/**
 * Provides the risk score classification based on a file on the file system. The existing file must be a valid YAML
 * file, and must match the specification of the proto file risk_score_classification.proto.
 */
public class RiskScoreClassificationProvider {

  private RiskScoreClassificationProvider() {
  }

  /**
   * The location of the risk score classification master file.
   */
  public static final String MASTER_FILE = "master-config/risk-score-classification.yaml";

  /**
   * Fetches the master configuration as a {@link RiskScoreClassification} instance.
   *
   * @return the risk score classification as {@link RiskScoreClassification}
   * @throws UnableToLoadFileException when the file/transformation did not succeed
   */
  public static RiskScoreClassification readMasterFile() throws UnableToLoadFileException {
    return readFile(MASTER_FILE);
  }

  /**
   * Fetches a risk score classification file based on the given path. The path must be available in the classloader.
   *
   * @param path The path, e.g. folder/my-risk-score-classification.yaml
   * @return the RiskScoreClassification
   * @throws UnableToLoadFileException when the file access/transformation did not succeed
   */
  public static RiskScoreClassification readFile(String path) throws UnableToLoadFileException {
    return YamlLoader.loadYamlIntoProtobufBuilder(path, RiskScoreClassification.Builder.class).build();
  }
}
