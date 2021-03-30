package app.coronawarn.server.services.distribution.assembly.tracewarnings.structure.file;

import app.coronawarn.server.common.persistence.domain.TraceTimeIntervalWarning;
import app.coronawarn.server.common.protocols.internal.pt.TraceWarningPackage;
import app.coronawarn.server.services.distribution.assembly.structure.file.FileOnDiskWithChecksum;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import com.google.protobuf.ByteString;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class TraceTimeIntervalWarningExportFile extends FileOnDiskWithChecksum {

  private final Set<app.coronawarn.server.common
                       .protocols.internal.pt.TraceTimeIntervalWarning> traceTimeIntervalWarnings;
  private final String region;
  private final int intervalNumber;
  private final DistributionServiceConfig distributionServiceConfig;

  TraceTimeIntervalWarningExportFile(
      Set<app.coronawarn.server.common.protocols.internal.pt.TraceTimeIntervalWarning> traceTimeIntervalWarnings,
      String region, int intervalNumber, DistributionServiceConfig distributionServiceConfig) {
    super(distributionServiceConfig.getTekExport().getFileName(), new byte[0]);

    this.region = region;
    this.intervalNumber = intervalNumber;
    this.distributionServiceConfig = distributionServiceConfig;
    this.traceTimeIntervalWarnings = traceTimeIntervalWarnings;
  }

  @Override
  public void prepare(ImmutableStack<Object> indices) {
    this.setBytes(createTraceWarningExportBytesWithHeader());
    super.prepare(indices);
  }

  /**
   * Creates a binary export file by converting the given warnings to their proto structures.
   */
  public static TraceTimeIntervalWarningExportFile fromTraceTimeIntervalWarnings(
      Collection<TraceTimeIntervalWarning> traceTimeIntervalWarnings, String country,
      int intervalNumber, DistributionServiceConfig distributionServiceConfig) {
    return new TraceTimeIntervalWarningExportFile(
        getTraceIntervalWarningsFromTraceIntervalWarnings(traceTimeIntervalWarnings), country,
        intervalNumber, distributionServiceConfig);
  }

  private byte[] createTraceWarningExportBytesWithHeader() {
    byte[] headerBytes = this.getHeaderBytes();
    byte[] traceWarningExportBytes = createTraceWarningExportBytes();
    return concatenate(headerBytes, traceWarningExportBytes);
  }

  private byte[] concatenate(byte[] arr1, byte[] arr2) {
    var concatenatedBytes = Arrays.copyOf(arr1, arr1.length + arr2.length);
    System.arraycopy(arr2, 0, concatenatedBytes, arr1.length, arr2.length);
    return concatenatedBytes;
  }

  private byte[] createTraceWarningExportBytes() {
    return TraceWarningPackage.newBuilder().setIntervalNumber(this.intervalNumber)
        .setRegion(this.region).addAllTimeIntervalWarnings(this.traceTimeIntervalWarnings).build()
        .toByteArray();
  }

  private static Set<app.coronawarn.server.common.protocols.internal.pt.TraceTimeIntervalWarning>
      getTraceIntervalWarningsFromTraceIntervalWarnings(
      Collection<TraceTimeIntervalWarning> traceTimeIntervalWarnings) {

    return traceTimeIntervalWarnings.stream().map(
        intervalWarning -> app.coronawarn.server.common.protocols.internal.pt.TraceTimeIntervalWarning
            .newBuilder()
            .setLocationIdHash(ByteString.copyFrom(intervalWarning.getTraceLocationId()))
            .setStartIntervalNumber(intervalWarning.getStartIntervalNumber())
            .setTransmissionRiskLevel(intervalWarning.getTransmissionRiskLevel()).build())
        .collect(Collectors.toSet());
  }

  private byte[] getHeaderBytes() {
    String header = distributionServiceConfig.getTekExport().getFileHeader();
    int headerWidth = distributionServiceConfig.getTekExport().getFileHeaderWidth();
    return padRight(header, headerWidth).getBytes(StandardCharsets.UTF_8);
  }

  private String padRight(String string, int padding) {
    String format = "%1$-" + padding + "s";
    return String.format(format, string);
  }
}
