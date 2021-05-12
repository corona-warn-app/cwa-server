package app.coronawarn.server.services.distribution.assembly.tracewarnings.structure.directory.file;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import app.coronawarn.server.common.persistence.domain.TraceTimeIntervalWarning;
import app.coronawarn.server.common.shared.collection.ImmutableStack;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.tracewarnings.structure.file.TraceTimeIntervalWarningExportFile;
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
class TraceTimeIntervalWarningExportFileTest {

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
  void fromTraceTimeIntervalWarningsShouldReturnNewInstance() throws Exception {
    //given
    List<TraceTimeIntervalWarning> traceTimeWarnings = Collections.emptyList();
    String country = "DE";
    int intervalNumber = 44;

    //when
    when(tekExport.getFileName()).thenReturn(FILE_NAME, FILE_NAME);
    when(distributionServiceConfig.getTekExport()).thenReturn(tekExport, tekExport);

    File outputFile = createExportFileWithChecksum(traceTimeWarnings, country, intervalNumber);
    File outputFile2 = createExportFileWithChecksum(traceTimeWarnings, country, intervalNumber);

    byte[] checksum = Files.readAllBytes(outputFile.toPath().resolve(FILE_NAME_CHECKSUM));
    byte[] checksum2 = Files.readAllBytes(outputFile2.toPath().resolve(FILE_NAME_CHECKSUM));

    assertThat(checksum).isEqualTo(checksum2);
  }

  private File createExportFileWithChecksum(List<TraceTimeIntervalWarning> traceTimeWarnings, String country,
      int intervalNumber) throws IOException {
    TraceTimeIntervalWarningExportFile exportFile2 = TraceTimeIntervalWarningExportFile.fromTraceTimeIntervalWarnings(
        traceTimeWarnings, country, intervalNumber, distributionServiceConfig);
    File outputFile2 = tempFolder.newFolder();
    Directory<WritableOnDisk> directory2 = new DirectoryOnDisk(outputFile2);
    directory2.addWritable(exportFile2);
    directory2.prepare(new ImmutableStack<>());
    directory2.write();
    return outputFile2;
  }

}
