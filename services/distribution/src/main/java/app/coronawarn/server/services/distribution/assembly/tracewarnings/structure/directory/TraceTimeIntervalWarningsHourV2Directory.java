package app.coronawarn.server.services.distribution.assembly.tracewarnings.structure.directory;

import app.coronawarn.server.common.persistence.domain.CheckInProtectedReports;
import app.coronawarn.server.common.shared.collection.ImmutableStack;
import app.coronawarn.server.services.distribution.assembly.component.CryptoProvider;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.archive.Archive;
import app.coronawarn.server.services.distribution.assembly.structure.archive.ArchiveOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.file.File;
import app.coronawarn.server.services.distribution.assembly.structure.file.FileOnDiskWithChecksum;
import app.coronawarn.server.services.distribution.assembly.tracewarnings.TraceTimeIntervalWarningsPackageBundler;
import app.coronawarn.server.services.distribution.assembly.tracewarnings.structure.file.CheckInProtectedReportsExportFile;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hour directory for checkins in the v2 implementation.
 */
public class TraceTimeIntervalWarningsHourV2Directory extends AbstractTraceTimeIntervalWarningsHourDirectory {

  private static final Logger logger = LoggerFactory.getLogger(TraceTimeIntervalWarningsHourV2Directory.class);

  /**
   * Creates an instance of the directory that holds packages for an hour since epoch, as defined by the API spec.
   */
  public TraceTimeIntervalWarningsHourV2Directory(
      TraceTimeIntervalWarningsPackageBundler traceWarningsBundler, CryptoProvider cryptoProvider,
      DistributionServiceConfig distributionServiceConfig) {
    super(traceWarningsBundler, cryptoProvider, distributionServiceConfig,
        indices -> {
          String country = (String) indices.peek();
          return traceWarningsBundler.getHoursForDistributableCheckInProtectedReports(country);
        }, Integer::valueOf);
  }

  @Override
  public void prepare(ImmutableStack<Object> indices) {
    this.addWritableToAll(currentIndices -> {
      Integer hourSinceEpoch = (Integer) currentIndices.peek();
      String country = (String) currentIndices.pop().peek();

      List<CheckInProtectedReports> checkInReportsForHour =
          this.traceWarningsBundler.getCheckInProtectedReportsForHour(hourSinceEpoch);
      if (checkInReportsForHour.isEmpty()) {
        return Optional.of(new FileOnDiskWithChecksum("index", new byte[0]));
      }
      logger.debug("Building protected reports export file for hour {} and country {} with {} encrypted checkins.",
          hourSinceEpoch, country, checkInReportsForHour.size());
      File<WritableOnDisk> checkInProtectedReportsExportFile =
          CheckInProtectedReportsExportFile.fromCheckInProtectedReports(
              checkInReportsForHour, country, hourSinceEpoch, distributionServiceConfig);

      Archive<WritableOnDisk> hourArchive =
          new ArchiveOnDisk(distributionServiceConfig.getOutputFileName());
      hourArchive.addWritable(checkInProtectedReportsExportFile);

      return Optional.of(decorateTraceWarningArchives(hourArchive));
    });
    super.prepare(indices);
  }
}
