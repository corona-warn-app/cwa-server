package app.coronawarn.server.services.distribution.assembly.tracewarnings.structure.directory;

import app.coronawarn.server.common.persistence.domain.TraceTimeIntervalWarning;
import app.coronawarn.server.common.shared.collection.ImmutableStack;
import app.coronawarn.server.services.distribution.assembly.component.CryptoProvider;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.archive.Archive;
import app.coronawarn.server.services.distribution.assembly.structure.archive.ArchiveOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.file.File;
import app.coronawarn.server.services.distribution.assembly.structure.file.FileOnDiskWithChecksum;
import app.coronawarn.server.services.distribution.assembly.tracewarnings.TraceTimeIntervalWarningsPackageBundler;
import app.coronawarn.server.services.distribution.assembly.tracewarnings.structure.file.TraceTimeIntervalWarningExportFile;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.util.List;
import java.util.Optional;

/**
 * Hour directory for checkins in the v1 implementation.
 */
@Deprecated(since = "2.8")
public class TraceTimeIntervalWarningsHourV1Directory extends AbstractTraceTimeIntervalWarningsHourDirectory {

  /**
   * Creates an instance of the directory that holds packages for an hour since epoch, as defined by the API spec.
   *
   * @deprecated because trace time warnings are being replaced by protected reports.
   */
  @Deprecated(since = "2.8")
  public TraceTimeIntervalWarningsHourV1Directory(
      TraceTimeIntervalWarningsPackageBundler traceWarningsBundler, CryptoProvider cryptoProvider,
      DistributionServiceConfig distributionServiceConfig) {
    super(traceWarningsBundler, cryptoProvider, distributionServiceConfig,
        indices -> {
          String country = (String) indices.peek();
          return traceWarningsBundler.getHoursForDistributableWarnings(country);
        }, Integer::valueOf);
  }

  @Override
  public void prepare(ImmutableStack<Object> indices) {
    this.addWritableToAll(currentIndices -> {
      Integer hourSinceEpoch = (Integer) currentIndices.peek();
      String country = (String) currentIndices.pop().peek();

      List<TraceTimeIntervalWarning> traceWarningsForCurrentHour =
          this.traceWarningsBundler.getTraceTimeWarningsForHour(hourSinceEpoch);
      if (traceWarningsForCurrentHour.isEmpty()) {
        return Optional.of(new FileOnDiskWithChecksum("index", new byte[0]));
      }

      File<WritableOnDisk> traceTimeIntervalWarningExportFile =
          TraceTimeIntervalWarningExportFile.fromTraceTimeIntervalWarnings(
              traceWarningsForCurrentHour, country, hourSinceEpoch, distributionServiceConfig);

      Archive<WritableOnDisk> hourArchive =
          new ArchiveOnDisk(distributionServiceConfig.getOutputFileName());
      hourArchive.addWritable(traceTimeIntervalWarningExportFile);

      return Optional.of(decorateTraceWarningArchives(hourArchive));
    });
    super.prepare(indices);
  }
}
