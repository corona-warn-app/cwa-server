package app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.directory;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.shared.collection.ImmutableStack;
import app.coronawarn.server.services.distribution.assembly.component.CryptoProvider;
import app.coronawarn.server.services.distribution.assembly.diagnosiskeys.DiagnosisKeyBundler;
import app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.file.TemporaryExposureKeyExportFile;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.archive.Archive;
import app.coronawarn.server.services.distribution.assembly.structure.archive.ArchiveOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.archive.decorator.signing.DistributionArchiveSigningDecorator;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.file.File;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

public class DiagnosisKeysHourDirectory extends IndexDirectoryOnDisk<LocalDateTime> {

  private final DiagnosisKeyBundler diagnosisKeyBundler;
  private final CryptoProvider cryptoProvider;
  private final DistributionServiceConfig distributionServiceConfig;

  /**
   * Constructs a {@link DiagnosisKeysHourDirectory} instance for the specified date.
   *
   * @param diagnosisKeyBundler A {@link DiagnosisKeyBundler} containing the {@link DiagnosisKey DiagnosisKeys}.
   * @param cryptoProvider      The {@link CryptoProvider} used for cryptographic signing.
   * @param distributionServiceConfig The configuration to set {@link DistributionServiceConfig}
   */
  public DiagnosisKeysHourDirectory(DiagnosisKeyBundler diagnosisKeyBundler, CryptoProvider cryptoProvider,
      DistributionServiceConfig distributionServiceConfig) {
    super(distributionServiceConfig.getApi().getHourPath(),
        indices -> {
          String country = (String) indices.pop().peek();
          return diagnosisKeyBundler.getHoursWithDistributableDiagnosisKeys(((LocalDate) indices.peek()), country);
        },
        LocalDateTime::getHour);

    this.diagnosisKeyBundler = diagnosisKeyBundler;
    this.cryptoProvider = cryptoProvider;
    this.distributionServiceConfig = distributionServiceConfig;
  }

  @Override
  public void prepare(ImmutableStack<Object> indices) {
    this.addWritableToAll(currentIndices -> {
      LocalDateTime currentHour = (LocalDateTime) currentIndices.peek();
      // The LocalDateTime currentHour already contains both the date and the hour information, so
      // we can throw away the LocalDate that's the second item on the stack from the "/date"
      // IndexDirectory.
      String country = (String) currentIndices.pop().pop().peek();

      List<DiagnosisKey> diagnosisKeysForCurrentHour =
          this.diagnosisKeyBundler.getDiagnosisKeysForHour(currentHour, country);

      long startTimestamp = currentHour.toEpochSecond(ZoneOffset.UTC);
      long endTimestamp = currentHour.plusHours(1).toEpochSecond(ZoneOffset.UTC);
      File<WritableOnDisk> temporaryExposureKeyExportFile = TemporaryExposureKeyExportFile.fromDiagnosisKeys(
          diagnosisKeysForCurrentHour, country, startTimestamp, endTimestamp, distributionServiceConfig);

      Archive<WritableOnDisk> hourArchive = new ArchiveOnDisk(distributionServiceConfig.getOutputFileName());
      hourArchive.addWritable(temporaryExposureKeyExportFile);

      return Optional.of(decorateDiagnosisKeyArchive(hourArchive));
    });
    super.prepare(indices);
  }

  private Directory<WritableOnDisk> decorateDiagnosisKeyArchive(Archive<WritableOnDisk> archive) {
    return new DistributionArchiveSigningDecorator(archive, cryptoProvider, distributionServiceConfig);
  }
}
