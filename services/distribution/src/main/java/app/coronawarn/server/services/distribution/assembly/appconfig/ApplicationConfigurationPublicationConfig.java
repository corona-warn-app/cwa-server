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

import app.coronawarn.server.common.protocols.internal.AppFeatures;
import app.coronawarn.server.common.protocols.internal.ApplicationConfiguration;
import app.coronawarn.server.common.protocols.internal.ApplicationConfiguration.Builder;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Provides the application configuration needed for the mobile client. Contains all necessary sub-configs, including:
 * <ul>
 *   <li>Exposure Configuration</li>
 *   <li>Risk Score Classification</li>
 *   <li>App Config, e.g. minimum risk threshold</li>
 * </ul>
 *
 * <p>The application config is fetched from the master-config folder.</p>
 */
@Configuration
public class ApplicationConfigurationPublicationConfig {

  /**
   * The location of the exposure configuration master file.
   */
  public static final String MASTER_FILE = "master-config/app-config.yaml";

  /**
   * Fetches the master configuration as a ApplicationConfiguration instance.
   *
   * @return the exposure configuration as ApplicationConfiguration
   * @throws UnableToLoadFileException when the file/transformation did not succeed
   */
  @Bean
  public ApplicationConfiguration createMasterConfiguration(DistributionServiceConfig distributionServiceConfig)
      throws UnableToLoadFileException {
    return YamlLoader.loadYamlIntoProtobufBuilder(MASTER_FILE, Builder.class)
        .setAppFeatures(
            AppFeatures.newBuilder().addAllAppFeatures(distributionServiceConfig.getAppFeaturesProto()).build())
        .build();
  }
}
