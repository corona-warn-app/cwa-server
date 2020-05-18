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
  private IndexDirectory<Integer> decoratee;
  private Directory decorator;

  @BeforeEach
  public void setup() throws IOException {
    outputFolder.create();
    outputFile = outputFolder.newFolder();
    parent = new DirectoryImpl(outputFile);
    decoratee = new IndexDirectoryImpl<>("foo", __ -> index, __ -> __);
    decorator = new IndexingDecorator<>(decoratee);

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
