package app.coronawarn.server.services.distribution.statistics.exceptions;

public class BucketNotFoundException extends RuntimeException {

  public BucketNotFoundException(String bucketName) {
    super(String.format("Bucket not found: %s", bucketName));
  }

}
