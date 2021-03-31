package app.coronawarn.server.services.distribution.assembly.tracewarnings.structure.directory.decorator;

import app.coronawarn.server.services.distribution.assembly.structure.directory.decorator.indexing.IndexingDecoratorOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import app.coronawarn.server.services.distribution.assembly.tracewarnings.TraceTimeIntervalWarningsPackageBundler;
import app.coronawarn.server.services.distribution.assembly.tracewarnings.structure.directory.TraceTimeIntervalWarningsHourDirectory;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.util.Set;

public class HourIntervalIndexingDecorator extends IndexingDecoratorOnDisk<Integer> {


  private TraceTimeIntervalWarningsPackageBundler packageBundler;

  public HourIntervalIndexingDecorator(TraceTimeIntervalWarningsHourDirectory directory,
      TraceTimeIntervalWarningsPackageBundler packageBundler,
      DistributionServiceConfig distributionServiceConfig) {
    super(directory, distributionServiceConfig.getOutputFileName());
    this.packageBundler = packageBundler;
  }

  /**
   * Returns the index of the decorated {@link TraceTimeIntervalWarningsHourDirectory}.
   */
  @Override
  public Set<Integer> getIndex(ImmutableStack<Object> indices) {
    final String currentCountry = (String) indices.peek();
    return packageBundler.getHourIntervalForDistributableWarnings(currentCountry);
  }

}
