package app.coronawarn.server.services.distribution.assembly.tracewarnings.structure.directory.decorator;

import app.coronawarn.server.common.shared.collection.ImmutableStack;
import app.coronawarn.server.services.distribution.assembly.structure.file.FileOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.file.FileOnDiskWithChecksum;
import app.coronawarn.server.services.distribution.assembly.tracewarnings.TraceTimeIntervalWarningsPackageBundler;
import app.coronawarn.server.services.distribution.assembly.tracewarnings.structure.directory.TraceTimeIntervalWarningsHourV1Directory;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.json.simple.JSONObject;

/**
 * The v1 Implementation of hour interval indexing.
 *
 * @deprecated because trace time warnings are being replaced by protected reports.
 */
@Deprecated(since = "2.8")
public class HourIntervalIndexingV1Decorator extends AbstractHourIntervalIndexingDecorator {

  /**
   * Decorator for trace time interval warnings hour directory.
   *
   * @param directory                 the directory to decorate.
   * @param packageBundler            the package bundler that contains the tracetime warning.
   * @param distributionServiceConfig distribution config.
   * @deprecated because trace time warnings are being replaced by protected reports.
   */
  @Deprecated(since = "2.8")
  public HourIntervalIndexingV1Decorator(TraceTimeIntervalWarningsHourV1Directory directory,
      TraceTimeIntervalWarningsPackageBundler packageBundler, DistributionServiceConfig distributionServiceConfig) {
    super(directory, packageBundler, distributionServiceConfig);
  }

  /**
   * Calculates the oldest and latest hour of distributable trace time interval warnings.
   *
   * @param indexFileName the name of the file that will be written to disk and contains the JSON.
   * @param indices       the indices that indicate the current path inside a directory.
   * @return the file on disk that contains the oldest and latest as JSON with a checksum.
   * @deprecated because trace time warnings are being replaced by protected reports.
   */
  @Override
  @Deprecated(since = "2.8")
  public FileOnDisk getIndexFile(String indexFileName, ImmutableStack<Object> indices) {
    String currentCountry = (String) indices.peek();
    final Optional<Integer> oldestHourWithDistributableWarnings = packageBundler
        .getOldestHourWithDistributableWarnings(currentCountry);
    final Optional<Integer> latestHourWithDistributableWarnings = packageBundler
        .getLatestHourWithDistributableWarnings(currentCountry);
    JSONObject object = new JSONObject();
    object.put("oldest", oldestHourWithDistributableWarnings.orElse(null));
    object.put("latest", latestHourWithDistributableWarnings.orElse(null));
    return new FileOnDiskWithChecksum(indexFileName, object.toJSONString().getBytes(StandardCharsets.UTF_8));
  }
}
