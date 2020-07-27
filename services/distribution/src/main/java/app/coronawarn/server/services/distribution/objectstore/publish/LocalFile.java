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

package app.coronawarn.server.services.distribution.objectstore.publish;

import app.coronawarn.server.services.distribution.assembly.structure.file.FileOnDiskWithChecksum;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a file, which is subject for publishing to S3.
 */
public abstract class LocalFile {

  private static final Logger logger = LoggerFactory.getLogger(LocalFile.class);

  /**
   * the path to the file to be represented.
   */
  private final Path file;

  /**
   * the assigned S3 key.
   */
  private final String s3Key;

  /**
   * the checksum of this file.
   */
  private String checksum = "";

  /**
   * Constructs a new file representing a file on the disk.
   *
   * @param file     the path to the file to be represented
   * @param basePath the base path
   */
  public LocalFile(Path file, Path basePath) {
    this.file = file;
    this.s3Key = createS3Key(file, basePath);
    this.checksum = loadChecksum();
  }

  public String getS3Key() {
    return s3Key;
  }

  public String getChecksum() {
    return checksum;
  }

  public Path getFile() {
    return file;
  }

  private String loadChecksum() {
    try {
      return Files.readString(FileOnDiskWithChecksum.buildChecksumPathForFile(file)).trim();
    } catch (IOException e) {
      logger.debug("Unable to load checksum file.");
      return "";
    }
  }

  protected String createS3Key(Path file, Path rootFolder) {
    Path relativePath = rootFolder.relativize(file);
    return relativePath.toString().replaceAll("\\\\", "/");
  }
}
