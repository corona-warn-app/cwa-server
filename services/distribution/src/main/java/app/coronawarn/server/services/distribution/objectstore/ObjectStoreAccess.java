package app.coronawarn.server.services.distribution.objectstore;

import app.coronawarn.server.services.distribution.objectstore.publish.LocalFile;
import app.coronawarn.server.services.distribution.objectstore.publish.MetadataProvider;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidBucketNameException;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.MinioException;
import io.minio.errors.XmlParserException;
import io.minio.messages.Item;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
public class ObjectStoreAccess implements MetadataProvider {

  private static final Logger logger = LoggerFactory.getLogger(ObjectStoreAccess.class);

  private static final String DEFAULT_REGION = "eu-west-1";

  private final String bucket;

  private MinioClient client;

  @Autowired
  public ObjectStoreAccess(ObjectStoreConfigurationProperties configurationProperties)
      throws IOException, GeneralSecurityException, MinioException {
    this.client = createClient(configurationProperties);

    this.bucket = configurationProperties.getBucket();

    if (!this.client.bucketExists(this.bucket)) {
      throw new IllegalArgumentException("Supplied bucket does not exist " + bucket);
    }
  }

  private MinioClient createClient(ObjectStoreConfigurationProperties configurationProperties)
      throws InvalidPortException, InvalidEndpointException {
    if (isSSL(configurationProperties)) {
      return new MinioClient(
          configurationProperties.getEndpoint(),
          configurationProperties.getPort(),
          configurationProperties.getAccessKey(), configurationProperties.getSecretKey(),
          DEFAULT_REGION,
          true
      );
    } else {
      return new MinioClient(
          configurationProperties.getEndpoint(),
          configurationProperties.getPort(),
          configurationProperties.getAccessKey(), configurationProperties.getSecretKey()
      );
    }
  }

  private boolean isSSL(ObjectStoreConfigurationProperties configurationProperties) {
    return configurationProperties.getEndpoint().startsWith("https://");
  }

  /**
   * Stores the target file on the S3.
   *
   * @param localFile the file to be published
   */
  public void putObject(LocalFile localFile)
      throws IOException, GeneralSecurityException, MinioException {
    String s3Key = localFile.getS3Key();

    logger.info("... uploading " + s3Key);

    this.client.putObject(bucket, s3Key, localFile.getFile().toString(), null);
  }

  /**
   * Deletes objects in the object store, based on the given prefix (folder structure).
   *
   * @param prefix the prefix, e.g. my/folder/
   */
  public void deleteObjectsWithPrefix(String prefix)
      throws MinioException, GeneralSecurityException, IOException {
    List<String> toDelete = getObjectsWithPrefix(prefix)
        .stream()
        .map(S3Object::getObjectName)
        .collect(Collectors.toList());

    logger.info("Deleting " + toDelete.size() + " entries with prefix " + prefix);
    var response = this.client.removeObjects(bucket, toDelete);
    response.forEach(err -> {
      try {
        System.err.println(err.get());
      } catch (ErrorResponseException e) {
        e.printStackTrace();
      } catch (InsufficientDataException e) {
        e.printStackTrace();
      } catch (InternalException e) {
        e.printStackTrace();
      } catch (InvalidBucketNameException e) {
        e.printStackTrace();
      } catch (InvalidKeyException e) {
        e.printStackTrace();
      } catch (InvalidResponseException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      } catch (NoSuchAlgorithmException e) {
        e.printStackTrace();
      } catch (XmlParserException e) {
        e.printStackTrace();
      }
    });
    logger.info("Deletion result:" + response);
  }

  /**
   * Fetches the list of objects in the store with the given prefix.
   *
   * @param prefix the prefix, e.g. my/folder/
   * @return the list of objects
   */
  public List<S3Object> getObjectsWithPrefix(String prefix)
      throws IOException, GeneralSecurityException, MinioException {
    var objects = this.client.listObjects(bucket, prefix, true, true, false);

    var list = new ArrayList<S3Object>();
    for (Result<Item> item : objects) {
      list.add(S3Object.of(item.get()));
    }

    return list;
  }

  private Map<String, String> createMetadataFor(LocalFile file) {
    return Map.of("cwa.hash", file.getHash());
  }

  @Override
  public Map<String, String> fetchMetadataFor(String s3Key) {
    return Collections.EMPTY_MAP;
  }


}
