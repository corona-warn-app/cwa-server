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
import static java.io.File.separator;
import static java.lang.String.join;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.list;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.service.common.KeySharingPoliciesChecker;
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
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
@ContextConfiguration(classes = {CryptoProvider.class, DistributionServiceConfig.class,
    KeySharingPoliciesChecker.class},
    initializers = ConfigFileApplicationContextInitializer.class)
class DiagnosisKeysDirectoryTest {

  @Autowired
  CryptoProvider cryptoProvider;

  @Autowired
  DistributionServiceConfig distributionServiceConfig;

  @Autowired
  KeySharingPoliciesChecker sharingPolicyChecker;

  @Rule
  private final TemporaryFolder outputFolder = new TemporaryFolder();

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
    buildDirectoryStructure(emptyList(), "DE");

    Set<String> expectedFiles = Set.of(
        join(separator, "diagnosis-keys", "country", "index"),
        join(separator, "diagnosis-keys", "country", "DE", "date", "index")
    );

    Set<String> actualFiles = getFilePaths(outputFile, outputFile.getAbsolutePath());

    assertThat(actualFiles).isEqualTo(amendWithChecksumFiles(expectedFiles));
  }

  @Test
  void checkBuildsTheCorrectDirectoryStructureForMultipleSupportedCountriesWhenNoKeys() {
    buildDirectoryStructure(emptyList(), "DE", "FR");

    Set<String> expectedFiles = Set.of(
        join(separator, "diagnosis-keys", "country", "index"),
        join(separator, "diagnosis-keys", "country", "DE", "date", "index"),
        join(separator, "diagnosis-keys", "country", "FR", "date", "index")
    );

    Set<String> actualFiles = getFilePaths(outputFile, outputFile.getAbsolutePath());

    assertThat(actualFiles).isEqualTo(amendWithChecksumFiles(expectedFiles));
  }

  @Test
  void checkBuildsTheCorrectDirectoryStructureForOneCountry() {
    List<DiagnosisKey> diagnosisKeys = buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 3, 0, 0), 5);

    buildDirectoryStructure(diagnosisKeys, "DE");

    Set<String> expectedFiles = Sets.newLinkedHashSet(join(separator, "diagnosis-keys", "country", "index"),
        join(separator, "diagnosis-keys", "country", "DE", "date", "index"));
    expectedFiles.addAll(generateExpectedDirectoryStructure("DE", "1970-01-03"));
    expectedFiles.addAll(generateExpectedDirectoryStructure("DE", "1970-01-04"));

    Set<String> actualFiles = getFilePaths(outputFile, outputFile.getAbsolutePath());

    assertThat(actualFiles).isEqualTo(amendWithChecksumFiles(expectedFiles));
  }

  @Test
  void checkBuildsTheCorrectDirectoryStructureForDifferentVisitedCountries() {
    Collection<DiagnosisKey> diagnosisKeysOfCountries =
        buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 3, 0, 0), 5, "FR", list("DE", "FR"),
            ReportType.CONFIRMED_CLINICAL_DIAGNOSIS, 1);

    buildDirectoryStructure(diagnosisKeysOfCountries, "DE", "FR", "DK");

    Set<String> expectedFiles = Sets.newLinkedHashSet(join(separator, "diagnosis-keys", "country", "index"),
        join(separator, "diagnosis-keys", "country", "DE", "date", "index"),
        join(separator, "diagnosis-keys", "country", "FR", "date", "index"),
        join(separator, "diagnosis-keys", "country", "DK", "date", "index"));
    expectedFiles.addAll(generateExpectedDirectoryStructure("DE", "1970-01-03"));
    expectedFiles.addAll(generateExpectedDirectoryStructure("DE", "1970-01-04"));
    expectedFiles.addAll(generateExpectedDirectoryStructure("FR", "1970-01-03"));
    expectedFiles.addAll(generateExpectedDirectoryStructure("FR", "1970-01-04"));

    Set<String> actualFiles = getFilePaths(outputFile, outputFile.getAbsolutePath());

    assertThat(actualFiles).isEqualTo(amendWithChecksumFiles(expectedFiles));
  }

  @Test
  void checkBuildsTheCorrectDirectoryStructureForTwoCountriesWithDifferentKeys() {
    Collection<DiagnosisKey> diagnosisKeysOfCountries =
        buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 3, 0, 0), 5, "FR", list("DE"),
            ReportType.CONFIRMED_CLINICAL_DIAGNOSIS, 1);
    diagnosisKeysOfCountries.addAll(buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 4, 0, 0), 5, "FR", list("FR"),
        ReportType.CONFIRMED_CLINICAL_DIAGNOSIS, 1));

    buildDirectoryStructure(diagnosisKeysOfCountries, "DE", "FR", "DK");

    Set<String> expectedFiles = Sets.newLinkedHashSet(join(separator, "diagnosis-keys", "country", "index"),
        join(separator, "diagnosis-keys", "country", "DE", "date", "index"),
        join(separator, "diagnosis-keys", "country", "FR", "date", "index"),
        join(separator, "diagnosis-keys", "country", "DK", "date", "index"));
    expectedFiles.addAll(generateExpectedDirectoryStructure("DE", "1970-01-03"));
    expectedFiles.addAll(generateExpectedDirectoryStructure("DE", "1970-01-04"));
    expectedFiles.addAll(generateExpectedDirectoryStructure("FR", "1970-01-04"));

    Set<String> actualFiles = getFilePaths(outputFile, outputFile.getAbsolutePath());

    assertThat(actualFiles).isEqualTo(amendWithChecksumFiles(expectedFiles));
  }

  @Test
  void checkBuildsTheCorrectDirectoryStructureForMultipleSupportedCountriesAndSingleVisitedCountry() {
    Collection<DiagnosisKey> diagnosisKeysOfCountries =
        buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 3, 0, 0), 5, "FR", list("FR"),
            ReportType.CONFIRMED_CLINICAL_DIAGNOSIS, 1);

    buildDirectoryStructure(diagnosisKeysOfCountries, "DE", "FR", "DK");

    Set<String> expectedFiles = Sets.newLinkedHashSet(join(separator, "diagnosis-keys", "country", "index"),
        join(separator, "diagnosis-keys", "country", "DE", "date", "index"),
        join(separator, "diagnosis-keys", "country", "FR", "date", "index"),
        join(separator, "diagnosis-keys", "country", "DK", "date", "index"));
    expectedFiles.addAll(generateExpectedDirectoryStructure("FR", "1970-01-03"));
    expectedFiles.addAll(generateExpectedDirectoryStructure("FR", "1970-01-04"));

    Set<String> actualFiles = getFilePaths(outputFile, outputFile.getAbsolutePath());

    assertThat(actualFiles).isEqualTo(amendWithChecksumFiles(expectedFiles));
  }

  private void buildDirectoryStructure(Collection<DiagnosisKey> keyseparator, String... supportedCountries) {
    DistributionServiceConfig serviceConfigSpy = spy(distributionServiceConfig);
    when(serviceConfigSpy.getSupportedCountries()).thenReturn(supportedCountries);

    DiagnosisKeyBundler bundler = new ProdDiagnosisKeyBundler(serviceConfigSpy, sharingPolicyChecker);
    bundler.setDiagnosisKeys(keyseparator, LocalDateTime.of(1970, 1, 5, 0, 0));

    Directory<WritableOnDisk> directory = new DiagnosisKeysDirectory(bundler, cryptoProvider, serviceConfigSpy);
    parentDirectory.addWritable(directory);
    directory.prepare(new ImmutableStack<>());
    directory.write();
  }

  private Set<String> amendWithChecksumFiles(Set<String> expectedFiles) {
    Set<String> allExpectedFiles = new HashSet<>(expectedFiles);

    var checksumFiles = expectedFiles.stream()
        .map(file -> file + FileOnDiskWithChecksum.CHECKSUM_FILE_SUFFIX)
        .collect(Collectors.toSet());
    allExpectedFiles.addAll(checksumFiles);

    return allExpectedFiles;
  }

  private Set<String> generateExpectedDirectoryStructure(String country, String date) {
    Set<String> directoryStructure = IntStream.range(0, 24).mapToObj(Integer::toString)
        .map(hour -> join(separator, "diagnosis-keys", "country", country, "date", date, "hour", hour, "index"))
        .collect(Collectors.toSet());
    directoryStructure.add(join(separator, "diagnosis-keys", "country", country, "date", date, "index"));
    directoryStructure.add(join(separator, "diagnosis-keys", "country", country, "date", date, "hour", "index"));
    return directoryStructure;
  }
}
