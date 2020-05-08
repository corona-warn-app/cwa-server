package app.coronawarn.server.tools.testdatagenerator.structure;

/**
 * A {@link Directory} that will write an aggregation of all its subdirectories.
 */
public interface AggregatingDirectory {

  void aggregate();
}
