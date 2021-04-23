

package app.coronawarn.server.services.distribution.assembly.structure.file;

import app.coronawarn.server.common.shared.util.IoUtils;
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

    IoUtils.makeNewFile(checksumFile.getParent().toFile(), checksumFile.getFileName().toString());
    IoUtils.writeBytesToFile(checksum, checksumFile.toFile());
  }

  /**
   * Fetches the target bytes for computation of the checksum. Will take the bytes of its {@link FileOnDisk}.
   *
   * @return the checksum bytes
   */
  protected byte[] getBytesForChecksum() {
    return super.getBytes();
  }

  /**
   * Calculates the CWA-hash value. If this code changes, please adapt the
   * <a href=
   * "https://github.com/corona-warn-app/cwa-server/blob/HEAD/docs/DISTRIBUTION.md#cwa-hash">CWA-hash</a>
   * documentation. This code is intended to mimic the AWS ETag hash code for multi-part uploads.
   *
   * @see <a href=
   *      "https://stackoverflow.com/questions/12186993/what-is-the-algorithm-to-compute-the-amazon-s3-etag-for-a-file-larger-than-5gb#answer-19896823">answer
   *      on StackOverflow</a>
   *
   * @param fileContent the binary file content
   * @return the calculated hash value
   */
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
