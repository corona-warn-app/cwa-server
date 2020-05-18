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

import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectoryImpl;
import app.coronawarn.server.services.distribution.assembly.structure.directory.decorator.IndexingDecorator;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Assembles the content underneath the {@code /version} path of the CWA API.
 */
@Component
public class CwaApiStructureProvider {

  public static final String VERSION_DIRECTORY = "version";
  public static final String VERSION_V1 = "v1";

  @Autowired
  private ExposureConfigurationStructureProvider exposureConfigurationStructureProvider;

  @Autowired
  private DiagnosisKeysStructureProvider diagnosisKeysStructureProvider;

  /**
   * Returns the base directory.
   */
  public Directory getDirectory() {
    IndexDirectory<?> versionDirectory =
        new IndexDirectoryImpl<>(VERSION_DIRECTORY, __ -> Set.of(VERSION_V1), Object::toString);

    versionDirectory
        .addDirectoryToAll(__ -> exposureConfigurationStructureProvider.getExposureConfiguration());
    versionDirectory.addDirectoryToAll(__ -> diagnosisKeysStructureProvider.getDiagnosisKeys());

    return new IndexingDecorator<>(versionDirectory);
  }
}
