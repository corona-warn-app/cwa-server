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

package app.coronawarn.server.services.distribution.objectstore.publish;

import com.google.common.io.BaseEncoding;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import javax.xml.bind.DatatypeConverter;
import org.springframework.util.DigestUtils;

/**
 * Represents a file, which is subject for publishing to S3.
 */
public abstract class LocalFile {

  /** the path to the file to be represented. */
  private final Path file;

  /** the assigned S3 key. */
  private final String s3Key;

  /** the hash of this file. */
  private final String hash;

  /**
   * Constructs a new file representing a file on the disk.
   *
   * @param file the path to the file to be represented
   * @param basePath the base path
   */
  public LocalFile(Path file, Path basePath) {
    this.file = file;
    this.hash = computeS3ETag();
    this.s3Key = createS3Key(file, basePath);
  }

  public String getS3Key() {
    return s3Key;
  }

  public String getHash() {
    return hash;
  }

  public Path getFile() {
    return file;
  }

  private String hash() {
    try {
      MessageDigest digester = MessageDigest.getInstance("MD5");
      digester.update(Files.readAllBytes(file));

      return DatatypeConverter.printHexBinary(digester.digest());
    } catch (IOException | NoSuchAlgorithmException e) {
      throw new RuntimeException("Unable to compute hashes due to ", e);
    }
  }

  private String hash2() {
    List<String> md5s = List.of(hash());
    StringBuilder stringBuilder = new StringBuilder();
    for (String md5:md5s) {
      stringBuilder.append(md5);
    }

    String hex = stringBuilder.toString();
    byte raw[] = BaseEncoding.base16().decode(hex.toUpperCase());
    String digest = DigestUtils.md5DigestAsHex(raw);

    return digest + "-" + md5s.size();
  }

  private String computeS3ETag() {
    try {
      String md5 = DigestUtils.md5DigestAsHex(Files.readAllBytes(file));
      byte raw[] = BaseEncoding.base16().decode(md5.toUpperCase());

      return DigestUtils.md5DigestAsHex(raw) + "-1";
    } catch (IOException e) {
      e.printStackTrace();
    }

    return "";
  }

  protected String createS3Key(Path file, Path rootFolder) {
    Path relativePath = rootFolder.relativize(file);
    return relativePath.toString().replaceAll("\\\\", "/");
  }
}
