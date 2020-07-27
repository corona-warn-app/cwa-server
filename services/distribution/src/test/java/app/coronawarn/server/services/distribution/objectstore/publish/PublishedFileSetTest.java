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

package app.coronawarn.server.services.distribution.objectstore.publish;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import app.coronawarn.server.services.distribution.objectstore.client.S3Object;

class PublishedFileSetTest {

  @ParameterizedTest
  @ValueSource(strings = {
      "version/v1/diagnosis-keys/country/DE/date/2020-01-01",
      "version/v1/diagnosis-keys/country/DE/date/2020-06-11/hour/0",
      "version/v1/diagnosis-keys/country/DE/date/2020-06-11/hour/23"})
  void testShouldNotPublishWithoutForceUpdateConfiguration(String key) {
    List<S3Object> s3Objects = List.of(new S3Object(key, "1234"));
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
