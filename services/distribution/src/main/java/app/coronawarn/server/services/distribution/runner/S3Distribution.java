/*
 * Corona-Warn-App
 *
 * SAP SE and all other contributors /
 * copyright owners license this file to you under the Apache
 * License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package app.coronawarn.server.services.distribution.runner;

import app.coronawarn.server.services.distribution.assembly.component.OutputDirectoryProvider;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.objectstore.ObjectStoreAccess;
import app.coronawarn.server.services.distribution.objectstore.S3Object;
import app.coronawarn.server.services.distribution.objectstore.S3Publisher;
import io.minio.errors.MinioException;
import java.io.IOException;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * This runner will sync the base working directory to the S3.
 */
@Component
@Order(3)
public class S3Distribution implements ApplicationRunner {

  private static final Logger logger = LoggerFactory.getLogger(S3Distribution.class);

  private final OutputDirectoryProvider outputDirectoryProvider;

  private final ObjectStoreAccess objectStoreAccess;

  private final DistributionServiceConfig distributionServiceConfig;

  /**
   * Creates an S3Distribution object, which will upload and remove files from the S3-compatible storage.
   *
   * @param outputDirectoryProvider   the outputDirectoryProvider.
   * @param objectStoreAccess         the objectStoreAccess for the S3Distribution.
   * @param distributionServiceConfig the distributionServiceConfig, used to retrieve the retention days.
   */
  public S3Distribution(OutputDirectoryProvider outputDirectoryProvider, ObjectStoreAccess objectStoreAccess,
      DistributionServiceConfig distributionServiceConfig) {
    this.outputDirectoryProvider = outputDirectoryProvider;
    this.objectStoreAccess = objectStoreAccess;
    this.distributionServiceConfig = distributionServiceConfig;
  }

  @Override
  public void run(ApplicationArguments args) {
    try {
      Path pathToDistribute = outputDirectoryProvider.getFileOnDisk().toPath().toAbsolutePath();
      S3Publisher s3Publisher = new S3Publisher(pathToDistribute, objectStoreAccess);

      s3Publisher.publish();
      logger.info("Data pushed to CDN successfully.");
    } catch (UnsupportedOperationException | GeneralSecurityException | MinioException | IOException e) {
      logger.error("Distribution failed.", e);
    }
  }

  /**
   * Deletes all diagnosis-key files from S3 that are older than retentionDays.
   *
   * @param retentionDays the number of days, that files should be retained on S3.
   */
  public void applyRetentionPolicy(int retentionDays) throws MinioException, GeneralSecurityException, IOException {
    List<S3Object> diagnosisKeysObjects = this.objectStoreAccess.getObjectsWithPrefix("version/v1/"
        + distributionServiceConfig.getApi().getDiagnosisKeysPath() + "/"
        + distributionServiceConfig.getApi().getCountryPath() + "/"
        + distributionServiceConfig.getApi().getCountryGermany() + "/"
        + distributionServiceConfig.getApi().getDatePath() + "/");
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
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}