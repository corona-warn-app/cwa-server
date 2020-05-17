package app.coronawarn.server.services.distribution.objectstore.publish;

import java.util.Map;

/**
 * Provides Meta information as available on S3.
 */
public interface MetadataProvider {

  /**
   * Fetches the metadata for a given S3 key.
   *
   * @param s3Key the S3 key, e.g. version/v1/file
   * @return the meta information - if no meta is set, will return an empty Map
   */
  Map<String, String> fetchMetadataFor(String s3Key);
}
