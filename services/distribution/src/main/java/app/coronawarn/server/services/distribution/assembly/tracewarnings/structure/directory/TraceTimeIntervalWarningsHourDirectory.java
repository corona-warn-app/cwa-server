package app.coronawarn.server.services.distribution.assembly.tracewarnings.structure.directory;

import app.coronawarn.server.common.persistence.domain.CheckInProtectedReports;
import app.coronawarn.server.common.persistence.domain.TraceTimeIntervalWarning;
import app.coronawarn.server.common.shared.collection.ImmutableStack;
import app.coronawarn.server.services.distribution.assembly.component.CryptoProvider;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.archive.Archive;
import app.coronawarn.server.services.distribution.assembly.structure.archive.ArchiveOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.archive.decorator.signing.DistributionArchiveSigningDecorator;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.file.File;
import app.coronawarn.server.services.distribution.assembly.structure.file.FileOnDiskWithChecksum;
import app.coronawarn.server.services.distribution.assembly.tracewarnings.TraceTimeIntervalWarningsPackageBundler;
import app.coronawarn.server.services.distribution.assembly.tracewarnings.structure.file.CheckInProtectedReportsExportFile;
import app.coronawarn.server.services.distribution.assembly.tracewarnings.structure.file.TraceTimeIntervalWarningExportFile;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.util.List;
import java.util.Optional;

public class TraceTimeIntervalWarningsHourDirectory extends IndexDirectoryOnDisk<Integer> {

  public static final String VERSION_V1 = "v1";
  private TraceTimeIntervalWarningsPackageBundler traceWarningsBundler;
  private CryptoProvider cryptoProvider;
  private DistributionServiceConfig distributionServiceConfig;
  private String version;

  /**
   * Creates an instance of the directory that holds packages for an hour since epoch, as defined by the API spec.
   */
  public TraceTimeIntervalWarningsHourDirectory(
      TraceTimeIntervalWarningsPackageBundler traceWarningsBundler, CryptoProvider cryptoProvider,
      DistributionServiceConfig distributionServiceConfig, String version) {
    super(distributionServiceConfig.getApi().getHourPath(), indices -> {
      String country = (String) indices.peek();

      if (version.equals(VERSION_V1)) {
        return traceWarningsBundler.getHoursForDistributableWarnings(country);
      } else {
        return traceWarningsBundler.getHoursForDistributableCheckInProtectedReports(country);
      }
    }, Integer::valueOf);

    this.traceWarningsBundler = traceWarningsBundler;
    this.cryptoProvider = cryptoProvider;
    this.distributionServiceConfig = distributionServiceConfig;
    this.version = version;
  }

  @Override
  public void prepare(ImmutableStack<Object> indices) {
    this.addWritableToAll(currentIndices -> {
      Integer hourSinceEpoch = (Integer) currentIndices.peek();
      String country = (String) currentIndices.pop().peek();
      if (version.equals(VERSION_V1)) {
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
      } else {
        List<CheckInProtectedReports> checkInReportsForHour =
            this.traceWarningsBundler.getCheckInProtectedReportsForHour(hourSinceEpoch);
        if (checkInReportsForHour.isEmpty()) {
          return Optional.of(new FileOnDiskWithChecksum("index", new byte[0]));
        }

        File<WritableOnDisk> checkInProtectedReportsExportFile =
            CheckInProtectedReportsExportFile.fromCheckInProtectedReports(
                checkInReportsForHour, country, hourSinceEpoch, distributionServiceConfig);

        Archive<WritableOnDisk> hourArchive =
            new ArchiveOnDisk(distributionServiceConfig.getOutputFileName());
        hourArchive.addWritable(checkInProtectedReportsExportFile);

        return Optional.of(decorateTraceWarningArchives(hourArchive));
      }
    });
    super.prepare(indices);
  }

  private Directory<WritableOnDisk> decorateTraceWarningArchives(Archive<WritableOnDisk> archive) {
    return new DistributionArchiveSigningDecorator(archive, cryptoProvider, distributionServiceConfig);
  }

  public String getVersion() {
    return version;
  }
}
