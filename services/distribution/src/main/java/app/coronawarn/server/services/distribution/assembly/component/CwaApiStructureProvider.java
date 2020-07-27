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

import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.decorator.indexing.IndexingDecoratorOnDisk;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Assembles the content underneath the {@code /version} path of the CWA API.
 */
@Component
public class CwaApiStructureProvider {

  public static final String VERSION_DIRECTORY = "version";
  public static final String VERSION_V1 = "v1";

  private final AppConfigurationStructureProvider appConfigurationStructureProvider;

  private final DiagnosisKeysStructureProvider diagnosisKeysStructureProvider;

  private final DistributionServiceConfig distributionServiceConfig;

  /**
   * Creates a new CwaApiStructureProvider.
   */
  CwaApiStructureProvider(
      AppConfigurationStructureProvider appConfigurationStructureProvider,
      DiagnosisKeysStructureProvider diagnosisKeysStructureProvider,
      DistributionServiceConfig distributionServiceConfig) {
    this.appConfigurationStructureProvider = appConfigurationStructureProvider;
    this.diagnosisKeysStructureProvider = diagnosisKeysStructureProvider;
    this.distributionServiceConfig = distributionServiceConfig;
  }

  /**
   * Returns the base directory.
   */
  public Directory<WritableOnDisk> getDirectory() {
    IndexDirectoryOnDisk<String> versionDirectory =
        new IndexDirectoryOnDisk<>(VERSION_DIRECTORY, __ -> Set.of(VERSION_V1), Object::toString);

    versionDirectory
        .addWritableToAll(__ -> appConfigurationStructureProvider.getAppConfiguration());
    versionDirectory.addWritableToAll(__ -> diagnosisKeysStructureProvider.getDiagnosisKeys());

    return new IndexingDecoratorOnDisk<>(versionDirectory, distributionServiceConfig.getOutputFileName());
  }
}
