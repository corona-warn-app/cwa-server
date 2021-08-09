package app.coronawarn.server.services.distribution.assembly.tracewarnings.structure.directory.decorator;

import app.coronawarn.server.common.shared.collection.ImmutableStack;
import app.coronawarn.server.services.distribution.assembly.structure.directory.decorator.indexing.IndexingDecoratorOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.file.FileOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.file.FileOnDiskWithChecksum;
import app.coronawarn.server.services.distribution.assembly.tracewarnings.TraceTimeIntervalWarningsPackageBundler;
import app.coronawarn.server.services.distribution.assembly.tracewarnings.structure.directory.TraceTimeIntervalWarningsHourDirectory;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.json.simple.JSONObject;

public class HourIntervalIndexingDecorator extends IndexingDecoratorOnDisk<Integer> {

  private String version;
  private TraceTimeIntervalWarningsPackageBundler packageBundler;
  public static String VERSION_V1 = "v1";

  /**
   * Decorator for trace time interval warnings hour directory.
   *
   * @param directory                 the directory to decorate.
   * @param packageBundler            the package bundler that contains the tracetime warning.
   * @param distributionServiceConfig distribution config.
   */
  public HourIntervalIndexingDecorator(TraceTimeIntervalWarningsHourDirectory directory,
      TraceTimeIntervalWarningsPackageBundler packageBundler,
      DistributionServiceConfig distributionServiceConfig,
      final String version) {
    super(directory, distributionServiceConfig.getOutputFileName());
    this.packageBundler = packageBundler;
    this.version = version;
  }

  @SuppressWarnings("unchecked")
  @Override
  public FileOnDisk getIndexFile(String indexFileName, ImmutableStack<Object> indices) {
    String currentCountry = (String) indices.peek();

    if (this.version.equals(VERSION_V1)) {
      final Optional<Integer> oldestHourWithDistributableWarnings = packageBundler
          .getOldestHourWithDistributableWarnings(currentCountry);
      final Optional<Integer> latestHourWithDistributableWarnings = packageBundler
          .getLatestHourWithDistributableWarnings(currentCountry);
      JSONObject object = new JSONObject();
      object.put("oldest", oldestHourWithDistributableWarnings.orElse(null));
      object.put("latest", latestHourWithDistributableWarnings.orElse(null));
      return new FileOnDiskWithChecksum(indexFileName, object.toJSONString().getBytes(StandardCharsets.UTF_8));
    } else {
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
}
