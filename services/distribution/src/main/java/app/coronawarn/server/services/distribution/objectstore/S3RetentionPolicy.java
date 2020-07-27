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

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.Api;
import app.coronawarn.server.services.distribution.objectstore.client.ObjectStoreOperationFailedException;
import app.coronawarn.server.services.distribution.objectstore.client.S3Object;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * Creates an S3RetentionPolicy object, which applies the retention policy to the S3 compatible storage.
 */
@Component
public class S3RetentionPolicy {

  private final ObjectStoreAccess objectStoreAccess;
  private final Api api;
  private final FailedObjectStoreOperationsCounter failedObjectStoreOperationsCounter;

  /**
   * Creates an {@link S3RetentionPolicy} instance with the specified parameters.
   */
  public S3RetentionPolicy(ObjectStoreAccess objectStoreAccess, DistributionServiceConfig distributionServiceConfig,
      FailedObjectStoreOperationsCounter failedOperationsCounter) {
    this.objectStoreAccess = objectStoreAccess;
    this.api = distributionServiceConfig.getApi();
    this.failedObjectStoreOperationsCounter = failedOperationsCounter;
  }

  /**
   * Deletes all diagnosis-key files from S3 that are older than retentionDays.
   *
   * @param retentionDays the number of days, that files should be retained on S3.
   */
  public void applyRetentionPolicy(int retentionDays) {
    List<S3Object> diagnosisKeysObjects = objectStoreAccess.getObjectsWithPrefix("version/v1/"
        + api.getDiagnosisKeysPath() + "/"
        + api.getCountryPath() + "/"
        + api.getCountryGermany() + "/"
        + api.getDatePath() + "/");
    final String regex = ".*([0-9]{4}-[0-9]{2}-[0-9]{2}).*";
    final Pattern pattern = Pattern.compile(regex);

    final LocalDate cutOffDate = LocalDate.now(ZoneOffset.UTC).minusDays(retentionDays);

    diagnosisKeysObjects.stream()
        .filter(diagnosisKeysObject -> {
          Matcher matcher = pattern.matcher(diagnosisKeysObject.getObjectName());
          return matcher.matches() && LocalDate.parse(matcher.group(1), DateTimeFormatter.ISO_LOCAL_DATE)
              .isBefore(cutOffDate);
        })
        .forEach(this::deleteDiagnosisKey);
  }

  /**
   * Java stream do not support checked exceptions within streams. This helper method rethrows them as unchecked
   * expressions, so they can be passed up to the Retention Policy.
   *
   * @param diagnosisKey the  diagnosis key, that should be deleted.
   */
  public void deleteDiagnosisKey(S3Object diagnosisKey) {
    try {
      objectStoreAccess.deleteObjectsWithPrefix(diagnosisKey.getObjectName());
    } catch (ObjectStoreOperationFailedException e) {
      failedObjectStoreOperationsCounter.incrementAndCheckThreshold(e);
    }
  }
}
