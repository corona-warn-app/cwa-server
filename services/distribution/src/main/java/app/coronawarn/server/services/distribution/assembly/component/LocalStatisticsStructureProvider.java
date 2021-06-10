package app.coronawarn.server.services.distribution.assembly.component;

import app.coronawarn.server.common.protocols.internal.stats.LocalStatistics;
import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.statistics.directory.LocalStatisticsDirectory;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;


/**
 * Reads configuration parameters from the respective files in the class path and build a {@link
 * LocalStatisticsStructureProvider} with them.
 */
@Component
public class LocalStatisticsStructureProvider {

  private final CryptoProvider cryptoProvider;
  private final DistributionServiceConfig distributionServiceConfig;
  private final Map<Integer, LocalStatistics> localStatisticsMap;

  /**
   * Creates a new LocalStatisticsStructureProvider.
   */
  public LocalStatisticsStructureProvider(
      CryptoProvider cryptoProvider,
      DistributionServiceConfig distributionServiceConfig,
      Map<Integer, LocalStatistics> localStatisticsMap) {
    this.cryptoProvider = cryptoProvider;
    this.distributionServiceConfig = distributionServiceConfig;
    this.localStatisticsMap = localStatisticsMap;
  }

  /**
   * Returns a list containing the archives with Local Statistics for mobile clients using signature file.
   */
  public List<Writable<WritableOnDisk>> getLocalStatisticsList() {
    if (!localStatisticsMap.isEmpty()) {
      return localStatisticsMap
          .keySet()
          .stream()
          .map(key -> new LocalStatisticsDirectory(cryptoProvider, distributionServiceConfig,
              localStatisticsMap.get(key)).getLocalStatisticsArchive(key))
          .collect(Collectors.toList());
    }

    return Collections.emptyList();
  }
}
