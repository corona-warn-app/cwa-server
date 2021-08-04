package app.coronawarn.server.services.distribution.assembly.tracewarnings.structure.file;

import app.coronawarn.server.common.persistence.domain.CheckInProtectedReports;
import app.coronawarn.server.common.protocols.internal.pt.TraceWarningPackage;
import app.coronawarn.server.common.shared.collection.ImmutableStack;
import app.coronawarn.server.services.distribution.assembly.structure.file.FileOnDiskWithChecksum;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import com.google.protobuf.ByteString;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A {@link app.coronawarn.server.services.distribution.assembly.structure.file.File} containing a list of {@link
 * app.coronawarn.server.common.protocols.internal.pt.CheckInProtectedReport} serialized protos.
 */
public class CheckInProtectedReportsExportFile extends FileOnDiskWithChecksum {

  private final List<app.coronawarn.server.common.protocols.internal.pt.CheckInProtectedReport>
      checkInProtectedReports;
  private final String region;
  private final int intervalNumber;

  CheckInProtectedReportsExportFile(
      List<app.coronawarn.server.common.protocols.internal.pt.CheckInProtectedReport> checkInProtectedReports,
      String region, int intervalNumber, DistributionServiceConfig distributionServiceConfig) {
    super(distributionServiceConfig.getTekExport().getFileName(), new byte[0]);

    this.region = region;
    this.intervalNumber = intervalNumber;
    this.checkInProtectedReports = checkInProtectedReports;
  }

  @Override
  public void prepare(ImmutableStack<Object> indices) {
    this.setBytes(createTraceWarningExportBytes());
    super.prepare(indices);
  }

  /**
   * Creates a binary export file by converting the given warnings to their proto structures.
   */
  public static CheckInProtectedReportsExportFile fromCheckInProtectedReports(
      List<CheckInProtectedReports> traceTimeIntervalWarnings, String country,
      int intervalNumber, DistributionServiceConfig distributionServiceConfig) {
    return new CheckInProtectedReportsExportFile(
        getCheckInProtectedReportFromCheckInProtectedReports(traceTimeIntervalWarnings), country,
        intervalNumber, distributionServiceConfig);
  }

  private byte[] createTraceWarningExportBytes() {
    return TraceWarningPackage.newBuilder().setIntervalNumber(this.intervalNumber)
        .setRegion(this.region).addAllCheckInProtectedReports(this.checkInProtectedReports).build()
        .toByteArray();
  }

  private static List<app.coronawarn.server.common.protocols.internal.pt.CheckInProtectedReport>
      getCheckInProtectedReportFromCheckInProtectedReports(
      List<CheckInProtectedReports> traceTimeIntervalWarnings) {

    return traceTimeIntervalWarnings.stream()
        .sorted(Comparator.comparing(CheckInProtectedReports::getId))
        .map(
            checkInReports -> app.coronawarn.server.common.protocols.internal.pt.CheckInProtectedReport
                .newBuilder()
                .setLocationIdHash(ByteString.copyFrom(checkInReports.getTraceLocationIdHash()))
                .setEncryptedCheckInRecord(ByteString.copyFrom(checkInReports.getEncryptedCheckInRecord()))
                .setIv(ByteString.copyFrom(checkInReports.getInitializationVector())).build())
        .collect(Collectors.toList());
  }
}
