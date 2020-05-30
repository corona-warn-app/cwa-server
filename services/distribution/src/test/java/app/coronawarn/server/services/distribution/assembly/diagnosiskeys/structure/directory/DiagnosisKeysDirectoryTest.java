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

package app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.directory;

import static app.coronawarn.server.services.distribution.common.Helpers.buildDiagnosisKeyForSubmissionTimestamp;
import static java.lang.String.join;
import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.services.distribution.assembly.component.CryptoProvider;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.rules.TemporaryFolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@EnableConfigurationProperties(value = DistributionServiceConfig.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CryptoProvider.class, DistributionServiceConfig.class},
    initializers = ConfigFileApplicationContextInitializer.class)
class DiagnosisKeysDirectoryTest {

  @Autowired
  CryptoProvider cryptoProvider;

  @Autowired
  DistributionServiceConfig distributionServiceConfig;

  @Rule
  private TemporaryFolder outputFolder = new TemporaryFolder();

  private File outputFile;
  private Directory<WritableOnDisk> parentDirectory;

  List<DiagnosisKey> diagnosisKeys;

  @BeforeEach
  void setupAll() throws IOException {
    outputFolder.create();
    outputFile = outputFolder.newFolder();
    parentDirectory = new DirectoryOnDisk(outputFile);

    // 01.01.1970 - 00:00 UTC
    long startTimestamp = 0;

    // Generate diagnosis keys covering 30 hours of submission timestamps
    // Until 02.01.1970 - 06:00 UTC -> 1 full day + 6 hours
    diagnosisKeys = IntStream.range(0, 30)
        .mapToObj(
            currentHour -> buildDiagnosisKeyForSubmissionTimestamp(startTimestamp + currentHour))
        .collect(Collectors.toList());
  }

  @Test
  void checkBuildsTheCorrectDirectoryStructureWhenNoKeys() {
    diagnosisKeys = new ArrayList<>();
    Directory<WritableOnDisk> directory = new DiagnosisKeysDirectory(diagnosisKeys, cryptoProvider,
        distributionServiceConfig);
    parentDirectory.addWritable(directory);
    directory.prepare(new ImmutableStack<>());
    directory.write();

    String s = File.separator;
    Set<String> expectedFiles = Set.of(
        join(s, "diagnosis-keys", "country", "index"),
        join(s, "diagnosis-keys", "country", "DE", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "index")
    );

    Set<String> actualFiles = getActualFiles(outputFile);

    assertThat(actualFiles).isEqualTo(expectedFiles);
  }

  @Test
  void checkBuildsTheCorrectDirectoryStructure() {
    Directory<WritableOnDisk> directory = new DiagnosisKeysDirectory(diagnosisKeys, cryptoProvider,
        distributionServiceConfig);
    parentDirectory.addWritable(directory);
    directory.prepare(new ImmutableStack<>());
    directory.write();

    String s = File.separator;
    Set<String> expectedFiles = Set.of(
        join(s, "diagnosis-keys", "country", "index"),
        join(s, "diagnosis-keys", "country", "DE", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "1970-01-01", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "1970-01-01", "hour", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "1970-01-01", "hour", "0", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "1970-01-01", "hour", "1", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "1970-01-01", "hour", "2", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "1970-01-01", "hour", "3", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "1970-01-01", "hour", "4", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "1970-01-01", "hour", "5", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "1970-01-01", "hour", "6", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "1970-01-01", "hour", "7", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "1970-01-01", "hour", "8", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "1970-01-01", "hour", "9", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "1970-01-01", "hour", "10", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "1970-01-01", "hour", "11", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "1970-01-01", "hour", "12", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "1970-01-01", "hour", "13", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "1970-01-01", "hour", "14", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "1970-01-01", "hour", "15", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "1970-01-01", "hour", "16", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "1970-01-01", "hour", "17", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "1970-01-01", "hour", "18", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "1970-01-01", "hour", "19", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "1970-01-01", "hour", "20", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "1970-01-01", "hour", "21", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "1970-01-01", "hour", "22", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "1970-01-01", "hour", "23", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "1970-01-02", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "1970-01-02", "hour", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "1970-01-02", "hour", "0", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "1970-01-02", "hour", "1", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "1970-01-02", "hour", "2", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "1970-01-02", "hour", "3", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "1970-01-02", "hour", "4", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "1970-01-02", "hour", "5", "index")
    );

    Set<String> actualFiles = getActualFiles(outputFile);

    assertThat(actualFiles).isEqualTo(expectedFiles);
  }

  private Set<String> getActualFiles(java.io.File root) {
    Set<String> files = Arrays.stream(Objects.requireNonNull(root.listFiles()))
        .filter(File::isFile)
        .map(File::getAbsolutePath)
        .map(path -> path.substring(outputFile.getAbsolutePath().length() + 1))
        .collect(Collectors.toSet());

    Set<java.io.File> directories = Arrays.stream(Objects.requireNonNull(root.listFiles()))
        .filter(File::isDirectory)
        .collect(Collectors.toSet());

    Set<String> subFiles = directories.stream()
        .map(this::getActualFiles)
        .flatMap(Set::stream)
        .collect(Collectors.toSet());

    files.addAll(subFiles);
    return files;
  }
}
