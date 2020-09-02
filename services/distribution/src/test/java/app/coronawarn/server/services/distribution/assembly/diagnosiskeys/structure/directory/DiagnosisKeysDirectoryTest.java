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

import static app.coronawarn.server.services.distribution.common.Helpers.buildDiagnosisKeys;
import static app.coronawarn.server.services.distribution.common.Helpers.getFilePaths;
import static java.lang.String.join;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.list;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.services.distribution.assembly.component.CryptoProvider;
import app.coronawarn.server.services.distribution.assembly.diagnosiskeys.DiagnosisKeyBundler;
import app.coronawarn.server.services.distribution.assembly.diagnosiskeys.ProdDiagnosisKeyBundler;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.file.FileOnDiskWithChecksum;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.assertj.core.util.Sets;
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

  @BeforeEach
  void setupAll() throws IOException {
    outputFolder.create();
    outputFile = outputFolder.newFolder();
    parentDirectory = new DirectoryOnDisk(outputFile);
  }

  @Test
  void checkBuildsTheCorrectDirectoryStructureForOneCountryWhenNoKeys() {
    distributionServiceConfig.setSupportedCountries("DE");
    DiagnosisKeyBundler bundler = new ProdDiagnosisKeyBundler(distributionServiceConfig);
    Directory<WritableOnDisk> directory = new DiagnosisKeysDirectory(bundler, cryptoProvider,
        distributionServiceConfig);
    bundler.setDiagnosisKeys(Collections.emptyList(), LocalDateTime.of(1970, 1, 5, 0, 0));
    parentDirectory.addWritable(directory);
    directory.prepare(new ImmutableStack<>());
    directory.write();

    String s = File.separator;
    Set<String> expectedFiles = Set.of(
        join(s, "diagnosis-keys", "country", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "index")
    );

    Set<String> actualFiles = getFilePaths(outputFile, outputFile.getAbsolutePath());

    assertThat(actualFiles).isEqualTo(amendWithChecksumFiles(expectedFiles));
  }

  @Test
  void checkBuildsTheCorrectDirectoryStructureForMultipleSupportedCountriesWhenNoKeys() {
    distributionServiceConfig.setSupportedCountries("DE,FR");
    DiagnosisKeyBundler bundler = new ProdDiagnosisKeyBundler(distributionServiceConfig);
    Directory<WritableOnDisk> directory = new DiagnosisKeysDirectory(bundler, cryptoProvider,
        distributionServiceConfig);
    bundler.setDiagnosisKeys(Collections.emptyList(), LocalDateTime.of(1970, 1, 5, 0, 0));
    parentDirectory.addWritable(directory);
    directory.prepare(new ImmutableStack<>());
    directory.write();

    String s = File.separator;
    Set<String> expectedFiles = Set.of(
        join(s, "diagnosis-keys", "country", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "index"),
        join(s, "diagnosis-keys", "country", "FR", "date", "index")
    );

    Set<String> actualFiles = getFilePaths(outputFile, outputFile.getAbsolutePath());

    assertThat(actualFiles).isEqualTo(amendWithChecksumFiles(expectedFiles));
  }

  @Test
  void checkBuildsTheCorrectDirectoryStructureForOneCountry() {
    distributionServiceConfig.setSupportedCountries("DE");
    DiagnosisKeyBundler bundler = new ProdDiagnosisKeyBundler(distributionServiceConfig);
    List<DiagnosisKey> diagnosisKeys = buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 3, 0, 0), 5);
    bundler.setDiagnosisKeys(diagnosisKeys, LocalDateTime.of(1970, 1, 5, 0, 0));
    Directory<WritableOnDisk> directory = new DiagnosisKeysDirectory(bundler, cryptoProvider,
        distributionServiceConfig);
    parentDirectory.addWritable(directory);
    directory.prepare(new ImmutableStack<>());
    directory.write();

    String s = File.separator;
    Set<String> expectedFiles = Sets.newLinkedHashSet(join(s, "diagnosis-keys", "country", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "index"));
    expectedFiles.addAll(generateDirectoryStructureForDate("DE", "1970-01-03"));
    expectedFiles.addAll(generateDirectoryStructureForDate("DE", "1970-01-04"));

    Set<String> actualFiles = getFilePaths(outputFile, outputFile.getAbsolutePath());

    assertThat(actualFiles).isEqualTo(amendWithChecksumFiles(expectedFiles));
  }

  @Test
  void checkBuildsTheCorrectDirectoryStructureForDifferentVisitedCountries() {
    distributionServiceConfig.setSupportedCountries("DE,FR,DK");
    DiagnosisKeyBundler bundler = new ProdDiagnosisKeyBundler(distributionServiceConfig);

    Collection<DiagnosisKey> diagnosisKeysOfCountries = buildDiagnosisKeys(6,
        LocalDateTime.of(1970, 1, 3, 0, 0), 5, "FR",
        list("DE", "FR"));

    bundler.setDiagnosisKeys(diagnosisKeysOfCountries, LocalDateTime.of(1970, 1, 5, 0, 0));
    Directory<WritableOnDisk> directory = new DiagnosisKeysDirectory(bundler, cryptoProvider,
        distributionServiceConfig);
    parentDirectory.addWritable(directory);
    directory.prepare(new ImmutableStack<>());
    directory.write();

    String s = File.separator;
    Set<String> expectedFiles = Sets.newLinkedHashSet(join(s, "diagnosis-keys", "country", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "index"),
        join(s, "diagnosis-keys", "country", "FR", "date", "index"),
        join(s, "diagnosis-keys", "country", "DK", "date", "index"));
    expectedFiles.addAll(generateDirectoryStructureForDate("DE", "1970-01-03"));
    expectedFiles.addAll(generateDirectoryStructureForDate("DE", "1970-01-04"));
    expectedFiles.addAll(generateDirectoryStructureForDate("FR", "1970-01-03"));
    expectedFiles.addAll(generateDirectoryStructureForDate("FR", "1970-01-04"));

    Set<String> actualFiles = getFilePaths(outputFile, outputFile.getAbsolutePath());

    assertThat(actualFiles).isEqualTo(amendWithChecksumFiles(expectedFiles));
  }

  @Test
  void checkBuildsTheCorrectDirectoryStructure() {
    distributionServiceConfig.setSupportedCountries("DE,FR,DK");
    DiagnosisKeyBundler bundler = new ProdDiagnosisKeyBundler(distributionServiceConfig);

    Collection<DiagnosisKey> diagnosisKeysOfCountries = buildDiagnosisKeys(6,
        LocalDateTime.of(1970, 1, 3, 0, 0), 5, "FR",
        list("DE"));
    diagnosisKeysOfCountries.addAll(buildDiagnosisKeys(6,
        LocalDateTime.of(1970, 1, 4, 0, 0), 5, "FR",
        list("FR")));

    bundler.setDiagnosisKeys(diagnosisKeysOfCountries, LocalDateTime.of(1970, 1, 5, 0, 0));
    Directory<WritableOnDisk> directory = new DiagnosisKeysDirectory(bundler, cryptoProvider,
        distributionServiceConfig);
    parentDirectory.addWritable(directory);
    directory.prepare(new ImmutableStack<>());
    directory.write();

    String s = File.separator;
    Set<String> expectedFiles = Sets.newLinkedHashSet(join(s, "diagnosis-keys", "country", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "index"),
        join(s, "diagnosis-keys", "country", "FR", "date", "index"),
        join(s, "diagnosis-keys", "country", "DK", "date", "index"));
    expectedFiles.addAll(generateDirectoryStructureForDate("DE", "1970-01-03"));
    expectedFiles.addAll(generateDirectoryStructureForDate("DE", "1970-01-04"));
    expectedFiles.addAll(generateDirectoryStructureForDate("FR", "1970-01-04"));

    Set<String> actualFiles = getFilePaths(outputFile, outputFile.getAbsolutePath());

    assertThat(actualFiles).isEqualTo(amendWithChecksumFiles(expectedFiles));
  }

  @Test
  void checkBuildsTheCorrectDirectoryStructureForMultipleSupportedCountriesAndSingleVisitedCountry() {
    distributionServiceConfig.setSupportedCountries("DE,FR,DK");
    DiagnosisKeyBundler bundler = new ProdDiagnosisKeyBundler(distributionServiceConfig);

    Collection<DiagnosisKey> diagnosisKeysOfCountries = buildDiagnosisKeys(6,
        LocalDateTime.of(1970, 1, 3, 0, 0), 5, "FR",
        list("FR"));

    bundler.setDiagnosisKeys(diagnosisKeysOfCountries, LocalDateTime.of(1970, 1, 5, 0, 0));
    Directory<WritableOnDisk> directory = new DiagnosisKeysDirectory(bundler, cryptoProvider,
        distributionServiceConfig);
    parentDirectory.addWritable(directory);
    directory.prepare(new ImmutableStack<>());
    directory.write();

    String s = File.separator;
    Set<String> expectedFiles = Sets.newLinkedHashSet(join(s, "diagnosis-keys", "country", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "index"),
        join(s, "diagnosis-keys", "country", "FR", "date", "index"),
        join(s, "diagnosis-keys", "country", "DK", "date", "index"));
    expectedFiles.addAll(generateDirectoryStructureForDate("FR", "1970-01-03"));
    expectedFiles.addAll(generateDirectoryStructureForDate("FR", "1970-01-04"));

    Set<String> actualFiles = getFilePaths(outputFile, outputFile.getAbsolutePath());

    assertThat(actualFiles).isEqualTo(amendWithChecksumFiles(expectedFiles));
  }

  private Set<String> amendWithChecksumFiles(Set<String> expectedFiles) {
    Set<String> allExpectedFiles = new HashSet<>(expectedFiles);

    var checksumFiles = expectedFiles.stream()
        .map(file -> file + FileOnDiskWithChecksum.CHECKSUM_FILE_SUFFIX)
        .collect(Collectors.toSet());
    allExpectedFiles.addAll(checksumFiles);

    return allExpectedFiles;
  }

  private Set<String> generateDirectoryStructureForDate(String country, String date) {
    String s = File.separator;
    Set<String> directoryStructure = IntStream.range(0, 24).mapToObj(Integer::toString)
        .map(hour -> join(s, "diagnosis-keys", "country", country, "date", date, "hour", hour, "index"))
        .collect(Collectors.toSet());
    directoryStructure.add(join(s, "diagnosis-keys", "country", country, "date", date, "index"));
    directoryStructure.add(join(s, "diagnosis-keys", "country", country, "date", date, "hour", "index"));
    return directoryStructure;
  }
}
