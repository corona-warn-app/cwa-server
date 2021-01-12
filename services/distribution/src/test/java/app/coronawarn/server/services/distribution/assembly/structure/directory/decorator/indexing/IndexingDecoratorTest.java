package app.coronawarn.server.services.distribution.assembly.structure.directory.decorator.indexing;

import static app.coronawarn.server.services.distribution.common.Helpers.prepareAndWrite;
import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.file.FileOnDiskWithChecksum;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class IndexingDecoratorTest {

  private static final Set<Integer> INDEX = Set.of(1, 2, 3);
  @TempDir
  java.io.File outputFile;

  @BeforeEach
  public void setup() throws IOException {
    Directory<WritableOnDisk> parent = new DirectoryOnDisk(outputFile);
    IndexDirectory<Integer, WritableOnDisk> decoratee = new IndexDirectoryOnDisk<>("foo", ignoredValue -> INDEX,
        value -> value);
    IndexDirectory<Integer, WritableOnDisk> decorator = new IndexingDecoratorOnDisk<>(decoratee, "bar");

    parent.addWritable(decorator);

    prepareAndWrite(parent);
  }

  @Test
  void checkWritesIndexFile() throws IOException, ParseException {
    java.io.File actualIndexDirectoryFile = Objects.requireNonNull(outputFile.listFiles())[0];
    java.io.File actualPhysicalFile = Stream.of(actualIndexDirectoryFile).filter(File::isDirectory).map(File::listFiles)
        .flatMap(Arrays::stream).filter(File::isFile)
        .filter(file -> !FileOnDiskWithChecksum.isChecksumFile(file.toPath())).findFirst().get();

    JSONParser jsonParser = new JSONParser();
    try (FileReader reader = new FileReader(actualPhysicalFile)) {
      JSONArray indexJson = (JSONArray) jsonParser.parse(reader);
      INDEX.forEach(expected -> assertThat(indexJson.contains(expected.longValue()))
          .withFailMessage(expected.toString()).isTrue());
    }
  }

}
