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

package app.coronawarn.server.services.distribution.assembly.structure.file;

import app.coronawarn.server.services.distribution.assembly.io.IO;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.util.DigestUtils;

/**
 * A {@link File} that can be written to disk, alongside a second {@link File} containing the hashsum of the actual
 * {@link FileOnDisk}.<br> The checksum file resides in the same folder as the actual file, and will use the same file
 * name with suffix {@link FileOnDiskWithChecksum#CHECKSUM_FILE_SUFFIX}.
 */
public class FileOnDiskWithChecksum extends FileOnDisk {

  /**
   * The file suffix for checksum files.
   */
  public static final String CHECKSUM_FILE_SUFFIX = ".checksum";

  /**
   * Creates a new file on disk. A separate checksum will be generated automatically in the same folder.
   *
   * @param name  the name of the file
   * @param bytes the content of the file
   */
  public FileOnDiskWithChecksum(String name, byte[] bytes) {
    super(name, bytes);
  }

  @Override
  public void write() {
    super.write();

    writeChecksumFile();
  }

  private void writeChecksumFile() {
    var checksumFile = getRoot().toPath().resolve(super.getName() + CHECKSUM_FILE_SUFFIX);
    byte[] checksum = computeChecksum(this.getBytesForChecksum()).getBytes(StandardCharsets.UTF_8);

    IO.makeNewFile(checksumFile.getParent().toFile(), checksumFile.getFileName().toString());
    IO.writeBytesToFile(checksum, checksumFile.toFile());
  }

  /**
   * Fetches the target bytes for computation of the checksum. Will take the bytes of its {@link FileOnDisk}.
   *
   * @return the checksum bytes
   */
  protected byte[] getBytesForChecksum() {
    return super.getBytes();
  }

  private static String computeChecksum(byte[] fileContent) {
    String md5 = DigestUtils.md5DigestAsHex(fileContent);
    byte[] raw = Hex.decode(md5.toUpperCase());

    return DigestUtils.md5DigestAsHex(raw);
  }

  /**
   * Checks whether the given path translate to a checksum file.
   *
   * @param path the path handle of the checksum file
   * @return true if it is a checksum file, false otherwise
   */
  public static boolean isChecksumFile(Path path) {
    return path.toString().endsWith(CHECKSUM_FILE_SUFFIX);
  }

  /**
   * Constructs and returns the checksum {@link Path} handle for a given file.
   *
   * @param file the file to create the checksum path for
   * @return the checksum path, which incorporates the original file + the checksum suffix.
   */
  public static Path buildChecksumPathForFile(Path file) {
    return Path.of(file.toString() + CHECKSUM_FILE_SUFFIX);
  }
}
