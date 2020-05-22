package app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.file;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.TemporaryExposureKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.TemporaryExposureKeyExport;
import app.coronawarn.server.services.distribution.assembly.structure.archive.decorator.signing.AbstractSigningDecorator;
import app.coronawarn.server.services.distribution.assembly.structure.file.FileOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
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

  /**
   * The mandatory header for all TEK Export files.
   */
  private static final byte[] HEADER = "EK Export v1    ".getBytes(StandardCharsets.UTF_8);

  private static final String INDEX_FILE_NAME = "export.bin";

  private final Collection<TemporaryExposureKey> temporaryExposureKeys;
  private final String region;
  private final long startTimestamp;
  private final long endTimestamp;

  private TemporaryExposureKeyExportFile(Collection<TemporaryExposureKey> temporaryExposureKeys, String region,
      long startTimestamp, long endTimestamp) {
    super(INDEX_FILE_NAME, new byte[0]);
    this.temporaryExposureKeys = temporaryExposureKeys;
    this.region = region;
    this.startTimestamp = startTimestamp;
    this.endTimestamp = endTimestamp;
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
      Collection<TemporaryExposureKey> temporaryExposureKeys, String region, long startTimestamp, long endTimestamp) {
    return new TemporaryExposureKeyExportFile(temporaryExposureKeys, region, startTimestamp, endTimestamp);
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
      long startTimestamp, long endTimestamp) {
    return new TemporaryExposureKeyExportFile(getTemporaryExposureKeysFromDiagnosisKeys(diagnosisKeys), region,
        startTimestamp, endTimestamp);
  }

  @Override
  public void prepare(ImmutableStack<Object> indices) {
    this.setBytes(createKeyExportBytesWithHeader());
    super.prepare(indices);
  }

  private byte[] createKeyExportBytesWithHeader() {
    return Bytes.concat(HEADER, createTemporaryExposureKeyExportBytes());
  }

  private byte[] createTemporaryExposureKeyExportBytes() {
    return TemporaryExposureKeyExport.newBuilder()
        .setStartTimestamp(this.startTimestamp)
        .setEndTimestamp(this.endTimestamp)
        .setRegion(this.region)
        // TODO Use buildPartial and then set batch stuff somewhere else
        .setBatchNum(1)
        .setBatchSize(1)
        .addAllSignatureInfos(Set.of(AbstractSigningDecorator.getSignatureInfo()))
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
        .setRollingStartIntervalNumber(Math.toIntExact(diagnosisKey.getRollingStartNumber()))
        .setRollingPeriod(Math.toIntExact(diagnosisKey.getRollingPeriod()))
        .build())
        .collect(Collectors.toSet());
  }

  /**
   * Returns a new byte array based on the given byte array, without the TEK file header.
   *
   * @param keyExportBytes the files of the key export file (with headers).
   * @return the original key export file bytes, representing the proto definition.
   */
  public static byte[] withoutFileHeader(byte[] keyExportBytes) {
    if (!Arrays.equals(keyExportBytes, 0, HEADER.length, HEADER, 0, HEADER.length)) {
      throw new IllegalArgumentException("Supplied bytes are not starting with EK Export File header");
    }

    return Arrays.copyOfRange(keyExportBytes, HEADER.length, keyExportBytes.length);
  }
}
