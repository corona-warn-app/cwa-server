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

package app.coronawarn.server.services.distribution.assembly.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * A class containing helper functions for general purpose file IO.
 */
public class IO {

  private IO() {
  }

  /**
   * Create a file on the disk if it does not already exist.
   *
   * @param root The parent file.
   * @param name The name of the new file.
   */
  public static void makeNewFile(File root, String name) {
    File directory = new File(root, name);
    try {
      if (!directory.createNewFile()) {
        throw new IOException("Could not create " + name + ", file already exists");
      }
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to create file: " + name, e);
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
      throw new UncheckedIOException("Could not write file " + outputFile, e);
    }
  }
}
