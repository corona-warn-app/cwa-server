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
import io.minio.MinioClient;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Manages the instantiation of the {@link MinioClient} bean.
 */
@Configuration
public class ObjectStoreClientConfig {

  private static final String DEFAULT_REGION = "eu-west-1";

  @Bean
  public ObjectStoreClient createObjectStoreClient(DistributionServiceConfig distributionServiceConfig)
      throws InvalidPortException, InvalidEndpointException {
    return createClient(distributionServiceConfig.getObjectStore());
  }

  private MinioClientWrapper createClient(ObjectStore objectStore)
      throws InvalidPortException, InvalidEndpointException {
    if (isSsl(objectStore)) {
      return new MinioClientWrapper(new MinioClient(
          objectStore.getEndpoint(),
          objectStore.getPort(),
          objectStore.getAccessKey(), objectStore.getSecretKey(),
          DEFAULT_REGION,
          true));
    } else {
      return new MinioClientWrapper(new MinioClient(
          objectStore.getEndpoint(),
          objectStore.getPort(),
          objectStore.getAccessKey(), objectStore.getSecretKey()));
    }
  }

  private boolean isSsl(ObjectStore objectStore) {
    return objectStore.getEndpoint().startsWith("https://");
  }
}
