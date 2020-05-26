/*
 * Corona-Warn-App
 *
 * SAP SE and all other contributors /
 * copyright owners license this file to you under the Apache
 * License, Version 2.0 (the "License"); you may not use this
 *  file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package app.coronawarn.server.services.distribution.assembly.component;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * This service will create the master index file for all hourly package files present on this distribution.
 */
@Service
public class MasterIndexFileCreator {

  private static final Logger logger = LoggerFactory.getLogger(MasterIndexFileCreator.class);

  private static final Pattern HOUR_INDEX_PATTERN = Pattern.compile(".*/hour/\\d{1,2}/index$");

  private static final int DIRECTORY_SCANNING_MAX_DEPTH = 10;

  private static final String NEW_LINE_SEPARATOR = "\r\n";

  private final DistributionServiceConfig distributionServiceConfig;

  private final OutputDirectoryProvider outputDirectoryProvider;

  /**
   * Creates a new instance with the given configuration and directory provider.
   *
   * @param distributionServiceConfig the distribution config.
   * @param outputDirectoryProvider   the output directory provider.
   */
  public MasterIndexFileCreator(DistributionServiceConfig distributionServiceConfig,
      OutputDirectoryProvider outputDirectoryProvider) {
    this.distributionServiceConfig = distributionServiceConfig;
    this.outputDirectoryProvider = outputDirectoryProvider;
  }

  /**
   * Creates the master index file, and puts it in: version/_VERSION_/diagnosis-keys/index.
   *
   * @throws IOException in case the file could not be created
   */
  public void createIndex() throws IOException {
    Path versionDirectory = getVersionDirectory();

    try (Stream<Path> stream = Files.list(versionDirectory)) {
      stream
          .filter(Files::isDirectory)
          .forEach(this::createIndexForCountriesAtVersionFolder);
    }
  }

  private void createIndexForCountriesAtVersionFolder(Path versionFolder) {
    var countryFolder = versionFolder
        .resolve(distributionServiceConfig.getApi().getDiagnosisKeysPath())
        .resolve(distributionServiceConfig.getApi().getCountryPath());

    try (Stream<Path> stream = Files.list(countryFolder)) {
      stream.filter(Files::isDirectory).forEach(this::generateMainIndex);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private void generateMainIndex(Path path) {
    logger.info("Creating index for for {}", path.toAbsolutePath());

    try (Stream<Path> stream = Files.walk(path, DIRECTORY_SCANNING_MAX_DEPTH)) {
      String indexFileContent = stream
          .filter(Files::isRegularFile)
          .filter(MasterIndexFileCreator::isRelevantForMainIndex)
          .map(Path::getParent)
          .map(path::relativize)
          .map(Path::toString)
          .sorted()
          .collect(Collectors.joining(NEW_LINE_SEPARATOR));

      storeIndexFile(path, indexFileContent);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private void storeIndexFile(Path countryFolder, String content) {
    Path targetIndexFile = countryFolder.resolve(distributionServiceConfig.getApi().getDiagnosisKeysIndexPath());

    try {
      logger.debug("Storing index file on {}", targetIndexFile.toAbsolutePath());

      Files.writeString(targetIndexFile, content, StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static boolean isRelevantForMainIndex(Path path) {
    return HOUR_INDEX_PATTERN.matcher(path.toString()).matches();
  }

  private Path getVersionDirectory() {
    return this.outputDirectoryProvider.getFileOnDisk().toPath().resolve(CwaApiStructureProvider.VERSION_DIRECTORY);
  }

}
