

package app.coronawarn.server.services.distribution.assembly.appconfig.parsing;

import app.coronawarn.server.common.shared.exception.UnableToLoadFileException;
import org.yaml.snakeyaml.nodes.ScalarNode;

/**
 * This exception is raised in case SnakeYaml was not able to map the content of the include to the target data type.
 */
public class IncludeResolveFailedException extends RuntimeException {

  /**
   * Creates a new Include Resolve Failed Exception.
   *
   * @param scalarNode the node for which the processing failed
   * @param e          the reason
   */
  public IncludeResolveFailedException(ScalarNode scalarNode, UnableToLoadFileException e) {
    super(computeMessage(scalarNode), e);
  }

  private static String computeMessage(ScalarNode scalarNode) {
    return String.format("Unable to resolve Yaml node: %s -> %s", scalarNode.getNodeId().name(), scalarNode.getValue());
  }
}
