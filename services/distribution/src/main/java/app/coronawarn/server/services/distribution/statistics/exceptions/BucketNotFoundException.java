package app.coronawarn.server.services.distribution.statistics.exceptions;

@SuppressWarnings("serial")
public class BucketNotFoundException extends RuntimeException {
  public BucketNotFoundException(final String bucketName, final Throwable cause) {
    super(String.format("Bucket not found: %s", bucketName), cause);
  }
}
