

package app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.file;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.TemporaryExposureKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.TemporaryExposureKeyExport;
import app.coronawarn.server.common.shared.collection.ImmutableStack;
import app.coronawarn.server.services.distribution.assembly.structure.file.FileOnDiskWithChecksum;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import com.google.protobuf.ByteString;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A {@link app.coronawarn.server.services.distribution.assembly.structure.file.File} containing a {@link
 * TemporaryExposureKeyExport}.
 */
public class TemporaryExposureKeyExportFile extends FileOnDiskWithChecksum {

  private final List<TemporaryExposureKey> temporaryExposureKeys;
  private final String region;
  private final long startTimestamp;
  private final long endTimestamp;
  private final DistributionServiceConfig distributionServiceConfig;

  private TemporaryExposureKeyExportFile(List<TemporaryExposureKey> temporaryExposureKeys, String region,
      long startTimestamp, long endTimestamp, DistributionServiceConfig distributionServiceConfig) {
    super(distributionServiceConfig.getTekExport().getFileName(), new byte[0]);
    this.region = region;
    this.startTimestamp = startTimestamp;
    this.endTimestamp = endTimestamp;
    this.distributionServiceConfig = distributionServiceConfig;

    this.temporaryExposureKeys = temporaryExposureKeys
        .stream()
        .sorted(new TemporaryExposureKeyComparator())
        .collect(Collectors.toList());
  }

  /**
   * Constructs a {@link TemporaryExposureKeyExportFile} from {@link TemporaryExposureKey TemporaryExposureKeys}.
   *
   * @param temporaryExposureKeys The {@link TemporaryExposureKey TemporaryExposureKeys} to bundle into the {@link
   *                              TemporaryExposureKeyExport}.
   * @param region                The region that the {@link TemporaryExposureKey TemporaryExposureKeys} are from.
   * @param startTimestamp        The start of the time window covered by the {@link TemporaryExposureKeyExport}, in UTC
   *                              seconds since epoch.
   * @param endTimestamp          The end of the time window covered by the {@link TemporaryExposureKeyExport}, in UTC *
   *                              seconds since epoch.
   * @param distributionServiceConfig The distribution service configuration {@link DistributionServiceConfig}
   * @return A new {@link TemporaryExposureKeyExportFile}.
   */
  public static TemporaryExposureKeyExportFile fromTemporaryExposureKeys(
      List<TemporaryExposureKey> temporaryExposureKeys, String region, long startTimestamp, long endTimestamp,
      DistributionServiceConfig distributionServiceConfig) {
    return new TemporaryExposureKeyExportFile(temporaryExposureKeys, region, startTimestamp, endTimestamp,
        distributionServiceConfig);
  }

  /**
   * Constructs a {@link TemporaryExposureKeyExportFile} from {@link DiagnosisKey DiagnosisKeys}.
   *
   * @param diagnosisKeys  The {@link DiagnosisKey DiagnosisKeys} to bundle into the {@link
   *                       TemporaryExposureKeyExport}.
   * @param region         The region that the {@link TemporaryExposureKey TemporaryExposureKeys} are from.
   * @param startTimestamp The start of the time window covered by the {@link TemporaryExposureKeyExport}, in UTC
   *                       seconds since epoch.
   * @param endTimestamp   The end of the time window covered by the {@link TemporaryExposureKeyExport}, in UTC *
   *                       seconds since epoch.
   * @param distributionServiceConfig The distribution service configuration {@link DistributionServiceConfig}
   * @return A new {@link TemporaryExposureKeyExportFile}.
   */
  public static TemporaryExposureKeyExportFile fromDiagnosisKeys(List<DiagnosisKey> diagnosisKeys, String region,
      long startTimestamp, long endTimestamp, DistributionServiceConfig distributionServiceConfig) {
    return new TemporaryExposureKeyExportFile(getTemporaryExposureKeysFromDiagnosisKeys(diagnosisKeys), region,
        startTimestamp, endTimestamp, distributionServiceConfig);
  }

  @Override
  public void prepare(ImmutableStack<Object> indices) {
    this.setBytes(createKeyExportBytesWithHeader());
    super.prepare(indices);
  }

  private byte[] createKeyExportBytesWithHeader() {
    byte[] headerBytes = this.getHeaderBytes();
    byte[] temporaryExposureKeyExportBytes = createTemporaryExposureKeyExportBytes();
    return concatenate(headerBytes, temporaryExposureKeyExportBytes);
  }

  private byte[] concatenate(byte[] arr1, byte[] arr2) {
    var concatenatedBytes = Arrays.copyOf(arr1, arr1.length + arr2.length);
    System.arraycopy(arr2, 0, concatenatedBytes, arr1.length, arr2.length);
    return concatenatedBytes;
  }

  private byte[] createTemporaryExposureKeyExportBytes() {
    return TemporaryExposureKeyExport.newBuilder()
        .setStartTimestamp(this.startTimestamp)
        .setEndTimestamp(this.endTimestamp)
        .setRegion(this.region)
        .setBatchNum(1)
        .setBatchSize(1)
        .addAllSignatureInfos(Set.of(distributionServiceConfig.getSignature().getSignatureInfo()))
        .addAllKeys(this.temporaryExposureKeys)
        .build()
        .toByteArray();
  }

  private static List<TemporaryExposureKey> getTemporaryExposureKeysFromDiagnosisKeys(
      List<DiagnosisKey> diagnosisKeys) {
    return diagnosisKeys.stream().map(diagnosisKey -> TemporaryExposureKey.newBuilder()
        .setKeyData(ByteString.copyFrom(diagnosisKey.getKeyData()))
        .setTransmissionRiskLevel(diagnosisKey.getTransmissionRiskLevel())
        .setRollingStartIntervalNumber(diagnosisKey.getRollingStartIntervalNumber())
        .setRollingPeriod(diagnosisKey.getRollingPeriod())
        .setReportType(diagnosisKey.getReportType())
        .setDaysSinceOnsetOfSymptoms(diagnosisKey.getDaysSinceOnsetOfSymptoms())
        .build())
        .collect(Collectors.toList());
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

  /**
   * Returns the bytes of this TemporaryExposureKeyExportFile, but without the header.
   *
   * @return Array copy of TemporaryExposureKey data
   */
  public byte[] getBytesWithoutHeader() {
    byte[] headerBytes = this.getHeaderBytes();
    byte[] fileBytes = this.getBytes();
    if (!Arrays.equals(fileBytes, 0, headerBytes.length, headerBytes, 0, headerBytes.length)) {
      throw new IllegalArgumentException("Supplied bytes are not starting with EK Export File header");
    }
    return Arrays.copyOfRange(fileBytes, headerBytes.length, fileBytes.length);
  }
}
