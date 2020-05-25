/*
 * Corona-Warn-App
 *
 * SAP SE and all other contributors /
 * copyright owners license this file to you under the Apache
 * License, Version 2.0 (the "License"); you may not use this
 *  file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package app.coronawarn.server.services.distribution.assembly.component;

import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@EnableConfigurationProperties(value = DistributionServiceConfig.class)
@ExtendWith(SpringExtension.class)
@TestPropertySource(properties = "spring.config.location = classpath:master-index-test/config.yaml")
@ContextConfiguration(classes = {DistributionServiceConfig.class,OutputDirectoryProvider.class},
    initializers = ConfigFileApplicationContextInitializer.class)
class MasterIndexFileCreatorTest {

  private static final String TEST_BASE = "master-index-test/testout";

  private static String ACTUAL_PATH = TEST_BASE + "/version/v1/diagnosis-keys/index";

  @Autowired
  private DistributionServiceConfig distributionServiceConfig;

  @Autowired
  private OutputDirectoryProvider outputDirectoryProvider;

  @Autowired
  private ResourceLoader resourceLoader;

  @Test
  void test() throws IOException {
    var spy = Mockito.spy(outputDirectoryProvider);
    when(spy.getFileOnDisk()).thenReturn(getTestBase());

    var creator = new MasterIndexFileCreator(distributionServiceConfig, spy);

    creator.createIndex();

    var expected = Files.readAllLines(getExpected());
    var actual = Files.readAllLines(getActual());

    assertThat(actual.toString())
        .isEqualTo(expected.toString())
        .withFailMessage("Created file did not match expected file");
  }

  private File getTestBase() throws IOException {
    return resourceLoader.getResource("master-index-test/testout").getFile();
  }

  private Path getActual() throws IOException {
    return resourceLoader.getResource(ACTUAL_PATH).getFile().toPath();
  }

  private Path getExpected() throws IOException {
    return resourceLoader.getResource("master-index-test/expected_result.txt").getFile().toPath();
  }

}
