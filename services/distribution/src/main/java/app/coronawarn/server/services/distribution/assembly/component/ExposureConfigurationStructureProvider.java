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

package app.coronawarn.server.services.distribution.assembly.component;

import app.coronawarn.server.common.protocols.internal.RiskScoreParameters;
import app.coronawarn.server.services.distribution.assembly.exposureconfig.ExposureConfigurationProvider;
import app.coronawarn.server.services.distribution.assembly.exposureconfig.UnableToLoadFileException;
import app.coronawarn.server.services.distribution.assembly.exposureconfig.structure.directory.ExposureConfigurationDirectoryImpl;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Reads the exposure configuration parameters from the respective file in the class path and builds a {@link
 * ExposureConfigurationDirectoryImpl} with them.
 */
@Component
public class ExposureConfigurationStructureProvider {

  private static final Logger logger = LoggerFactory
      .getLogger(ExposureConfigurationStructureProvider.class);

  @Autowired
  private CryptoProvider cryptoProvider;

  public Directory getExposureConfiguration() {
    var riskScoreParameters = readExposureConfiguration();
    return new ExposureConfigurationDirectoryImpl(riskScoreParameters, cryptoProvider);
  }

  private RiskScoreParameters readExposureConfiguration() {
    logger.debug("Reading exposure configuration...");
    try {
      return ExposureConfigurationProvider.readMasterFile();
    } catch (UnableToLoadFileException e) {
      logger.error("Could not load exposure configuration parameters", e);
      throw new RuntimeException(e);
    }
  }
}
