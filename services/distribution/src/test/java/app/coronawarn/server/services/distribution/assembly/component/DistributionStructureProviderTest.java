

package app.coronawarn.server.services.distribution.assembly.component;

import static app.coronawarn.server.services.distribution.common.Helpers.buildDiagnosisKeys;
import static app.coronawarn.server.services.distribution.common.Helpers.buildTraceTimeIntervalWarning;
import static java.util.Collections.emptyList;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.domain.TraceTimeIntervalWarning;
import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.common.persistence.service.TraceTimeIntervalWarningService;
import app.coronawarn.server.common.persistence.service.common.KeySharingPoliciesChecker;
import app.coronawarn.server.services.distribution.assembly.diagnosiskeys.DiagnosisKeyBundler;
import app.coronawarn.server.services.distribution.assembly.diagnosiskeys.ProdDiagnosisKeyBundler;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import app.coronawarn.server.services.distribution.assembly.tracewarnings.TraceWarningsPackageBundler;
import app.coronawarn.server.services.distribution.assembly.transformation.EnfParameterAdapter;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.config.TransmissionRiskLevelEncoding;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.Rule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@EnableConfigurationProperties(value = {DistributionServiceConfig.class, TransmissionRiskLevelEncoding.class})
@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    classes = {CryptoProvider.class, DistributionServiceConfig.class,
        KeySharingPoliciesChecker.class, EnfParameterAdapter.class,},
    initializers = ConfigFileApplicationContextInitializer.class)
class DistributionStructureProviderTest {

  @Autowired
  CryptoProvider cryptoProvider;

  @Autowired
  KeySharingPoliciesChecker sharingPoliciesChecker;

  @Autowired
  EnfParameterAdapter enfParameterAdapter;

  @Autowired
  DistributionServiceConfig distributionServiceConfig;

  @MockBean
  OutputDirectoryProvider outputDirectoryProvider;

  @Mock
  DiagnosisKeyService diagnosisKeyService;
  List<DiagnosisKey> diagnosisKeys;

  @Mock
  TraceTimeIntervalWarningService traceTimeIntervalWarningService;

  @Rule
  final TemporaryFolder outputFolder = new TemporaryFolder();
  File outputDirectory;

  @BeforeEach
  void setup() throws IOException {
    diagnosisKeys = IntStream.range(0, 30)
        .mapToObj(currentHour -> buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 3, 0, 0).plusHours(currentHour), 5))
        .flatMap(List::stream)
        .collect(Collectors.toList());
    Mockito.when(diagnosisKeyService.getDiagnosisKeys()).thenReturn(diagnosisKeys);

    outputFolder.create();
    outputDirectory = outputFolder.newFolder("parent");
    Directory<WritableOnDisk> parentDirectory = new DirectoryOnDisk(outputDirectory);
    Directory<WritableOnDisk> spyParentDirectory = spy(parentDirectory);
    when(outputDirectoryProvider.getDirectory()).thenReturn(spyParentDirectory);
  }

  @Test
  void testGetDiagnosisKeysReturnsCorrectDirectoryName() {
    DiagnosisKeyBundler bundler = new ProdDiagnosisKeyBundler(distributionServiceConfig, sharingPoliciesChecker);
    DistributionStructureProvider distributionStructureProvider = new DistributionStructureProvider(
        diagnosisKeyService, null, cryptoProvider, distributionServiceConfig, bundler, null, enfParameterAdapter);
    Directory<WritableOnDisk> diagnosisKeys = distributionStructureProvider.getDiagnosisKeys();
    Assertions.assertEquals("diagnosis-keys", diagnosisKeys.getName());
  }

  @Test
  void testGetTraceWarningsReturnsCorrectDirectoryName() {
    when(traceTimeIntervalWarningService.getTraceTimeIntervalWarning()).thenReturn(emptyList());
    TraceWarningsPackageBundler bundler = new TraceWarningsPackageBundler(distributionServiceConfig);
    DistributionStructureProvider distributionStructureProvider = new DistributionStructureProvider(
        null, traceTimeIntervalWarningService, cryptoProvider, distributionServiceConfig, null, bundler, enfParameterAdapter);
    Directory<WritableOnDisk> traceWarnings = distributionStructureProvider.getTraceWarnings();
    Assertions.assertEquals("twp", traceWarnings.getName());
  }

  /**
   * A new TraceWarningPackage shall be assembled and published to CDN at the beginning of the hour,
   * according to HTTP Response for Hour Package Download. They shall include all TraceTimeIntervalWarnings
   * that have been submitted and but yet published (i.e. usually all the records that have been received since
   * the previous publishing of an Hour Package).
   *
   * An Hour Package shall be named after the hour interval derived from the current timestamp as per Derive Hour
   * Interval from Timestamp. The resulting package name should be a 6-digit number such as 448188.
   */
  @Test
  void testPublishingNewHourPackages() throws IOException {
    List<TraceTimeIntervalWarning> traceWarnings = buildTraceTimeIntervalWarning(5, 10, 30);
    when(traceTimeIntervalWarningService.getTraceTimeIntervalWarning()).thenReturn(traceWarnings);
    Directory<WritableOnDisk> outputDirectory = this.outputDirectoryProvider.getDirectory();
    TraceWarningsPackageBundler bundler = new TraceWarningsPackageBundler(distributionServiceConfig);
    DistributionStructureProvider distributionStructureProvider = new DistributionStructureProvider(
        null, traceTimeIntervalWarningService, cryptoProvider, distributionServiceConfig, null, bundler, enfParameterAdapter);
    outputDirectory.addWritable(distributionStructureProvider.getTraceWarnings());
    outputDirectoryProvider.clear();
    outputDirectory.prepare(new ImmutableStack<>());
    outputDirectory.write();
  }

  /**
   * If there are no TraceTimeIntervalWarnings to be published and the package would be empty, an empty file
   * (not a zip file with signature!) shall be published on CDN instead with HTTP response header cwa-empty-pkg:
   * 1 (to indicate that the package is empty).
   */
  @Test
  void testPublishingEmptyNewHourPackages() throws IOException {
    when(traceTimeIntervalWarningService.getTraceTimeIntervalWarning()).thenReturn(emptyList());
    Directory<WritableOnDisk> outputDirectory = this.outputDirectoryProvider.getDirectory();
    TraceWarningsPackageBundler bundler = new TraceWarningsPackageBundler(distributionServiceConfig);
    DistributionStructureProvider distributionStructureProvider = new DistributionStructureProvider(
        null, traceTimeIntervalWarningService, cryptoProvider, distributionServiceConfig, null, bundler, enfParameterAdapter);

    // Trigger Publishing of the TWPs
    Directory<WritableOnDisk> traceWarnings = distributionStructureProvider.getTraceWarnings();
    outputDirectory.addWritable(traceWarnings);
    outputDirectoryProvider.clear();
    outputDirectory.prepare(new ImmutableStack<>());
    outputDirectory.write();

    // check resulting package
    // check that it contains an empty file for the current interval
    Assertions.assertEquals(outputDirectory, "TODO");
  }

}
