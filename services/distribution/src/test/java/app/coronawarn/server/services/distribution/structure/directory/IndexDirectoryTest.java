package app.coronawarn.server.services.distribution.structure.directory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import app.coronawarn.server.services.distribution.structure.Writable;
import app.coronawarn.server.services.distribution.structure.file.File;
import app.coronawarn.server.services.distribution.structure.file.FileImpl;
import app.coronawarn.server.services.distribution.structure.functional.Formatter;
import app.coronawarn.server.services.distribution.structure.functional.IndexFunction;
import app.coronawarn.server.services.distribution.structure.util.ImmutableStack;
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

public class IndexDirectoryTest {

  private static final String name = "Numbers";
  private static final Set<Integer> index = Set.of(0, 1, 2);
  private static final IndexFunction<Integer> indexFunction = __ -> index;
  private static final Formatter<Integer> indexFormatter = Integer::valueOf;
  private IndexDirectory<Integer> indexDirectory;
  private Directory outputDirectory;

  @Rule
  private TemporaryFolder temporaryFolder = new TemporaryFolder();
  private java.io.File outputFile;

  @BeforeEach
  public void setup() throws IOException {
    temporaryFolder.create();
    outputFile = temporaryFolder.newFolder();

    indexDirectory = new IndexDirectoryImpl<>(name, indexFunction, indexFormatter);
    outputDirectory = new DirectoryImpl(outputFile);
    outputDirectory.addDirectory(indexDirectory);
  }

  @Test
  public void checkGetIndex() {
    assertEquals(index, indexDirectory.getIndex(new ImmutableStack<>()));
  }

  @Test
  public void checkGetIndexFormatter() {
    assertEquals(indexFormatter, indexDirectory.getIndexFormatter());
  }

  @Test
  public void checkAddFileToAll() {
    List<File> expectedFileList = new ArrayList<>();
    indexDirectory.addFileToAll(__ -> {
      File newFile = new FileImpl("index", new byte[0]);
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
        .map(Writable::getFileOnDisk)
        .sorted()
        .collect(Collectors.toList());

    assertEquals(expectedPhysicalFiles, actualPhysicalFiles);
  }

  @Test
  public void checkAddDirectoryToAll() {
    List<Directory> expectedFileList = new ArrayList<>();
    indexDirectory.addDirectoryToAll(__ -> {
      Directory newDirectory = new DirectoryImpl("something");
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
        .map(Writable::getFileOnDisk)
        .collect(Collectors.toSet());

    assertEquals(expectedPhysicalFiles, actualPhysicalFiles);
  }

  private static Stream<java.io.File> getContainedElements(java.io.File directory) {
    return Arrays.stream(directory.listFiles());
  }

  private void prepareAndWrite(Directory directory) {
    directory.prepare(new ImmutableStack<>());
    directory.write();
  }
}
