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

package app.coronawarn.server.services.distribution.assembly.structure.directory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import app.coronawarn.server.services.distribution.assembly.structure.file.File;
import app.coronawarn.server.services.distribution.assembly.structure.file.FileImpl;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import java.io.IOException;
import java.util.Set;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.rules.TemporaryFolder;

public class DirectoryTest {

  private java.io.File outputDir = new java.io.File("test");
  private Directory parentDirectory;
  private Directory childDirectory;
  private File childFile;

  @Rule
  private TemporaryFolder outputFolder = new TemporaryFolder();

  @BeforeEach
  public void setup() throws IOException {
    outputFolder.create();
    outputDir = outputFolder.newFolder();
    parentDirectory = new DirectoryImpl(outputDir);
    childDirectory = new DirectoryImpl("Child");
    childFile = new FileImpl("Child", new byte[0]);
  }

  @Test
  public void checkFilesInDirectory() {
    parentDirectory.addFile(childFile);
    assertThat(parentDirectory.getFiles()).isEqualTo(Set.of(childFile));
  }

  @Test
  public void checkParentOfFilesInDirectory() {
    parentDirectory.addFile(childFile);
    assertThat(childFile.getParent()).isEqualTo(parentDirectory);
  }

  @Test
  public void checkDirectoriesInDirectory() {
    parentDirectory.addDirectory(childDirectory);
    assertThat(parentDirectory.getDirectories()).isEqualTo(Set.of(childDirectory));
  }

  @Test
  public void checkParentOfDirectoriesInDirectory() {
    parentDirectory.addDirectory(childDirectory);
    assertThat(childDirectory.getParent()).isEqualTo(parentDirectory);
  }

  @Test
  public void checkPrepareDelegatesToFiles() {
    File spyChildFile = spy(childFile);
    ImmutableStack<Object> expectedStack = new ImmutableStack<>();

    parentDirectory.addFile(spyChildFile);
    parentDirectory.prepare(expectedStack);

    verify(spyChildFile).prepare(expectedStack);
  }

  @Test
  public void checkPrepareDelegatesToDirectories() {
    Directory spyChildDirectory = spy(childDirectory);
    ImmutableStack<Object> expectedStack = new ImmutableStack<>();

    parentDirectory.addDirectory(spyChildDirectory);
    parentDirectory.prepare(expectedStack);

    verify(spyChildDirectory).prepare(expectedStack);
  }

  @Test
  public void checkWriteThrowsWithoutParent() {
    assertThat(catchThrowable(new DirectoryImpl("")::write))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  public void checkWriteWritesOwnDirectory() {
    class MockFile extends java.io.File {

      public MockFile() {
        super(outputDir.getPath());
      }
    }

    java.io.File mockOutputDirectory = spy(new MockFile());
    parentDirectory = new DirectoryImpl(mockOutputDirectory);

    parentDirectory.write();
    verify(mockOutputDirectory).mkdirs();
  }

  @Test
  public void checkWriteDelegatesToFiles() {
    File spyChildFile = spy(childFile);

    parentDirectory.addFile(spyChildFile);
    parentDirectory.write();

    verify(spyChildFile).write();
  }

  @Test
  public void checkWriteDelegatesToDirectories() {
    Directory spyChildDirectory = spy(childDirectory);

    parentDirectory.addDirectory(spyChildDirectory);
    parentDirectory.write();

    verify(spyChildDirectory).write();
  }
}
