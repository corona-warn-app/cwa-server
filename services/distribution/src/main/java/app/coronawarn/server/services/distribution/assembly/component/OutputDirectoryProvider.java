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
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryImpl;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Creates and clears a {@link Directory} on disk, which is defined by the application properties.
 */
@Component
public class OutputDirectoryProvider {

  private static final Logger logger = LoggerFactory.getLogger(OutputDirectoryProvider.class);

  @Value("${services.distribution.paths.output}")
  private String outputPath;

  public Directory getDirectory() {
    return new DirectoryImpl(getFileOnDisk());
  }

  public java.io.File getFileOnDisk() {
    return new java.io.File(outputPath);
  }

  /**
   * Removes the output directory and all files and directories within it.
   *
   * @throws IOException if file operation fails
   */
  public void clear() throws IOException {
    logger.debug("Clearing output directory...");
    java.io.File outputDirectory = getFileOnDisk();
    FileUtils.deleteDirectory(outputDirectory);
    outputDirectory.mkdirs();
  }
}
