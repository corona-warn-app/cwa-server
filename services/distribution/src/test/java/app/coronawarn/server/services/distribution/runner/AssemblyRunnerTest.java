/*
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

package app.coronawarn.server.services.distribution.runner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.coronawarn.server.services.distribution.assembly.component.CwaApiStructureProvider;
import app.coronawarn.server.services.distribution.assembly.component.OutputDirectoryProvider;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryOnDisk;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.io.IOException;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.rules.TemporaryFolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@EnableConfigurationProperties(value = DistributionServiceConfig.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {Assembly.class}, initializers = ConfigFileApplicationContextInitializer.class)
class AssemblyRunnerTest {

  @MockBean
  OutputDirectoryProvider outputDirectoryProvider;

  @MockBean
  CwaApiStructureProvider cwaApiStructureProvider;

  @Autowired
  Assembly assembly;

  private Directory<WritableOnDisk> parentDirectory;
  private Directory<WritableOnDisk> childDirectory;

  @Rule
  private TemporaryFolder outputFolder = new TemporaryFolder();

  @BeforeEach
  void setup() throws IOException {
    outputFolder.create();
    var outputDirectory = outputFolder.newFolder("parent");
    var outputSubDirectory = outputFolder.newFolder("parent/child");
    parentDirectory = new DirectoryOnDisk(outputDirectory);
    childDirectory = new DirectoryOnDisk(outputSubDirectory);
  }

  @Test
  void shouldCorrectlyCreatePrepareAndWriteDirectories() throws IOException {
    Directory<WritableOnDisk> spyParentDirectory = spy(parentDirectory);

    when(outputDirectoryProvider.getDirectory()).thenReturn(spyParentDirectory);
    when(cwaApiStructureProvider.getDirectory()).thenReturn(childDirectory);

    assembly.run(null);

    verify(outputDirectoryProvider, times(1)).getDirectory();
    verify(outputDirectoryProvider, times(1)).clear();
    verify(cwaApiStructureProvider, times(1)).getDirectory();
    verify(spyParentDirectory, times(1)).prepare(any());
    verify(spyParentDirectory, times(1)).write();
  }
}
