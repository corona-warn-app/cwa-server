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

package app.coronawarn.server.services.distribution.assembly.structure.file;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryOnDisk;
import java.io.IOException;
import java.nio.file.Files;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.rules.TemporaryFolder;

class FileTest {

  private final byte[] bytes = "World".getBytes();
  private FileOnDisk file;

  @Rule
  private TemporaryFolder outputFolder = new TemporaryFolder();

  @BeforeEach
  public void setup() throws IOException {
    outputFolder.create();
    file = new FileOnDisk("Hello", bytes);
  }

  @Test
  void checkGetBytes() {
    assertThat(file.getBytes()).isEqualTo(bytes);
  }

  @Test
  void checkSetBytes() {
    byte[] bytes = "Goodbye".getBytes();
    file.setBytes(bytes);
    assertThat(file.getBytes()).isEqualTo(bytes);
  }

  @Test
  void checkWriteThrowsWithoutParent() {
    assertThat(catchThrowable(file::write)).isInstanceOf(NullPointerException.class);
  }

  @Test
  void checkWrite() throws IOException {
    java.io.File outputFile = outputFolder.newFolder();
    Directory<WritableOnDisk> directory = new DirectoryOnDisk(outputFile);

    directory.addWritable(file);
    directory.write();

    byte[] writtenBytes = Files.readAllBytes(file.getFileOnDisk().toPath());

    assertThat(writtenBytes).isEqualTo(bytes);
    assertThat(outputFile.listFiles()).isNotNull().hasSize(1);
  }

}
