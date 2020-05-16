package app.coronawarn.server.services.distribution.objectstore;

import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.transfer.MultipleFileUpload;
import com.amazonaws.services.s3.transfer.Transfer.TransferState;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.TransferProgress;
import com.amazonaws.services.s3.transfer.Upload;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * <p>Grants access to the object store, enabling basic functionality for working with files.</p>
 * <p>Use S3Publisher for more convenient access.</p>
 * <br>
 * Make sure the following ENV vars are available.
 * <ul>
 * <li>cwa.objectstore.endpoint</li>
 * <li>cwa.objectstore.bucket</li>
 * <li>AWS_ACCESS_KEY_ID</li>
 * <li>AWS_SECRET_ACCESS_KEY</li>
 * </ul>
 */
@Component
public class ObjectStoreAccess {

  private static final Logger logger = LoggerFactory.getLogger(ObjectStoreAccess.class);

  private final String bucket;

  private S3Client client;

  private AmazonS3 oldClient;

  @Autowired
  public ObjectStoreAccess(@Value("${cwa.objectstore.endpoint:notset}") String endpoint,
      @Value("${cwa.objectstore.bucket:notset}") String bucket) throws URISyntaxException {
    this.bucket = bucket;

    if ("notset".equals(endpoint) || "notset".equals(bucket)) {
      logger.error("S3 Connection parameters missing - unable to serve S3 integration.");
      return;
    }

    this.client = S3Client.builder()
        .endpointOverride(new URI(endpoint))
        .region(Region.EU_CENTRAL_1) /* required by SDK, but ignored on S3 side */
        .build();

    var endpointConfiguration = new EndpointConfiguration(endpoint,
        Regions.DEFAULT_REGION.getName());

    this.oldClient = AmazonS3ClientBuilder
        .standard()
        .withEndpointConfiguration(endpointConfiguration)
        .build();
  }

  /**
   * Stores an object in the object store.
   *
   * @param key the key to use, e.g. my/folder/struc/file.ext
   * @param file the file to upload
   */
  public void putObject(String key, File file) {
    RequestBody bodyFile = RequestBody.fromFile(file);

    this.client
        .putObject(PutObjectRequest.builder().bucket(this.bucket).key(key).build(), bodyFile);
  }

  public void putObjects(String keyName, List<File> files) throws InterruptedException {
    int maxUploadThreads = 5;
    TransferManager tm = TransferManagerBuilder.standard()
        .withS3Client(this.oldClient)
        .withMultipartUploadThreshold((long) (5 * 1024 * 1025))
        .withExecutorFactory(() -> Executors.newFixedThreadPool(maxUploadThreads))
        .build();

    logger.info("Starting upload");
    MultipleFileUpload multipleFileUpload = tm
        .uploadFileList(this.bucket, "cktest/", new File("."), files);

    //showMultiUploadProgress(multipleFileUpload);
    multipleFileUpload.waitForCompletion();
    logger.info("Finished.");
  }

  public static void showMultiUploadProgress(MultipleFileUpload multi_upload) {
    // print the upload's human-readable description
    System.out.println(multi_upload.getDescription());

    // snippet-start:[s3.java1.s3_xfer_mgr_progress.substranferes]
    Collection<? extends Upload> sub_xfers = new ArrayList<Upload>();
    sub_xfers = multi_upload.getSubTransfers();

    do {
      System.out.println("\nSubtransfer progress:\n");
      for (Upload u : sub_xfers) {
        System.out.println("  " + u.getDescription());
        if (u.isDone()) {
          TransferState xfer_state = u.getState();
          System.out.println("  " + xfer_state);
        } else {
          TransferProgress progress = u.getProgress();
          double pct = progress.getPercentTransferred();
          System.out.print(pct);
          System.out.println();
        }
      }

      // wait a bit before the next update.
      try {
        Thread.sleep(200);
      } catch (InterruptedException e) {
        return;
      }
    } while (multi_upload.isDone() == false);
    // print the final state of the transfer.
    TransferState xfer_state = multi_upload.getState();
    System.out.println("\nMultipleFileUpload " + xfer_state);
    // snippet-end:[s3.java1.s3_xfer_mgr_progress.substranferes]
  }

  /**
   * Deletes objects in the object store, based on the given prefix (folder structure).
   *
   * @param prefix the prefix, e.g. my/folder/
   */
  public void deleteObjectsWithPrefix(String prefix) {
    ListObjectsV2Response files = getObjectsWithPrefix(prefix);
    Collection<ObjectIdentifier> identifiers = files
        .contents()
        .stream()
        .map(s3object -> ObjectIdentifier.builder().key(s3object.key()).build())
        .collect(Collectors.toList());

    this.client.deleteObjects(DeleteObjectsRequest.builder().bucket(this.bucket).delete(
        Delete.builder().objects(identifiers).build()).build());
  }

  /**
   * Fetches the list of objects in the store with the given prefix.
   *
   * @param prefix the prefix, e.g. my/folder/
   * @return the list of objects
   */
  public ListObjectsV2Response getObjectsWithPrefix(String prefix) {
    return client
        .listObjectsV2(ListObjectsV2Request.builder().prefix(prefix).bucket(this.bucket).build());
  }

  private void trackUploadProgress() {

  }

}
