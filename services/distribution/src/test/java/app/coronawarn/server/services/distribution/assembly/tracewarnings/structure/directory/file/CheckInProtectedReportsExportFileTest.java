package app.coronawarn.server.services.distribution.assembly.tracewarnings.structure.directory.file;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import app.coronawarn.server.common.persistence.domain.CheckInProtectedReports;
import app.coronawarn.server.common.protocols.internal.pt.CheckInProtectedReport;
import app.coronawarn.server.common.protocols.internal.pt.TraceWarningPackage;
import app.coronawarn.server.common.shared.collection.ImmutableStack;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.tracewarnings.structure.file.CheckInProtectedReportsExportFile;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.TekExport;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CheckInProtectedReportsExportFileTest {

  public static final String FILE_NAME_CHECKSUM = "fileName.checksum";
  public static final String FILE_NAME = "fileName";

  @Mock
  private DistributionServiceConfig distributionServiceConfig;

  @Mock
  TekExport tekExport;

  @Rule
  TemporaryFolder tempFolder = new TemporaryFolder();

  @BeforeEach
  public void setup() throws Exception {
    tempFolder.create();
  }


  @Test
  void testCheckInProtectedReportsExportFileContainsAllInformation() throws Exception {
    //given
    byte[] traceLocationId = new byte[]{1, 2, 3, 4};
    byte[] iv = new byte[]{1, 2, 3, 4};
    byte[] encryptedCheckInReport = new byte[]{1, 2, 3, 4};
    byte[] mac = new byte[]{};
    long submissionTimeStamp = 1000L;
    CheckInProtectedReports checkInProtectedReport = new CheckInProtectedReports(traceLocationId, iv,
        encryptedCheckInReport, mac, submissionTimeStamp);
    List<CheckInProtectedReports> checkInProtectedReports = Collections.singletonList(checkInProtectedReport);
    String country = "DE";
    int intervalNumber = 44;

    //when
    when(tekExport.getFileName()).thenReturn(FILE_NAME, FILE_NAME);
    when(distributionServiceConfig.getTekExport()).thenReturn(tekExport, tekExport);

    File outputFile = createExportFileWithChecksum(checkInProtectedReports, country, intervalNumber);
    byte[] content = Files.readAllBytes(outputFile.toPath().resolve(FILE_NAME));
    final TraceWarningPackage actual = TraceWarningPackage.parseFrom(content);

    assertThat(actual.getIntervalNumber()).isEqualTo(intervalNumber);
    assertThat(actual.getRegion()).isEqualTo(country);
    assertThat(actual.getCheckInProtectedReportsCount()).isEqualTo(1);
    final CheckInProtectedReport extractedCheckInFromFile = actual.getCheckInProtectedReportsList().get(0);
    assertThat(extractedCheckInFromFile.getEncryptedCheckInRecord().toByteArray()).isEqualTo(encryptedCheckInReport);
    assertThat(extractedCheckInFromFile.getIv().toByteArray()).isEqualTo(iv);
    assertThat(extractedCheckInFromFile.getLocationIdHash().toByteArray()).isEqualTo(traceLocationId);
    assertThat(extractedCheckInFromFile.getMac().toByteArray()).isEqualTo(mac);
  }

  @Test
  void checkInProtectedReportsShouldHaveSameChecksumIfEqualContent() throws Exception {
    //given
    List<CheckInProtectedReports> checkInProtectedReports = Collections.emptyList();
    String country = "DE";
    int intervalNumber = 44;

    //when
    when(tekExport.getFileName()).thenReturn(FILE_NAME, FILE_NAME);
    when(distributionServiceConfig.getTekExport()).thenReturn(tekExport, tekExport);

    File outputFile = createExportFileWithChecksum(checkInProtectedReports, country, intervalNumber);
    File outputFile2 = createExportFileWithChecksum(checkInProtectedReports, country, intervalNumber);

    byte[] checksum = Files.readAllBytes(outputFile.toPath().resolve(FILE_NAME_CHECKSUM));
    byte[] checksum2 = Files.readAllBytes(outputFile2.toPath().resolve(FILE_NAME_CHECKSUM));

    assertThat(checksum).isEqualTo(checksum2);
  }

  private File createExportFileWithChecksum(List<CheckInProtectedReports> protectedCheckIns, String country,
      int intervalNumber) throws IOException {
    CheckInProtectedReportsExportFile underTest = CheckInProtectedReportsExportFile.fromCheckInProtectedReports(
        protectedCheckIns, country, intervalNumber, distributionServiceConfig);
    File outputFile = tempFolder.newFolder();
    Directory<WritableOnDisk> directory = new DirectoryOnDisk(outputFile);
    directory.addWritable(underTest);
    directory.prepare(new ImmutableStack<>());
    directory.write();
    return outputFile;
  }
}
