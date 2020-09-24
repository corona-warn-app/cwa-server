

package app.coronawarn.server.services.distribution.objectstore.publish;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import app.coronawarn.server.services.distribution.objectstore.client.S3Object;
import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PublishedFileSetTest {

  @ParameterizedTest
  @ValueSource(strings = {
      "version/v1/diagnosis-keys/country/DE/date/2020-01-01",
      "version/v1/diagnosis-keys/country/DE/date/2020-06-11/hour/0",
      "version/v1/diagnosis-keys/country/DE/date/2020-06-11/hour/23"})
  void testShouldNotPublishWithoutForceUpdateConfiguration(String key) {
    List<S3Object> s3Objects = List.of(new S3Object(key.replace('/', File.separatorChar), "1234"));
    PublishedFileSet publishedSet = new PublishedFileSet(s3Objects,  false);
    LocalFile testFile = new LocalIndexFile(Path.of("/root", key, "/index"), Path.of("/root"));
    assertFalse(publishedSet.shouldPublish(testFile));
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "version/v1/diagnosis-keys/country/DE/date/2020-01-01",
      "version/v1/diagnosis-keys/country/DE/date/2020-06-11/hour/0",
      "version/v1/diagnosis-keys/country/DE/date/2020-06-11/hour/23"})
  void testShouldPublishWithForceUpdateConfiguration(String key) {
    List<S3Object> s3Objects = List.of(new S3Object(key, "1234"));
    PublishedFileSet publishedSet = new PublishedFileSet(s3Objects, true);
    LocalFile testFile = new LocalIndexFile(Path.of("/root", key, "/index"), Path.of("/root"));
    assertTrue(publishedSet.shouldPublish(testFile));
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "version/v1/diagnosis-keys/country/DE/date/2020-01-01",
      "version/v1/diagnosis-keys/country/DE/date/2020-06-11/hour/0",
      "version/v1/diagnosis-keys/country/DE/date/2020-06-11/hour/23"})
  void testShouldPublishWhenObjectStoreEmpty(String key) {
    PublishedFileSet publishedSet = new PublishedFileSet(Collections.emptyList(), false);
    LocalFile testFile = new LocalIndexFile(Path.of("/root", key, "/index"), Path.of("/root"));
    assertTrue(publishedSet.shouldPublish(testFile));
  }

}
