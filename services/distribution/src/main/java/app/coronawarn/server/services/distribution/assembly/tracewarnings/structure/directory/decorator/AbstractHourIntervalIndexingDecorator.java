package app.coronawarn.server.services.distribution.assembly.tracewarnings.structure.directory.decorator;

import app.coronawarn.server.services.distribution.assembly.structure.directory.decorator.indexing.IndexingDecoratorOnDisk;
import app.coronawarn.server.services.distribution.assembly.tracewarnings.TraceTimeIntervalWarningsPackageBundler;
import app.coronawarn.server.services.distribution.assembly.tracewarnings.structure.directory.AbstractTraceTimeIntervalWarningsHourDirectory;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;

/**
 * I'm responsible for providing common indexing logic. In this case it means that this is the base for v1 and v2
 * Implementations for hour interval indexing.
 */
public class AbstractHourIntervalIndexingDecorator extends IndexingDecoratorOnDisk<Integer> {


  protected final TraceTimeIntervalWarningsPackageBundler packageBundler;

  /**
   * Decorator for trace time interval warnings hour directory.
   *
   * @param directory                 the directory to decorate.
   * @param packageBundler            the package bundler that contains the tracetime warning.
   * @param distributionServiceConfig distribution config.
   */
  public AbstractHourIntervalIndexingDecorator(AbstractTraceTimeIntervalWarningsHourDirectory directory,
      TraceTimeIntervalWarningsPackageBundler packageBundler,
      DistributionServiceConfig distributionServiceConfig) {
    super(directory, distributionServiceConfig.getOutputFileName());
    this.packageBundler = packageBundler;
  }
}
