package app.coronawarn.server.services.distribution.assembly.tracewarnings.structure.directory;

import app.coronawarn.server.common.persistence.domain.TraceTimeIntervalWarning;
import app.coronawarn.server.services.distribution.assembly.component.CryptoProvider;
import app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.archive.decorator.signing.DiagnosisKeySigningDecorator;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.archive.Archive;
import app.coronawarn.server.services.distribution.assembly.structure.archive.ArchiveOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.file.File;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import app.coronawarn.server.services.distribution.assembly.tracewarnings.TraceTimeIntervalWarningsPackageBundler;
import app.coronawarn.server.services.distribution.assembly.tracewarnings.structure.file.TraceTimeIntervalWarningExportFile;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.util.List;
import java.util.Optional;

public class TraceTimeWarningsHourDirectory extends IndexDirectoryOnDisk<Integer> {

  private TraceTimeIntervalWarningsPackageBundler traceWarningsBundler;
  private CryptoProvider cryptoProvider;
  private DistributionServiceConfig distributionServiceConfig;

  public TraceTimeWarningsHourDirectory(TraceTimeIntervalWarningsPackageBundler traceWarningsBundler,
      CryptoProvider cryptoProvider, DistributionServiceConfig distributionServiceConfig) {
    super(distributionServiceConfig.getApi().getHourPath(), indices -> {
      String country = (String) indices.peek();
      return traceWarningsBundler.getHoursSinceEpochWithDistributableWarnings(country);
    }, Integer::valueOf);

    this.traceWarningsBundler = traceWarningsBundler;
    this.cryptoProvider = cryptoProvider;
    this.distributionServiceConfig = distributionServiceConfig;
  }

  @Override
  public void prepare(ImmutableStack<Object> indices) {
    this.addWritableToAll(currentIndices -> {
      Integer hourSinceEpoch = (Integer) currentIndices.peek();
      String country = (String) currentIndices.pop().peek();

      List<TraceTimeIntervalWarning> traceWarningsForCurrentHour =
          this.traceWarningsBundler.getTraceTimeWarningsForHour(hourSinceEpoch);

      File<WritableOnDisk> temporaryExposureKeyExportFile = TraceTimeIntervalWarningExportFile
          .fromTraceTimeIntervalWarnings(traceWarningsForCurrentHour, country,
              hourSinceEpoch, distributionServiceConfig);

      Archive<WritableOnDisk> hourArchive =
          new ArchiveOnDisk(distributionServiceConfig.getOutputFileName());
      hourArchive.addWritable(temporaryExposureKeyExportFile);

      return Optional.of(decorateDiagnosisKeyArchive(hourArchive));
    });
    super.prepare(indices);
  }

  private Directory<WritableOnDisk> decorateDiagnosisKeyArchive(Archive<WritableOnDisk> archive) {
    return new DiagnosisKeySigningDecorator(archive, cryptoProvider, distributionServiceConfig);
  }
}
