package app.coronawarn.server.services.distribution.objectstore;

import app.coronawarn.server.services.distribution.objectstore.publish.PublishFileSet;
import app.coronawarn.server.services.distribution.objectstore.publish.PublishedFileSet;
import java.io.IOException;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Publishes a folder on the disk to S3 while keeping the folder and file structure.<br>
 * Moreover, does the following:
 * <br>
 * <ul>
 *   <li>Publishes index files on a different route, removing the trailing "/index" part.</li>
 *   <li>Adds meta information to the uploaded files, e.g. the sha256 hash value.</li>
 *   <li>Only performs the upload for files, which do not yet exist on the object store, and
 *   checks whether the existing files hash differ from the to-be-uploaded files hash. Only if the
 *   hash differs, the file will ultimately be uploaded</li>
 *   <li>Currently not implemented: Set cache control headers</li>
 *   <li>Currently not implemented: Supports multi threaded upload of files.</li>
 * </ul>
 */
public class S3Publisher {

  private static final Logger logger = LoggerFactory.getLogger(S3Publisher.class);

  /** The default CWA root folder, which contains all CWA related files */
  private static final String CWA_S3_ROOT_DEFAULT = "version";

  /** The CWA root folder to be used, which contains all CWA related files */
  private final String cwaS3Root;

  /** root folder for the upload on the local disk */
  private final Path root;

  /** access to the object store */
  private final ObjectStoreAccess access;

  public S3Publisher(Path root, ObjectStoreAccess access) {
    this(root, access, CWA_S3_ROOT_DEFAULT);
  }

  public S3Publisher(Path root, ObjectStoreAccess access, String s3Root) {
    this.root = root;
    this.access = access;
    this.cwaS3Root = s3Root;
  }

  /**
   * Synchronizes the files to S3.
   *
   * @throws IOException in case there were problems reading files from the disk.
   */
  public void publish() throws IOException {
    var published = new PublishedFileSet(access.getObjectsWithPrefix(cwaS3Root), access);
    var toPublish = new PublishFileSet(root);

    var diff = toPublish
        .getFiles()
        .stream()
        .filter(published::isNotYetPublished);

    logger.info("Beginning upload... ");
    diff.forEach(access::putObject);
    logger.info("Upload completed.");
  }

}
