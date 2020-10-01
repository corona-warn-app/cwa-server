

package app.coronawarn.server.services.distribution.assembly.appconfig.parsing;

import org.yaml.snakeyaml.nodes.Node;

/**
 * Indicates, that the target property for a Yaml {@link IncludeConstruct} is not a valid Protobuf Message, because they
 * should contain a Message Builder. This Message.Builder is required for the processing.
 */
public class NoMessageBuilderOnClassException extends RuntimeException {

  /**
   * Creates a new exception instance based on the given {@link Node}.
   *
   * @param node the node, which points to a non-Protobuf message type.
   */
  public NoMessageBuilderOnClassException(Node node) {
    super(String.format("No Message.Builder on class: %s", node.getType().getSimpleName()));
  }
}
