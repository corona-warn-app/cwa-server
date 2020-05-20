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

package app.coronawarn.server.services.distribution.assembly.structure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryImpl;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import java.io.File;
import org.junit.jupiter.api.Test;

public class WritableTest {

  private static class TestWritable extends WritableImpl {

    protected TestWritable(String name) {
      super(name);
    }

    @Override
    public void write() {
    }

    @Override
    public void prepare(ImmutableStack<Object> indices) {
    }
  }

  @Test
  public void checkGetName() {
    String name = "Test";
    Writable writable = new TestWritable(name);
    assertThat(writable.getName()).isEqualTo(name);
  }

  @Test
  public void checkGetAndSetParent() {
    Directory parent = new DirectoryImpl("Parent");
    Writable child = new TestWritable("Child");
    child.setParent(parent);
    assertThat(child.getParent()).isEqualTo(parent);
  }

  @Test
  public void checkGetFileOnDiskForRoot() {
    File file = new File("Root");
    Directory parent = new DirectoryImpl(file);
    assertThat(parent.getFileOnDisk()).isEqualTo(file);
  }

  @Test
  public void checkGetFileOnDiskRelativeToRoot() {
    File file = new File("Root");
    Directory parent = new DirectoryImpl(file);
    Writable child = new TestWritable("Child");
    child.setParent(parent);
    assertThat(child.getFileOnDisk()).isEqualTo(file.toPath().resolve("Child").toFile());
  }

  @Test
  public void checkGetFileOnDiskThrowsIfNoParent() {
    Directory orphan = new DirectoryImpl("Orphan");
    assertThat(catchThrowable(orphan::getFileOnDisk)).isInstanceOf(NullPointerException.class);
  }
}
