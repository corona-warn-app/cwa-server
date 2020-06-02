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

import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.file.FileOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import app.coronawarn.server.services.distribution.assembly.structure.util.functional.Formatter;
import app.coronawarn.server.services.distribution.assembly.structure.util.functional.IndexFunction;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.rules.TemporaryFolder;

class IndexDirectoryTest {

  private static final String name = "Numbers";
  private static final Set<Integer> index = Set.of(0, 1, 2);
  private static final IndexFunction<Integer> indexFunction = __ -> index;
  private static final Formatter<Integer> indexFormatter = Integer::valueOf;
  private IndexDirectory<Integer, WritableOnDisk> indexDirectory;
  private Directory<WritableOnDisk> outputDirectory;

  @Rule
  private TemporaryFolder temporaryFolder = new TemporaryFolder();
  private java.io.File outputFile;

  @BeforeEach
  public void setup() throws IOException {
    temporaryFolder.create();
    outputFile = temporaryFolder.newFolder();

    indexDirectory = new IndexDirectoryOnDisk<>(name, indexFunction, indexFormatter);
    outputDirectory = new DirectoryOnDisk(outputFile);
    outputDirectory.addWritable(indexDirectory);
  }

  @Test
  void checkGetIndex() {
    assertThat(indexDirectory.getIndex(new ImmutableStack<>())).isEqualTo(index);
  }

  @Test
  void checkGetIndexFormatter() {
    assertThat(indexDirectory.getIndexFormatter()).isEqualTo(indexFormatter);
  }

  @Test
  void checkAddFileToAll() {
    List<FileOnDisk> expectedFileList = new ArrayList<>();
    indexDirectory.addWritableToAll(__ -> {
      FileOnDisk newFile = new FileOnDisk("index", new byte[0]);
      expectedFileList.add(newFile);
      return newFile;
    });

    prepareAndWrite(outputDirectory);

    java.io.File actualIndexDirectoryFile = Objects.requireNonNull(outputFile.listFiles())[0];
    List<java.io.File> actualPhysicalFiles = Stream.of(actualIndexDirectoryFile)
        .flatMap(IndexDirectoryTest::getContainedElements)
        .flatMap(IndexDirectoryTest::getContainedElements)
        .sorted()
        .collect(Collectors.toList());
    List<java.io.File> expectedPhysicalFiles = expectedFileList.stream()
        .map(WritableOnDisk::getFileOnDisk)
        .sorted()
        .collect(Collectors.toList());

    assertThat(actualPhysicalFiles).isEqualTo(expectedPhysicalFiles);
  }

  @Test
  void checkAddDirectoryToAll() {
    List<DirectoryOnDisk> expectedFileList = new ArrayList<>();
    indexDirectory.addWritableToAll(__ -> {
      DirectoryOnDisk newDirectory = new DirectoryOnDisk("something");
      expectedFileList.add(newDirectory);
      return newDirectory;
    });

    prepareAndWrite(outputDirectory);

    java.io.File actualIndexDirectoryFile = Objects.requireNonNull(outputFile.listFiles())[0];
    Set<java.io.File> actualPhysicalFiles = Stream.of(actualIndexDirectoryFile)
        .flatMap(IndexDirectoryTest::getContainedElements)
        .flatMap(IndexDirectoryTest::getContainedElements)
        .collect(Collectors.toSet());
    Set<java.io.File> expectedPhysicalFiles = expectedFileList.stream()
        .map(WritableOnDisk::getFileOnDisk)
        .collect(Collectors.toSet());

    assertThat(actualPhysicalFiles).isEqualTo(expectedPhysicalFiles);
  }

  private static Stream<java.io.File> getContainedElements(java.io.File directory) {
    return Arrays.stream(directory.listFiles());
  }

  private void prepareAndWrite(Directory<WritableOnDisk> directory) {
    directory.prepare(new ImmutableStack<>());
    directory.write();
  }
}
