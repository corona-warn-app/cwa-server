package app.coronawarn.server.services.distribution.assembly.structure.directory.decorators;

import static app.coronawarn.server.services.distribution.common.Helpers.prepareAndWrite;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryImpl;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectoryImpl;
import app.coronawarn.server.services.distribution.assembly.structure.directory.decorator.IndexingDecorator;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.rules.TemporaryFolder;

public class IndexingDecoratorTest {

  @Rule
  private TemporaryFolder outputFolder = new TemporaryFolder();

  private static final Set<Integer> index = Set.of(1, 2, 3);
  private java.io.File outputFile;
  private Directory parent;
  private IndexDirectory<Integer> decoree;
  private Directory decorator;

  @BeforeEach
  public void setup() throws IOException {
    outputFolder.create();
    outputFile = outputFolder.newFolder();
    parent = new DirectoryImpl(outputFile);
    decoree = new IndexDirectoryImpl<>("foo", __ -> index, __ -> __);
    decorator = new IndexingDecorator<>(decoree);

    parent.addDirectory(decorator);

    prepareAndWrite(parent);
  }

  @Test
  public void checkWritesIndexFile() throws IOException, ParseException {
    java.io.File actualIndexDirectoryFile = Objects.requireNonNull(outputFile.listFiles())[0];
    java.io.File actualPhysicalFile = Stream.of(actualIndexDirectoryFile)
        .map(File::listFiles)
        .flatMap(Arrays::stream)
        .filter(File::isFile)
        .findFirst()
        .get();

    JSONParser jsonParser = new JSONParser();
    FileReader reader = new FileReader(actualPhysicalFile);
    Object obj = jsonParser.parse(reader);
    JSONArray indexJson = (JSONArray) obj;

    index.forEach(expected ->
        assertTrue(indexJson.contains(expected.longValue()), expected.toString()));
  }
}
