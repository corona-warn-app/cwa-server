/*-
 * ---license-start
 * Corona-Warn-App
 * ---
 * Copyright (C) 2020 SAP SE and all other contributors
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ---license-end
 */

package app.coronawarn.server.services.distribution.objectstore;

import static app.coronawarn.server.services.distribution.assembly.structure.util.TimeUtils.getUtcDate;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.util.Lists.list;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.ObjectStore;
import app.coronawarn.server.services.distribution.objectstore.client.ObjectStoreOperationFailedException;
import app.coronawarn.server.services.distribution.objectstore.client.S3Object;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@EnableConfigurationProperties(value = DistributionServiceConfig.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {S3RetentionPolicy.class, ObjectStore.class, FailedObjectStoreOperationsCounter.class},
    initializers = ConfigFileApplicationContextInitializer.class)
class S3RetentionPolicyTest {

  @MockBean
  private ObjectStoreAccess objectStoreAccess;

  @MockBean
  private FailedObjectStoreOperationsCounter failedObjectStoreOperationsCounter;

  @Autowired
  private S3RetentionPolicy s3RetentionPolicy;

  @Autowired
  private DistributionServiceConfig distributionServiceConfig;

  @Test
  void shouldDeleteOldFiles() {
    Collection<String> supportedCounties = asList(distributionServiceConfig.getSupportedCountries());

    Collection<String> expectedFilesToBeDeleted = supportedCounties.stream()
        .map(country -> generateFileName(getUtcDate().minusDays(2), country))
        .collect(toList());

    List<S3Object> mockResponse = list(new S3Object("version/v1/configuration/country/DE/app_config"));
    mockResponse.addAll(
        supportedCounties.stream().map(country -> new S3Object(generateFileName(getUtcDate(), country)))
            .collect(toList()));
    mockResponse.addAll(expectedFilesToBeDeleted.stream().map(S3Object::new).collect(toList()));

    when(objectStoreAccess.getObjectsWithPrefix(any())).thenReturn(mockResponse);

    s3RetentionPolicy.applyRetentionPolicy(1);

    expectedFilesToBeDeleted.forEach(expectedFileToBeDeleted ->
        verify(objectStoreAccess, atLeastOnce()).deleteObjectsWithPrefix(eq(expectedFileToBeDeleted)));
  }

  @Test
  void shouldNotDeleteFilesIfAllAreValid() {
    when(objectStoreAccess.getObjectsWithPrefix(any())).thenReturn(List.of(
        new S3Object(generateFileName(getUtcDate().minusDays(1))), // TODO specify country
        new S3Object(generateFileName(getUtcDate().plusDays(1))),
        new S3Object(generateFileName(getUtcDate())),
        new S3Object("version/v1/configuration/country/DE/app_config")));

    s3RetentionPolicy.applyRetentionPolicy(1);

    verify(objectStoreAccess, never()).deleteObjectsWithPrefix(any());
  }

  @Test
  void deleteDiagnosisKeysUpdatesFailedOperationCounter() {
    doThrow(ObjectStoreOperationFailedException.class).when(objectStoreAccess).deleteObjectsWithPrefix(any());

    s3RetentionPolicy.deleteDiagnosisKey(new S3Object("foo"));

    verify(failedObjectStoreOperationsCounter, times(1))
        .incrementAndCheckThreshold(any(ObjectStoreOperationFailedException.class));
  }

  private String generateFileName(LocalDate date, String country) {
    var api = distributionServiceConfig.getApi();

    return api.getVersionPath() + "/" + api.getVersionV1() + "/" + api.getDiagnosisKeysPath() + "/"
        + api.getCountryPath() + "/" + country + "/" + api.getDatePath() + "/" + date.toString() + "/"
        + api.getHourPath() + "/0";
  }
}
