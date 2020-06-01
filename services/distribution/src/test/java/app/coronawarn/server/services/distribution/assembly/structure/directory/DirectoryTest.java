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

package app.coronawarn.server.services.distribution.assembly.structure.directory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.file.File;
import app.coronawarn.server.services.distribution.assembly.structure.file.FileOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import java.io.IOException;
import java.util.Set;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.rules.TemporaryFolder;

class DirectoryTest {

  private java.io.File outputDir = new java.io.File("test");
  private Directory<WritableOnDisk> parentDirectory;
  private File<WritableOnDisk> childFile;

  @Rule
  private TemporaryFolder outputFolder = new TemporaryFolder();

  @BeforeEach
  public void setup() throws IOException {
    outputFolder.create();
    outputDir = outputFolder.newFolder();
    parentDirectory = new DirectoryOnDisk(outputDir);
    childFile = new FileOnDisk("Child", new byte[0]);
  }

  @Test
  void checkWritablesInDirectory() {
    parentDirectory.addWritable(childFile);
    assertThat(parentDirectory.getWritables()).isEqualTo(Set.of(childFile));
  }

  @Test
  void checkParentOfWritablesInDirectory() {
    parentDirectory.addWritable(childFile);
    assertThat(childFile.getParent()).isEqualTo(parentDirectory);
  }

  @Test
  void checkPrepareDelegatesToWritables() {
    File<WritableOnDisk> spyChildFile = spy(childFile);
    ImmutableStack<Object> expectedStack = new ImmutableStack<>();

    parentDirectory.addWritable(spyChildFile);
    parentDirectory.prepare(expectedStack);

    verify(spyChildFile).prepare(expectedStack);
  }

  @Test
  void checkWriteThrowsWithoutParent() {
    assertThat(catchThrowable(new DirectoryOnDisk("")::write))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void checkWriteWritesOwnDirectory() {
    class MockFile extends java.io.File {

      public MockFile() {
        super(outputDir.getPath());
      }
    }

    java.io.File mockOutputDirectory = spy(new MockFile());
    parentDirectory = new DirectoryOnDisk(mockOutputDirectory);

    parentDirectory.write();
    verify(mockOutputDirectory).mkdirs();
  }

  @Test
  void checkWriteDelegatesToWritables() {
    File<WritableOnDisk> spyChildFile = spy(childFile);

    parentDirectory.addWritable(spyChildFile);
    parentDirectory.write();

    verify(spyChildFile).write();
  }
}
