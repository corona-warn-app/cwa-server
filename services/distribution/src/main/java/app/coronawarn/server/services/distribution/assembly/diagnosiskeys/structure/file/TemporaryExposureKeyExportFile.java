package app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.file;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.TemporaryExposureKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.TemporaryExposureKeyExport;
import app.coronawarn.server.services.distribution.assembly.structure.file.FileOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import com.google.common.base.Strings;
import com.google.common.primitives.Bytes;
import com.google.protobuf.ByteString;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A {@link app.coronawarn.server.services.distribution.assembly.structure.file.File} containing a {@link
 * TemporaryExposureKeyExport}.
 */
public class TemporaryExposureKeyExportFile extends FileOnDisk {

  private final Collection<TemporaryExposureKey> temporaryExposureKeys;
  private final String region;
  private final long startTimestamp;
  private final long endTimestamp;
  private final DistributionServiceConfig distributionServiceConfig;

  private TemporaryExposureKeyExportFile(Collection<TemporaryExposureKey> temporaryExposureKeys, String region,
      long startTimestamp, long endTimestamp, DistributionServiceConfig distributionServiceConfig) {
    super(distributionServiceConfig.getTekExport().getFileName(), new byte[0]);
    this.temporaryExposureKeys = temporaryExposureKeys;
    this.region = region;
    this.startTimestamp = startTimestamp;
    this.endTimestamp = endTimestamp;
    this.distributionServiceConfig = distributionServiceConfig;
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
   * @return A new {@link TemporaryExposureKeyExportFile}.
   */
  public static TemporaryExposureKeyExportFile fromTemporaryExposureKeys(
      Collection<TemporaryExposureKey> temporaryExposureKeys, String region, long startTimestamp, long endTimestamp,
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
   * @return A new {@link TemporaryExposureKeyExportFile}.
   */
  public static TemporaryExposureKeyExportFile fromDiagnosisKeys(Collection<DiagnosisKey> diagnosisKeys, String region,
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
    return Bytes.concat(this.getHeaderBytes(), createTemporaryExposureKeyExportBytes());
  }

  private byte[] createTemporaryExposureKeyExportBytes() {
    return TemporaryExposureKeyExport.newBuilder()
        .setStartTimestamp(this.startTimestamp)
        .setEndTimestamp(this.endTimestamp)
        .setRegion(this.region)
        // TODO Use buildPartial and then set batch stuff somewhere else
        .setBatchNum(1)
        .setBatchSize(1)
        .addAllSignatureInfos(Set.of(distributionServiceConfig.getSignature().getSignatureInfo()))
        .addAllKeys(this.temporaryExposureKeys)
        .build()
        .toByteArray();
  }

  private static Set<TemporaryExposureKey> getTemporaryExposureKeysFromDiagnosisKeys(
      Collection<DiagnosisKey> diagnosisKeys) {
    return diagnosisKeys.stream().map(diagnosisKey -> TemporaryExposureKey.newBuilder()
        .setKeyData(ByteString.copyFrom(diagnosisKey.getKeyData()))
        .setTransmissionRiskLevel(diagnosisKey.getTransmissionRiskLevel())
        // TODO cwa-server/#233 Rolling start number and period should be int32
        .setRollingStartIntervalNumber(diagnosisKey.getRollingStartIntervalNumber())
        .setRollingPeriod(diagnosisKey.getRollingPeriod())
        .build())
        .collect(Collectors.toSet());
  }

  private byte[] getHeaderBytes() {
    String header = distributionServiceConfig.getTekExport().getFileHeader();
    int headerWidth = distributionServiceConfig.getTekExport().getFileHeaderWidth();
    return Strings.padEnd(header, headerWidth, ' ').getBytes(StandardCharsets.UTF_8);
  }

  /**
   * Returns the bytes of this TemporaryExposureKeyExportFile, but without the header.
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
