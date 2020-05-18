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

package app.coronawarn.server.services.distribution.assembly.structure.directory;

import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.file.File;
import java.util.Set;

/**
 * A {@link Writable} containing {@link File files} and further {@link Directory directories}.
 */
public interface Directory extends Writable {

  /**
   * Adds a {@link File file} to the {@link DirectoryImpl#getFiles files} of this {@link Directory}.
   */
  void addFile(File file);

  /**
   * Returns all {@link File files} contained in this {@link Directory}.
   */
  Set<File> getFiles();

  /**
   * Adds a {@link Directory directory} to the {@link DirectoryImpl#getDirectories directories} of this {@link
   * Directory}.
   */
  void addDirectory(Directory directory);


  /**
   * Returns all {@link Directory directories} contained in this {@link Directory}.
   */
  Set<Directory> getDirectories();
}
