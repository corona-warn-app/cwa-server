package app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.file;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.TemporaryExposureKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.TemporaryExposureKeyExport;
import app.coronawarn.server.services.distribution.assembly.structure.archive.decorator.signing.AbstractSigningDecorator;
import app.coronawarn.server.services.distribution.assembly.structure.file.FileOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import com.google.protobuf.ByteString;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TemporaryExposureKeyExportFile extends FileOnDisk {

  private static final String INDEX_FILE_NAME = "export.bin";

  private final Collection<TemporaryExposureKey> temporaryExposureKeys;
  private final String region;
  private final long startTimestamp;
  private final long endTimestamp;

  private TemporaryExposureKeyExportFile(Collection<TemporaryExposureKey> temporaryExposureKeys,
      String region, long startTimestamp, long endTimestamp) {
    super(INDEX_FILE_NAME, new byte[0]);
    this.temporaryExposureKeys = temporaryExposureKeys;
    this.region = region;
    this.startTimestamp = startTimestamp;
    this.endTimestamp = endTimestamp;
  }

  /**
   * TODO.
   */
  public static TemporaryExposureKeyExportFile fromTemporaryExposureKeys(
      Collection<TemporaryExposureKey> temporaryExposureKeys, String region, long startTimestamp,
      long endTimestamp) {
    return new TemporaryExposureKeyExportFile(temporaryExposureKeys, region, startTimestamp,
        endTimestamp);
  }

  /**
   * TODO.
   */
  public static TemporaryExposureKeyExportFile fromDiagnosisKeys(
      Collection<DiagnosisKey> diagnosisKeys, String region) {
    return new TemporaryExposureKeyExportFile(
        getTemporaryExposureKeysFromDiagnosisKeys(diagnosisKeys), region,
        getStartTimestamp(diagnosisKeys), getEndTimestamp(diagnosisKeys));
  }

  @Override
  public void prepare(ImmutableStack<Object> indices) {
    this.setBytes(createTemporaryExposureKeyExportBytes());
    super.prepare(indices);
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
        // TODO Rolling start number and period should be int32
        .setRollingStartIntervalNumber(Math.toIntExact(diagnosisKey.getRollingStartNumber()))
        .setRollingPeriod(Math.toIntExact(diagnosisKey.getRollingPeriod()))
        .build())
        .collect(Collectors.toSet());
  }

  private static long getStartTimestamp(Collection<DiagnosisKey> diagnosisKeys) {
    return diagnosisKeys.stream()
        .mapToLong(DiagnosisKey::getSubmissionTimestamp)
        .min()
        .orElseThrow(NoSuchElementException::new);
  }

  private static long getEndTimestamp(Collection<DiagnosisKey> diagnosisKeys) {
    return diagnosisKeys.stream()
        .mapToLong(DiagnosisKey::getSubmissionTimestamp)
        .max()
        .orElseThrow(NoSuchElementException::new);
  }
}
