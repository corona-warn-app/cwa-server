package app.coronawarn.server.services.distribution.assembly.tracewarnings.structure.directory.decorator;

import app.coronawarn.server.common.shared.collection.ImmutableStack;
import app.coronawarn.server.services.distribution.assembly.structure.file.FileOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.file.FileOnDiskWithChecksum;
import app.coronawarn.server.services.distribution.assembly.tracewarnings.TraceTimeIntervalWarningsPackageBundler;
import app.coronawarn.server.services.distribution.assembly.tracewarnings.structure.directory.TraceTimeIntervalWarningsHourV2Directory;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.json.simple.JSONObject;

/**
 * The v2 Implementation of hour interval indexing, which uses the encrypted checkins.
 */
public class HourIntervalIndexingV2Decorator extends AbstractHourIntervalIndexingDecorator {

  /**
   * Decorator for trace time interval warnings hour directory.
   *
   * @param directory                 the directory to decorate.
   * @param packageBundler            the package bundler that contains the tracetime warning.
   * @param distributionServiceConfig distribution config.
   */
  public HourIntervalIndexingV2Decorator(TraceTimeIntervalWarningsHourV2Directory directory,
      TraceTimeIntervalWarningsPackageBundler packageBundler, DistributionServiceConfig distributionServiceConfig) {
    super(directory, packageBundler, distributionServiceConfig);
  }


  /**
   * Calculates the oldest and latest hour of distributable trace time interval warnings.
   *
   * @param indexFileName the name of the file that will be written to disk and contains the JSON.
   * @param indices       the indices that indicate the current path inside a directory.
   * @return the file on disk that contains the oldest and latest as JSON with a checksum.
   */
  @Override
  public FileOnDisk getIndexFile(String indexFileName, ImmutableStack<Object> indices) {
    String currentCountry = (String) indices.peek();
    final Optional<Integer> oldestHourWithDistributableCheckIns = packageBundler
        .getOldestHourWithDistributableCheckIns(currentCountry);
    final Optional<Integer> latestHourWithDistributableCheckIns = packageBundler
        .getLatestHourWithDistributableCheckins(currentCountry);
    JSONObject object = new JSONObject();
    object.put("oldest", oldestHourWithDistributableCheckIns.orElse(null));
    object.put("latest", latestHourWithDistributableCheckIns.orElse(null));
    return new FileOnDiskWithChecksum(indexFileName, object.toJSONString().getBytes(StandardCharsets.UTF_8));
  }
}
