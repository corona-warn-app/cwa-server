package app.coronawarn.server.services.distribution.structure.directory;

import static app.coronawarn.server.services.distribution.common.Helpers.prepareAndWrite;
import static org.junit.jupiter.api.Assertions.assertEquals;

import app.coronawarn.server.services.distribution.structure.Writable;
import app.coronawarn.server.services.distribution.structure.file.File;
import app.coronawarn.server.services.distribution.structure.file.FileImpl;
import app.coronawarn.server.services.distribution.structure.functional.Formatter;
import app.coronawarn.server.services.distribution.structure.functional.IndexFunction;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class IndexDirectoryTest {

  private static final String name = "Numbers";
  private static final List<Integer> index = List.of(0, 1, 2);
  private static final IndexFunction<Integer> indexFunction = __ -> index;
  private static final Formatter<Integer> indexFormatter = Integer::valueOf;
  private IndexDirectory<Integer> indexDirectory;
  private Directory outputDirectory;
  private java.io.File outputFile;

  @TempDir
  Path tempPath;

  @BeforeEach
  public void setup() throws IOException {
    outputFile = tempPath.toFile();

    indexDirectory = new IndexDirectoryImpl<>(name, indexFunction, indexFormatter);
    outputDirectory = new DirectoryImpl(outputFile);
    outputDirectory.addDirectory(indexDirectory);
  }

  @Test
  public void checkGetIndex() {
    assertEquals(index, indexDirectory.getIndex(new Stack<>()));
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
        .map(java.io.File::listFiles)
        .flatMap(Arrays::stream)
        .map(java.io.File::listFiles)
        .flatMap(Arrays::stream)
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
    List<Directory> expectedDirectoryList = new ArrayList<>();
    indexDirectory.addDirectoryToAll(__ -> {
      Directory newDirectory = new DirectoryImpl("foo");
      expectedDirectoryList.add(newDirectory);
      return newDirectory;
    });

    prepareAndWrite(outputDirectory);

    java.io.File actualIndexDirectoryFile = Objects.requireNonNull(outputFile.listFiles())[0];
    List<java.io.File> actualPhysicalFiles = Stream.of(actualIndexDirectoryFile)
        .map(java.io.File::listFiles)
        .flatMap(Arrays::stream)
        .map(java.io.File::listFiles)
        .flatMap(Arrays::stream)
        .sorted()
        .collect(Collectors.toList());
    List<java.io.File> expectedPhysicalFiles = expectedDirectoryList.stream()
        .map(Writable::getFileOnDisk)
        .sorted()
        .collect(Collectors.toList());

    assertEquals(expectedPhysicalFiles, actualPhysicalFiles);
  }
}
