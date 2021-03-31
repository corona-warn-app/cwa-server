package app.coronawarn.server.services.distribution.assembly.tracewarnings.structure.directory.decorator;

import app.coronawarn.server.services.distribution.assembly.structure.directory.decorator.indexing.IndexingDecoratorOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.file.FileOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.file.FileOnDiskWithChecksum;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import app.coronawarn.server.services.distribution.assembly.tracewarnings.TraceTimeIntervalWarningsPackageBundler;
import app.coronawarn.server.services.distribution.assembly.tracewarnings.structure.directory.TraceTimeIntervalWarningsHourDirectory;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.json.simple.JSONArray;

public class HourIntervalIndexingDecorator extends IndexingDecoratorOnDisk<Integer> {

  private TraceTimeIntervalWarningsPackageBundler packageBundler;

  public HourIntervalIndexingDecorator(TraceTimeIntervalWarningsHourDirectory directory,
      TraceTimeIntervalWarningsPackageBundler packageBundler,
      DistributionServiceConfig distributionServiceConfig) {
    super(directory, distributionServiceConfig.getOutputFileName());
    this.packageBundler = packageBundler;
  }

  @Override
  public FileOnDisk getIndexFile(String indexFileName, ImmutableStack<Object> indices) {
    String currentCountry = (String) indices.peek();
    Set<Integer> index = packageBundler.getHourIntervalForDistributableWarnings(currentCountry);
    JSONArray array = new JSONArray();
    List<?> elements = index.stream()
        .map(this.getIndexFormatter())
        .sorted()
        .collect(Collectors.toList());
    array.addAll(elements);
    return new FileOnDiskWithChecksum(indexFileName, array.toJSONString().getBytes(StandardCharsets.UTF_8));
  }
}
