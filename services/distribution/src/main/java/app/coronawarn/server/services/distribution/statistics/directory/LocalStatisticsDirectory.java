package app.coronawarn.server.services.distribution.statistics.directory;

import app.coronawarn.server.common.protocols.internal.stats.LocalStatistics;
import app.coronawarn.server.common.protocols.internal.stats.Statistics;
import app.coronawarn.server.services.distribution.assembly.component.CryptoProvider;
import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.archive.ArchiveOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.archive.decorator.signing.DistributionArchiveSigningDecorator;
import app.coronawarn.server.services.distribution.assembly.structure.file.FileOnDisk;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;

public class LocalStatisticsDirectory {

  private final LocalStatistics localStatistics;
  private final CryptoProvider cryptoProvider;
  private final DistributionServiceConfig distributionServiceConfig;

  /**
   * Creates an {@link LocalStatisticsDirectory} for the mobile structure that contains a file which will be used by
   * mobile devices.
   *
   * @param cryptoProvider The {@link CryptoProvider} whose artifacts to use for creating the signature.
   */
  public LocalStatisticsDirectory(
      CryptoProvider cryptoProvider,
      DistributionServiceConfig distributionServiceConfig,
      LocalStatistics localStatistics) {
    this.cryptoProvider = cryptoProvider;
    this.distributionServiceConfig = distributionServiceConfig;
    this.localStatistics = localStatistics;
  }

  /**
   * If the processing of the uploaded JSON file by TSI succeeds, it is written into a file, put into an archive with
   * the specified name and returned to be included in the CWA file structure.
   */
  public Writable<WritableOnDisk> getLocalStatisticsArchive(int regionGroup) {
    ArchiveOnDisk localStatisticsFile = new ArchiveOnDisk(
        distributionServiceConfig.getApi().getLocalStatisticsFileName() + "_" + regionGroup);
    localStatisticsFile
        .addWritable(new FileOnDisk("export.bin", localStatistics.toByteArray()));
    return new DistributionArchiveSigningDecorator(localStatisticsFile, cryptoProvider,
        distributionServiceConfig);
  }
}
