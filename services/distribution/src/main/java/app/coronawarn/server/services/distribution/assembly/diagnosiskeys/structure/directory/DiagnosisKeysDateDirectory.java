

package app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.directory;

import static java.lang.Boolean.FALSE;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.services.distribution.assembly.component.CryptoProvider;
import app.coronawarn.server.services.distribution.assembly.diagnosiskeys.DiagnosisKeyBundler;
import app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.archive.decorator.signing.DiagnosisKeySigningDecorator;
import app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.directory.decorator.HourIndexingDecorator;
import app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.file.TemporaryExposureKeyExportFile;
import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.archive.Archive;
import app.coronawarn.server.services.distribution.assembly.structure.archive.ArchiveOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.file.File;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class DiagnosisKeysDateDirectory extends IndexDirectoryOnDisk<LocalDate> {

  private static final DateTimeFormatter ISO8601 = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  private final DiagnosisKeyBundler diagnosisKeyBundler;
  private final CryptoProvider cryptoProvider;
  private final DistributionServiceConfig distributionServiceConfig;

  /**
   * Constructs a {@link DiagnosisKeysDateDirectory} instance associated with the specified {@link DiagnosisKey}
   * collection. Payload signing is be performed according to the specified {@link CryptoProvider}.
   *
   * @param diagnosisKeyBundler A {@link DiagnosisKeyBundler} containing the {@link DiagnosisKey DiagnosisKeys}.
   * @param cryptoProvider      The {@link CryptoProvider} used for payload signing.
   */
  public DiagnosisKeysDateDirectory(DiagnosisKeyBundler diagnosisKeyBundler,
      CryptoProvider cryptoProvider, DistributionServiceConfig distributionServiceConfig) {
    super(distributionServiceConfig.getApi().getDatePath(),
        indices -> {
          String country = (String) indices.peek();
          return diagnosisKeyBundler.getDatesWithDistributableDiagnosisKeys(country);
        }, ISO8601::format);
    this.cryptoProvider = cryptoProvider;
    this.diagnosisKeyBundler = diagnosisKeyBundler;
    this.distributionServiceConfig = distributionServiceConfig;
  }

  @Override
  public void prepare(ImmutableStack<Object> indices) {
    this.addWritableToAll(ignoredValue -> {
      DiagnosisKeysHourDirectory hourDirectory =
          new DiagnosisKeysHourDirectory(diagnosisKeyBundler, cryptoProvider, distributionServiceConfig);
      return Optional.of(decorateHourDirectory(hourDirectory));
    });
    this.addWritableToAll(this::indicesToDateDirectoryArchive);
    super.prepare(indices);
  }

  private Optional<Writable<WritableOnDisk>> indicesToDateDirectoryArchive(ImmutableStack<Object> currentIndices) {
    LocalDate currentDate = (LocalDate) currentIndices.peek();
    if (shouldNotInclude(currentDate)) {
      return Optional.empty();
    }
    String country = (String) currentIndices.pop().peek();

    List<DiagnosisKey> diagnosisKeysForCurrentHour =
        this.diagnosisKeyBundler.getDiagnosisKeysForDate(currentDate, country);

    long startTimestamp = currentDate.atStartOfDay().toEpochSecond(ZoneOffset.UTC);
    long endTimestamp = currentDate.plusDays(1).atStartOfDay().toEpochSecond(ZoneOffset.UTC);

    File<WritableOnDisk> temporaryExposureKeyExportFile = TemporaryExposureKeyExportFile.fromDiagnosisKeys(
        diagnosisKeysForCurrentHour, country, startTimestamp, endTimestamp, distributionServiceConfig);

    Archive<WritableOnDisk> dateArchive = new ArchiveOnDisk(distributionServiceConfig.getOutputFileName());
    dateArchive.addWritable(temporaryExposureKeyExportFile);

    return Optional.of(decorateDiagnosisKeyArchive(dateArchive));
  }

  private boolean shouldNotInclude(LocalDate currentDate) {
    return FALSE.equals(distributionServiceConfig.getIncludeIncompleteDays())
        && currentDate.equals(diagnosisKeyBundler.getDistributionTime().toLocalDate());
  }

  private Directory<WritableOnDisk> decorateHourDirectory(DiagnosisKeysHourDirectory hourDirectory) {
    return new HourIndexingDecorator(hourDirectory, distributionServiceConfig);
  }

  private Directory<WritableOnDisk> decorateDiagnosisKeyArchive(Archive<WritableOnDisk> archive) {
    return new DiagnosisKeySigningDecorator(archive, cryptoProvider, distributionServiceConfig);
  }
}
