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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.ObjectStore;
import app.coronawarn.server.services.distribution.objectstore.client.S3Object;
import java.time.LocalDate;
import java.time.ZoneOffset;
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
@ContextConfiguration(classes = {S3RetentionPolicy.class, ObjectStore.class},
    initializers = ConfigFileApplicationContextInitializer.class)
class S3RetentionPolicyTest {

  @MockBean
  private ObjectStoreAccess objectStoreAccess;

  @Autowired
  private S3RetentionPolicy s3RetentionPolicy;

  @Autowired DistributionServiceConfig distributionServiceConfig;

  @Test
  void shouldDeleteOldFiles() {
    String expectedFileToBeDeleted = generateFileName(LocalDate.now(ZoneOffset.UTC).minusDays(2));

    when(objectStoreAccess.getObjectsWithPrefix(any())).thenReturn(List.of(
        new S3Object(expectedFileToBeDeleted),
        new S3Object(generateFileName(LocalDate.now())),
        new S3Object("version/v1/configuration/country/DE/app_config")));

    s3RetentionPolicy.applyRetentionPolicy(1);

    verify(objectStoreAccess, atLeastOnce()).deleteObjectsWithPrefix(eq(expectedFileToBeDeleted));
  }

  @Test
  void shouldNotDeleteFilesIfAllAreValid() {
    when(objectStoreAccess.getObjectsWithPrefix(any())).thenReturn(List.of(
        new S3Object(generateFileName(LocalDate.now().minusDays(1))),
        new S3Object(generateFileName(LocalDate.now().plusDays(1))),
        new S3Object(generateFileName(LocalDate.now())),
        new S3Object("version/v1/configuration/country/DE/app_config")));

    s3RetentionPolicy.applyRetentionPolicy(1);

    verify(objectStoreAccess, never()).deleteObjectsWithPrefix(any());
  }

  private String generateFileName(LocalDate date) {
    var api = distributionServiceConfig.getApi();

    return "version/v1/" + api.getDiagnosisKeysPath() + "/"  + api.getCountryPath() + "/"
        + api.getCountryGermany() + "/" + api.getDatePath() + "/" + date.toString() + "/hour/0";
  }
}
