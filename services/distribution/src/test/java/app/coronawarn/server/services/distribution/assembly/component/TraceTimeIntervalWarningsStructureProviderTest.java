package app.coronawarn.server.services.distribution.assembly.component;

import static org.mockito.Mockito.*;
import static app.coronawarn.server.services.distribution.common.Helpers.*;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.Rule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.domain.TraceTimeIntervalWarning;
import app.coronawarn.server.common.persistence.service.TraceTimeIntervalWarningService;
import app.coronawarn.server.common.persistence.service.common.KeySharingPoliciesChecker;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import app.coronawarn.server.services.distribution.assembly.tracewarnings.TraceTimeIntervalWarningsPackageBundler;
import app.coronawarn.server.services.distribution.assembly.transformation.EnfParameterAdapter;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.config.TransmissionRiskLevelEncoding;

@EnableConfigurationProperties(
    value = {DistributionServiceConfig.class, TransmissionRiskLevelEncoding.class})
@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    classes = {CryptoProvider.class, DistributionServiceConfig.class,
        KeySharingPoliciesChecker.class, EnfParameterAdapter.class,},
    initializers = ConfigFileApplicationContextInitializer.class)
public class TraceTimeIntervalWarningsStructureProviderTest {

  @Autowired
  CryptoProvider cryptoProvider;

  @Autowired
  KeySharingPoliciesChecker sharingPoliciesChecker;

  @Autowired
  EnfParameterAdapter enfParameterAdapter;

  @Autowired
  DistributionServiceConfig distributionServiceConfig;

  @Mock
  TraceTimeIntervalWarningService traceTimeWarningService;
  List<DiagnosisKey> diagnosisKeys;

  @MockBean
  OutputDirectoryProvider outputDirectoryProvider;

  TraceTimeIntervalWarningsPackageBundler bundler;

  @Rule
  TemporaryFolder outputFolder = new TemporaryFolder();
  File outputDirectory;

  @BeforeEach
  public void setup() throws IOException {
    bundler = new TraceTimeIntervalWarningsPackageBundler(distributionServiceConfig);

    outputFolder.create();
    outputDirectory = outputFolder.newFolder("parent");
    Directory<WritableOnDisk> parentDirectory = new DirectoryOnDisk(outputDirectory);
    Directory<WritableOnDisk> spyParentDirectory = spy(parentDirectory);
    when(outputDirectoryProvider.getDirectory()).thenReturn(spyParentDirectory);
  }

  @Test
  void testGetTraceWarningsReturnsCorrectDirectoryName() {
    when(traceTimeWarningService.getTraceTimeIntervalWarning()).thenReturn(Collections.emptyList());
    TraceTimeIntervalWarningsStructureProvider distributionStructureProvider =
        new TraceTimeIntervalWarningsStructureProvider(traceTimeWarningService, bundler,
            cryptoProvider, distributionServiceConfig);
    Directory<WritableOnDisk> traceWarnings = distributionStructureProvider.getTraceWarningsDirectory();
    Assertions.assertEquals("twp", traceWarnings.getName());
  }

  /**
   * A new TraceWarningPackage shall be assembled and published to CDN at the beginning of the hour,
   * according to HTTP Response for Hour Package Download. They shall include all
   * TraceTimeIntervalWarnings that have been submitted and but yet published (i.e. usually all the
   * records that have been received since the previous publishing of an Hour Package).
   *
   * An Hour Package shall be named after the hour interval derived from the current timestamp as
   * per Derive Hour Interval from Timestamp. The resulting package name should be a 6-digit number
   * such as 448188.
   */
  @Test
  void testPublishingNewHourPackages() throws IOException {
    List<TraceTimeIntervalWarning> traceWarnings = buildTraceTimeIntervalWarning(5, 10, 30);
    traceWarnings.addAll(buildTraceTimeIntervalWarning(70, 100, 30));
    traceWarnings.addAll(buildTraceTimeIntervalWarning(90, 160, 30));
    when(traceTimeWarningService.getTraceTimeIntervalWarning()).thenReturn(traceWarnings);
    Directory<WritableOnDisk> outputDirectory = this.outputDirectoryProvider.getDirectory();
    TraceTimeIntervalWarningsStructureProvider distributionStructureProvider =
        new TraceTimeIntervalWarningsStructureProvider(traceTimeWarningService, bundler,
            cryptoProvider, distributionServiceConfig);
    outputDirectory.addWritable(distributionStructureProvider.getTraceWarningsDirectory());
    outputDirectoryProvider.clear();
    outputDirectory.prepare(new ImmutableStack<>());
    outputDirectory.write();

    Set<String> actualFiles = getFilePaths(outputFolder.getRoot(), outputFolder.getRoot().getAbsolutePath());
    //TODO : Write assertions
  }

  /**
   * If there are no TraceTimeIntervalWarnings to be published and the package would be empty, an
   * empty file (not a zip file with signature!) shall be published on CDN instead with HTTP
   * response header cwa-empty-pkg: 1 (to indicate that the package is empty).
   */
  @Test
  void testPublishingEmptyNewHourPackages() throws IOException {
    when(traceTimeWarningService.getTraceTimeIntervalWarning()).thenReturn(Collections.emptyList());
    Directory<WritableOnDisk> outputDirectory = this.outputDirectoryProvider.getDirectory();
    TraceTimeIntervalWarningsStructureProvider distributionStructureProvider =
        new TraceTimeIntervalWarningsStructureProvider(traceTimeWarningService, bundler,
            cryptoProvider, distributionServiceConfig);

    // Trigger Publishing of the TWPs
    Directory<WritableOnDisk> traceWarnings = distributionStructureProvider.getTraceWarningsDirectory();
    outputDirectory.addWritable(traceWarnings);
    outputDirectoryProvider.clear();
    outputDirectory.prepare(new ImmutableStack<>());
    outputDirectory.write();

    // check resulting package
    // check that it contains an empty file for the current interval
    Assertions.assertEquals(outputDirectory, "TODO");
  }
}
