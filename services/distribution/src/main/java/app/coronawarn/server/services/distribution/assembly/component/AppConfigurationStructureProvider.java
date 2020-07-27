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

package app.coronawarn.server.services.distribution.assembly.component;

import app.coronawarn.server.services.distribution.assembly.appconfig.structure.directory.AppConfigurationDirectory;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import org.springframework.stereotype.Component;

/**
 * Reads configuration parameters from the respective files in the class path and builds a {@link
 * AppConfigurationDirectory} with them.
 */
@Component
public class AppConfigurationStructureProvider {

  private final CryptoProvider cryptoProvider;
  private final DistributionServiceConfig distributionServiceConfig;

  AppConfigurationStructureProvider(CryptoProvider cryptoProvider,
      DistributionServiceConfig distributionServiceConfig) {
    this.cryptoProvider = cryptoProvider;
    this.distributionServiceConfig = distributionServiceConfig;
  }

  public Directory<WritableOnDisk> getAppConfiguration() {
    return new AppConfigurationDirectory(cryptoProvider, distributionServiceConfig);
  }
}
