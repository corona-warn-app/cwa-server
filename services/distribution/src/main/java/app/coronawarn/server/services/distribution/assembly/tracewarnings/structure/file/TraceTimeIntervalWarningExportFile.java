package app.coronawarn.server.services.distribution.assembly.tracewarnings.structure.file;

import app.coronawarn.server.common.persistence.domain.TraceTimeIntervalWarning;
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
 * app.coronawarn.server.common.protocols.internal.pt.TraceTimeIntervalWarning} serialized protos.
 */
public class TraceTimeIntervalWarningExportFile extends FileOnDiskWithChecksum {

  private final List<app.coronawarn.server.common.protocols.internal.pt.TraceTimeIntervalWarning>
      traceTimeIntervalWarnings;
  private final String region;
  private final int intervalNumber;

  TraceTimeIntervalWarningExportFile(
      List<app.coronawarn.server.common.protocols.internal.pt.TraceTimeIntervalWarning> traceTimeIntervalWarnings,
      String region, int intervalNumber, DistributionServiceConfig distributionServiceConfig) {
    super(distributionServiceConfig.getTekExport().getFileName(), new byte[0]);

    this.region = region;
    this.intervalNumber = intervalNumber;
    this.traceTimeIntervalWarnings = traceTimeIntervalWarnings;
  }

  @Override
  public void prepare(ImmutableStack<Object> indices) {
    this.setBytes(createTraceWarningExportBytes());
    super.prepare(indices);
  }

  /**
   * Creates a binary export file by converting the given warnings to their proto structures.
   */
  public static TraceTimeIntervalWarningExportFile fromTraceTimeIntervalWarnings(
      List<TraceTimeIntervalWarning> traceTimeIntervalWarnings, String country,
      int intervalNumber, DistributionServiceConfig distributionServiceConfig) {
    return new TraceTimeIntervalWarningExportFile(
        getTraceIntervalWarningsFromTraceIntervalWarnings(traceTimeIntervalWarnings), country,
        intervalNumber, distributionServiceConfig);
  }

  private byte[] createTraceWarningExportBytes() {
    return TraceWarningPackage.newBuilder().setIntervalNumber(this.intervalNumber)
        .setRegion(this.region).addAllTimeIntervalWarnings(this.traceTimeIntervalWarnings).build()
        .toByteArray();
  }

  private static List<app.coronawarn.server.common.protocols.internal.pt.TraceTimeIntervalWarning>
      getTraceIntervalWarningsFromTraceIntervalWarnings(
      List<TraceTimeIntervalWarning> traceTimeIntervalWarnings) {

    return traceTimeIntervalWarnings.stream()
        .sorted(Comparator.comparing(TraceTimeIntervalWarning::getId))
        .map(
            intervalWarning -> app.coronawarn.server.common.protocols.internal.pt.TraceTimeIntervalWarning
                .newBuilder()
                .setLocationIdHash(ByteString.copyFrom(intervalWarning.getTraceLocationId()))
                .setStartIntervalNumber(intervalWarning.getStartIntervalNumber())
                .setPeriod(intervalWarning.getPeriod())
                .setTransmissionRiskLevel(intervalWarning.getTransmissionRiskLevel()).build())
        .collect(Collectors.toList());
  }
}
