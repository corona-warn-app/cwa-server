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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import java.io.File;
import org.junit.jupiter.api.Test;

public class WritableTest {

  private static class TestWritable extends WritableOnDisk {

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
    WritableOnDisk writable = new TestWritable(name);
    assertEquals(name, writable.getName());
  }

  @Test
  public void checkGetAndSetParent() {
    DirectoryOnDisk parent = new DirectoryOnDisk("Parent");
    WritableOnDisk child = new TestWritable("Child");
    child.setParent(parent);
    assertEquals(parent, child.getParent());
  }

  @Test
  public void checkGetFileOnDiskForRoot() {
    File file = new File("Root");
    DirectoryOnDisk parent = new DirectoryOnDisk(file);
    assertEquals(file, parent.getFileOnDisk());
  }

  @Test
  public void checkGetFileOnDiskRelativeToRoot() {
    File file = new File("Root");
    DirectoryOnDisk parent = new DirectoryOnDisk(file);
    WritableOnDisk child = new TestWritable("Child");
    child.setParent(parent);
    assertEquals(file.toPath().resolve("Child").toFile(), child.getFileOnDisk());
  }

  @Test
  public void checkGetFileOnDiskThrowsIfNoParent() {
    DirectoryOnDisk orphan = new DirectoryOnDisk("Orphan");
    assertThrows(NullPointerException.class, orphan::getFileOnDisk);
  }
}
