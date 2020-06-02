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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.DigestUtils;

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
   * the etag of this file.
   */
  private final String etag;

  /**
   * Constructs a new file representing a file on the disk.
   *
   * @param file     the path to the file to be represented
   * @param basePath the base path
   */
  public LocalFile(Path file, Path basePath) {
    this.file = file;
    this.etag = computeS3ETag();
    this.s3Key = createS3Key(file, basePath);
  }

  public String getS3Key() {
    return s3Key;
  }

  public String getEtag() {
    return etag;
  }

  public Path getFile() {
    return file;
  }

  private String computeS3ETag() {
    try {
      String md5 = DigestUtils.md5DigestAsHex(Files.readAllBytes(file));
      byte[] raw = Hex.decode(md5.toUpperCase());

      return DigestUtils.md5DigestAsHex(raw) + "-1";
    } catch (IOException e) {
      logger.warn("Unable to compute E-Tag", e);
    }

    return "";
  }

  protected String createS3Key(Path file, Path rootFolder) {
    Path relativePath = rootFolder.relativize(file);
    return relativePath.toString().replaceAll("\\\\", "/");
  }
}
