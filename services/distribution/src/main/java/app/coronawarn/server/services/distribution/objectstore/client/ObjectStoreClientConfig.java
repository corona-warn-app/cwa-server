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

package app.coronawarn.server.services.distribution.objectstore.client;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.ObjectStore;
import java.net.URI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * Manages the instantiation of the {@link ObjectStoreClient} bean.
 */
@Configuration
public class ObjectStoreClientConfig {

  private static final String DEFAULT_REGION = "eu-west-1";

  @Bean
  public ObjectStoreClient createObjectStoreClient(DistributionServiceConfig distributionServiceConfig) {
    return createClient(distributionServiceConfig.getObjectStore());
  }

  private ObjectStoreClient createClient(ObjectStore objectStore) {
    return new S3ClientWrapper(S3Client.builder()
        .region(Region.of(DEFAULT_REGION))
        .endpointOverride(URI.create(objectStore.getEndpoint() + ":" + objectStore.getPort()))
        .credentialsProvider(new CredentialsProvider(objectStore.getAccessKey(), objectStore.getSecretKey()))
        .build());
  }

  /**
   * Statically serves credentials based on construction arguments.
   */
  static class CredentialsProvider implements AwsCredentialsProvider {

    final String accessKeyId;
    final String secretAccessKey;

    public CredentialsProvider(String accessKeyId, String secretAccessKey) {
      this.accessKeyId = accessKeyId;
      this.secretAccessKey = secretAccessKey;
    }

    @Override
    public AwsCredentials resolveCredentials() {
      return new AwsCredentials() {
        @Override
        public String accessKeyId() {
          return accessKeyId;
        }

        @Override
        public String secretAccessKey() {
          return secretAccessKey;
        }
      };
    }
  }
}
