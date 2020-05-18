/*
 * Corona-Warn-App
 *
 * Deutsche Telekom AG, SAP SE and all other contributors /
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

package app.coronawarn.server.services.distribution.assembly.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class containing helper functions for general purpose file IO.
 */
public class IO {

  private static final Logger logger = LoggerFactory.getLogger(IO.class);

  /**
   * Creates a new file on disk.
   *
   * @param root The parent file.
   * @param name The name of the new file.
   */
  public static void makeFile(File root, String name) {
    File directory = new File(root, name);
    try {
      directory.createNewFile();
    } catch (IOException e) {
      logger.error("Failed to create file: {}", name, e);
      throw new RuntimeException(e);
    }
  }

  /**
   * Writes bytes into a file.
   *
   * @param bytes      The content to write
   * @param outputFile The file to write the content into.
   */
  public static void writeBytesToFile(byte[] bytes, File outputFile) {
    try (FileOutputStream outputFileStream = new FileOutputStream(outputFile)) {
      outputFileStream.write(bytes);
    } catch (IOException e) {
      logger.error("Write operation failed.", e);
      throw new RuntimeException(e);
    }
  }
}
