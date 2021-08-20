package app.coronawarn.server.services.distribution.assembly.tracewarnings.structure.file;

import app.coronawarn.server.common.persistence.domain.CheckInProtectedReports;
import app.coronawarn.server.common.protocols.internal.pt.TraceWarningPackage;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import com.google.protobuf.ByteString;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A {@link app.coronawarn.server.services.distribution.assembly.structure.file.File} containing a list of {@link
 * app.coronawarn.server.common.protocols.internal.pt.CheckInProtectedReport} serialized protos.
 */
public class CheckInProtectedReportsExportFile extends AbstractCheckInExportFile {

  private final List<app.coronawarn.server.common.protocols.internal.pt.CheckInProtectedReport>
      checkInProtectedReports;

  public CheckInProtectedReportsExportFile(
      List<app.coronawarn.server.common.protocols.internal.pt.CheckInProtectedReport> checkInProtectedReports,
      String region, int intervalNumber, DistributionServiceConfig distributionServiceConfig) {
    super(region, intervalNumber, distributionServiceConfig.getTekExport().getFileName());
    this.checkInProtectedReports = checkInProtectedReports;
  }

  protected byte[] createTraceWarningExportBytes() {
    return TraceWarningPackage.newBuilder().setIntervalNumber(this.intervalNumber)
        .setRegion(this.region).addAllCheckInProtectedReports(this.checkInProtectedReports).build()
        .toByteArray();
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

  private static List<app.coronawarn.server.common.protocols.internal.pt.CheckInProtectedReport>
      getCheckInProtectedReportFromCheckInProtectedReports(
      List<CheckInProtectedReports> traceTimeIntervalWarnings) {

    return traceTimeIntervalWarnings.stream()
        .sorted(Comparator.comparing(CheckInProtectedReports::getId))
        .map(
            checkInReports -> app.coronawarn.server.common.protocols.internal.pt.CheckInProtectedReport
                .newBuilder()
                .setMac(getMacValue(checkInReports))
                .setLocationIdHash(ByteString.copyFrom(checkInReports.getTraceLocationIdHash()))
                .setEncryptedCheckInRecord(ByteString.copyFrom(checkInReports.getEncryptedCheckInRecord()))
                .setIv(ByteString.copyFrom(checkInReports.getInitializationVector())).build())
        .collect(Collectors.toList());
  }

  private static ByteString getMacValue(CheckInProtectedReports checkInReports) {
    return checkInReports.getMac() == null ? ByteString.EMPTY : ByteString.copyFrom(checkInReports.getMac());
  }
}
