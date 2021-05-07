

package app.coronawarn.server.services.distribution.objectstore.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import app.coronawarn.server.services.distribution.objectstore.ObjectStoreAccess;
import app.coronawarn.server.services.distribution.objectstore.client.S3Object;
import app.coronawarn.server.services.distribution.objectstore.publish.LocalFile;
import app.coronawarn.server.services.distribution.objectstore.publish.LocalGenericFile;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;

class ObjectStoreAccessIT extends BaseS3IntegrationTest {

  public static final String testCwaPrefix = "testing/cwa/";
  private static final String testRunId = testCwaPrefix + UUID.randomUUID().toString() + "/";
  private static final String rootTestFolder = "objectstore/";
  private static final String textFile = rootTestFolder + "store-test-file";
  private static final String testRunBatchesPrefix = testCwaPrefix + "batches/";

  @Autowired
  private ObjectStoreAccess objectStoreAccess;

  @Autowired
  private ResourceLoader resourceLoader;

  @BeforeEach
  void setup() {
    objectStoreAccess.deleteObjectsWithPrefix(testCwaPrefix);
  }


  @Test
  void contextLoads() {
    assertThat(objectStoreAccess).isNotNull();
  }

  @Test
  void defaultIsEmptyTrue() {
    var files = objectStoreAccess.getObjectsWithPrefix(testRunId);

    assertThat(files).withFailMessage("Content should be empty").isEmpty();
  }

  @Test
  void fetchFilesNothingFound() {
    var files = objectStoreAccess.getObjectsWithPrefix("THIS_PREFIX_DOES_NOT_EXIST");

    assertThat(files).withFailMessage("Found files, but should be empty!").isEmpty();
  }

  @Test
  void pushTestFileAndDelete() {
    LocalFile localFile = new LocalGenericFile(getExampleFile(), getRootTestFolder());
    String testFileTargetKey = testRunId + localFile.getS3Key();

    LocalFile localFileSpy = spy(localFile);
    when(localFileSpy.getS3Key()).thenReturn(testRunId + localFile.getS3Key());

    objectStoreAccess.putObject(localFileSpy);
    List<S3Object> files = objectStoreAccess.getObjectsWithPrefix(testRunId);
    assertThat(files).hasSize(1);

    assertThat(files.get(0).getObjectName()).isEqualTo(testFileTargetKey);

    objectStoreAccess.deleteObjectsWithPrefix(testRunId);

    List<S3Object> filesAfterDeletion = objectStoreAccess.getObjectsWithPrefix(testRunId);
    assertThat(filesAfterDeletion).isEmpty();
  }

  @Test
  void push1001TestFilesAndDelete() {
    int numberOfFiles = 1001;
    List<Pair<LocalFile, String>> localFilesSpyAndIds = IntStream.range(0, numberOfFiles)
        .mapToObj(i -> Pair.of(new LocalGenericFile(
            getExampleFile(),
            getRootTestFolder()), testRunBatchesPrefix + UUID.randomUUID().toString() + "/"))
        .map(pair -> {
          LocalFile spy = spy(pair.getLeft());
          when(spy.getS3Key()).thenReturn(pair.getRight() + pair.getLeft().getS3Key());
          return Pair.of(spy, pair.getRight());
        }).collect(Collectors.toList());
    localFilesSpyAndIds.forEach(p -> objectStoreAccess.putObject(p.getLeft()));
    final List<S3Object> s3Objects = localFilesSpyAndIds.stream()
        .map(p -> objectStoreAccess.getObjectsWithPrefix(p.getRight()))
        .flatMap(List::stream)
        .collect(Collectors.toList());
    assertThat(s3Objects).hasSize(numberOfFiles);
    objectStoreAccess.deleteObjectsWithPrefix(testRunBatchesPrefix);
    List<S3Object> filesAfterDeletion = objectStoreAccess.getObjectsWithPrefix(testRunBatchesPrefix);
    assertThat(filesAfterDeletion).isEmpty();

  }

  private Path getExampleFile() {
    try {
      return resourceLoader.getResource(textFile).getFile().toPath();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private Path getRootTestFolder() {
    try {
      return resourceLoader.getResource(rootTestFolder).getFile().toPath();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
