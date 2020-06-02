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
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * Manages the instantiation of the {@link ObjectStoreClient} bean.
 */
@Configuration
public class ObjectStoreClientConfig {

  private static final Region DEFAULT_REGION = Region.EU_CENTRAL_1;

  @Bean
  public ObjectStoreClient createObjectStoreClient(DistributionServiceConfig distributionServiceConfig) {
    return createClient(distributionServiceConfig.getObjectStore());
  }

  private ObjectStoreClient createClient(ObjectStore objectStore) {
    AwsCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(
        AwsBasicCredentials.create(objectStore.getAccessKey(), objectStore.getSecretKey()));
    String endpoint = removeTrailingSlash(objectStore.getEndpoint()) + ":" + objectStore.getPort();

    return new S3ClientWrapper(S3Client.builder()
        .region(DEFAULT_REGION)
        .endpointOverride(URI.create(endpoint))
        .credentialsProvider(credentialsProvider)
        .build());
  }

  private String removeTrailingSlash(String string) {
    return string.endsWith("/") ? string.substring(0, string.length() - 1) : string;
  }
}
