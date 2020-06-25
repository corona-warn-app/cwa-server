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

import app.coronawarn.server.services.distribution.assembly.component.CryptoProvider;
import app.coronawarn.server.services.distribution.assembly.diagnosiskeys.DiagnosisKeyBundler;
import app.coronawarn.server.services.distribution.assembly.diagnosiskeys.ProdDiagnosisKeyBundler;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import app.coronawarn.server.services.distribution.common.Helpers;
import app.coronawarn.server.services.distribution.common.Helpers.TestTuple;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
class DiagnosisKeysDateDirectoryTest {

  @Rule
  private final TemporaryFolder outputFolder = new TemporaryFolder();

  @Autowired
  CryptoProvider cryptoProvider;

  @Autowired
  DistributionServiceConfig distributionServiceConfig;

  private File outputFile;

  private static Stream<Arguments> createDiagnosisKeysAndExpectedFiles() {
    return Stream.of(
        //Creates correct structure for multiple dates
        new TestTuple(IntStream.range(0, 3)
            .mapToObj(currentDate -> IntStream.range(0, 5)
                .mapToObj(currentHour ->
                    buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 3 + currentDate, 0, 0).plusHours(currentHour), 5))
                .flatMap(List::stream)
                .collect(Collectors.toList()))
            .flatMap(List::stream)
            .collect(Collectors.toList()),
            Helpers.getExpectedDateAndHourFiles(Map.of(
                "1970-01-03", List.of("0", "1", "2", "3", "4"),
                "1970-01-04", List.of("0", "1", "2", "3", "4"),
                "1970-01-05", List.of("0", "1", "2", "3", "4")), "1970-01-06"),
            LocalDateTime.of(1970, 1, 6, 0, 0)
        ),
        //Does not include the current date
        new TestTuple(IntStream.range(0, 3)
            .mapToObj(currentDate -> IntStream.range(0, 5)
                .mapToObj(currentHour ->
                    buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 3 + currentDate, 0, 0).plusHours(currentHour), 5))
                .flatMap(List::stream)
                .collect(Collectors.toList()))
            .flatMap(List::stream)
            .collect(Collectors.toList()),
            Helpers.getExpectedDateAndHourFiles(Map.of(
                "1970-01-03", List.of("0", "1", "2", "3", "4"),
                "1970-01-04", List.of("0", "1", "2", "3", "4"),
                "1970-01-05", List.of("0", "1", "2", "3", "4")), "1970-01-05"),
            LocalDateTime.of(1970, 1, 5, 12, 0)
        ),
        //Does not include empty dates
        new TestTuple(IntStream.range(0, 3)
            .filter(currentDate -> currentDate != 1)
            .mapToObj(currentDate -> IntStream.range(0, 5)
                .mapToObj(currentHour ->
                    buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 3 + currentDate, 0, 0).plusHours(currentHour), 5))
                .flatMap(List::stream)
                .collect(Collectors.toList()))
            .flatMap(List::stream)
            .collect(Collectors.toList()),
            Helpers.getExpectedDateAndHourFiles(Map.of(
                "1970-01-03", List.of("0", "1", "2", "3", "4"),
                "1970-01-05", List.of("0", "1", "2", "3", "4")), "1970-01-05"),
            LocalDateTime.of(1970, 1, 5, 12, 0)
        ),
        //Does not include dates with too few keys
        new TestTuple(
            List.of(
                buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 3, 1, 0), 5),
                buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 4, 1, 0), 4),
                buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 5, 1, 0), 5))
                .stream()
                .flatMap(List::stream)
                .collect(Collectors.toList()),
            Helpers.getExpectedDateAndHourFiles(Map.of(
                "1970-01-03", List.of("1"),
                "1970-01-05", List.of("1")), "1970-01-06"),
            LocalDateTime.of(1970, 1, 6, 12, 0)
        ),
        //Does not include dates in the future
        new TestTuple(
            List.of(
                buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 3, 1, 0), 5),
                buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 4, 1, 0), 5),
                buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 5, 1, 0), 5))
                .stream()
                .flatMap(List::stream)
                .collect(Collectors.toList()),
            Helpers.getExpectedDateAndHourFiles(Map.of(
                "1970-01-03", List.of("1"),
                "1970-01-04", List.of("1")), "1970-01-04"),
            LocalDateTime.of(1970, 1, 4, 12, 0)
        )
    ).map(Arguments::of);
  }

  @BeforeEach
  void setupAll() throws IOException {
    outputFolder.create();
    outputFile = outputFolder.newFolder();
  }

  @ParameterizedTest
  @MethodSource("createDiagnosisKeysAndExpectedFiles")
  void checkBuildsTheCorrectDateDirectoryStructure(TestTuple testTuple) {

    DiagnosisKeyBundler bundler = new ProdDiagnosisKeyBundler(distributionServiceConfig);

    bundler.setDiagnosisKeys(testTuple.diagnosisKeys, testTuple.distributionRun);

    DiagnosisKeysDateDirectory dateDirectory = new DiagnosisKeysDateDirectory(bundler, cryptoProvider,
        distributionServiceConfig);

    Directory<WritableOnDisk> outputDirectory = new DirectoryOnDisk(outputFile);

    outputDirectory.addWritable(dateDirectory);

    dateDirectory.prepare(new ImmutableStack<>()
        .push("version-directory")
        .push("country-directory")
    );
    outputDirectory.write();

    final Set<String> actualFiles = Helpers.getFilePaths(outputFile, outputFile.getAbsolutePath());

    Assertions.assertThat(actualFiles).isEqualTo(testTuple.expectedFiles);
  }


}
