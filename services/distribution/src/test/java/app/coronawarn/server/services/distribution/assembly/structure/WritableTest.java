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

package app.coronawarn.server.services.distribution.assembly.structure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import java.io.File;
import org.junit.jupiter.api.Test;

class WritableTest {

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
  void checkGetName() {
    String name = "Test";
    WritableOnDisk writable = new TestWritable(name);
    assertThat(writable.getName()).isEqualTo(name);
  }

  @Test
  void checkGetAndSetParent() {
    DirectoryOnDisk parent = new DirectoryOnDisk("Parent");
    WritableOnDisk child = new TestWritable("Child");
    child.setParent(parent);
    assertThat(child.getParent()).isEqualTo(parent);
  }

  @Test
  void checkGetFileOnDiskForRoot() {
    File file = new File("Root");
    DirectoryOnDisk parent = new DirectoryOnDisk(file);
    assertThat(parent.getFileOnDisk()).isEqualTo(file);
  }

  @Test
  void checkGetFileOnDiskRelativeToRoot() {
    File file = new File("Root");
    DirectoryOnDisk parent = new DirectoryOnDisk(file);
    WritableOnDisk child = new TestWritable("Child");
    child.setParent(parent);
    assertThat(child.getFileOnDisk()).isEqualTo(file.toPath().resolve("Child").toFile());
  }

  @Test
  void checkGetFileOnDiskThrowsIfNoParent() {
    assertThat(catchThrowable(new DirectoryOnDisk("Orphan")::getFileOnDisk))
        .isInstanceOf(NullPointerException.class);
  }
}
