package app.coronawarn.server.services.distribution.statistics.file;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.objectstore.client.ObjectStoreClient;
import app.coronawarn.server.services.distribution.statistics.exceptions.BucketNotFoundException;
import app.coronawarn.server.services.distribution.statistics.exceptions.ConnectionException;
import app.coronawarn.server.services.distribution.statistics.exceptions.FilePathNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.retry.ExhaustedRetryException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.S3Exception;

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@EnableConfigurationProperties(value = {DistributionServiceConfig.class})
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RemoteStatisticJsonFileLoaderTest.class},
    initializers = ConfigFileApplicationContextInitializer.class)
public class RemoteStatisticJsonFileLoaderTest {

  @Autowired
  DistributionServiceConfig serviceConfig;

  @MockBean
  ObjectStoreClient mockS3client;

  @Test
  void shouldThrowBucketNotFound() {
    var mockException = mock(NoSuchBucketException.class);
    when(mockS3client.getSingleObjectContent(anyString(), anyString()))
        .thenThrow(new ExhaustedRetryException("", mockException));
    var loader = new RemoteStatisticJsonFileLoader(mockS3client, serviceConfig);
    assertThrows(BucketNotFoundException.class, loader::getContent);
  }

  @Test
  void shouldThrowConnectionException() {
    var mockException = mock(SdkClientException.class);
    when(mockS3client.getSingleObjectContent(anyString(), anyString()))
        .thenThrow(new ExhaustedRetryException("", mockException));
    var loader = new RemoteStatisticJsonFileLoader(mockS3client, serviceConfig);
    assertThrows(ConnectionException.class, loader::getContent);
  }

  @Test
  void shouldUnauthorizedFileException() {
    var mockException = mock(S3Exception.class);
    when(mockS3client.getSingleObjectContent(anyString(), anyString()))
        .thenThrow(new ExhaustedRetryException("", mockException));
    var loader = new RemoteStatisticJsonFileLoader(mockS3client, serviceConfig);
    assertThrows(FilePathNotFoundException.class, loader::getContent);
  }

}
